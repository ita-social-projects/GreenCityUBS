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

        switch (callback) {
            case TelegramBotConstants.SET_LANGUAGE_UA_CALLBACK -> {
                if (chat.getLanguageCode().equals(TelegramBotConstants.UA)) {
                    break;
                }
                chat.setLanguageCode(TelegramBotConstants.UA);
                chatRepository.save(chat);
                return processLanguageSwitchRequest(chatId, chat.getLanguageCode());
            }
            case TelegramBotConstants.SET_LANGUAGE_EN_CALLBACK -> {
                if (chat.getLanguageCode().equals(TelegramBotConstants.EN)) {
                    break;
                }
                chat.setLanguageCode(TelegramBotConstants.EN);
                chatRepository.save(chat);
                return processLanguageSwitchRequest(chatId, chat.getLanguageCode());
            }
        }
        return null;
    }

    private SendMessage processLanguageSwitchRequest(String chatId, String lang) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory.createAvailableCommandsMessage(chatId, lang));
    }
}
