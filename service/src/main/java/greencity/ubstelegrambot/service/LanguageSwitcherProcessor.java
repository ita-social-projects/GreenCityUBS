package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service("languageSwitcherProcessor")
@RequiredArgsConstructor
public class LanguageSwitcherProcessor implements TelegramUpdateProcessor {
    private final TelegramChatRepository chatRepository;
    private final TelegramUtils telegramUtils;

    @Override
    public SendMessage process(Update update) {
        String chatId = update.getCallbackQuery().getFrom().getId().toString();
        String callback = update.getCallbackQuery().getData();

        TelegramChat chat = chatRepository.findByChatId(chatId)
            .orElseThrow(() -> new RuntimeException("Chat not found"));
        String newLanguage = switch (callback) {
            case TelegramBotConstants.SET_LANGUAGE_UA_CALLBACK -> TelegramBotConstants.UA;
            case TelegramBotConstants.SET_LANGUAGE_EN_CALLBACK -> TelegramBotConstants.EN;
            default -> null;
        };

        if (newLanguage == null || newLanguage.equals(chat.getLanguageCode())) {
            return null;
        }

        chat.setLanguageCode(newLanguage);
        chatRepository.save(chat);

        if (chat.getChatState() == ChatState.NORMAL)
            return processLanguageSwitchRequest(chatId, newLanguage);
        else if (chat.getChatState() == ChatState.IN_SUPPORT) {
            return MessageFactory.createSupportReplyMarkup(chatId, newLanguage);
        } else
            return null;
    }

    private SendMessage processLanguageSwitchRequest(String chatId, String lang) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory.createAvailableCommandsMessage(chatId, lang));
    }
}
