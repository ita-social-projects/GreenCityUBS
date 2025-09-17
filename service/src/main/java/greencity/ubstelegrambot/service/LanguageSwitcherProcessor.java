package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.enums.MessageType;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramBotResponseService;
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
    private final TelegramManagerRepository telegramManagerRepository;
    private final TelegramUtils telegramUtils;
    private final TelegramBotResponseService telegramBotResponseService;

    /**
     * Handles incoming updates related to switch language in Telegram.
     * {@inheritDoc}
     */
    @Override
    public SendMessage process(Update update) {
        String chatId = update.getCallbackQuery().getFrom().getId().toString();
        String callback = update.getCallbackQuery().getData();

        TelegramChat chat = chatRepository.findByChatId(chatId)
            .orElseThrow(() -> new RuntimeException("Chat not found"));
        String newLanguage = switch (callback) {
            case TelegramBotConstants.SET_LANGUAGE_UK_CALLBACK -> TelegramBotConstants.UK;
            case TelegramBotConstants.SET_LANGUAGE_EN_CALLBACK -> TelegramBotConstants.EN;
            default -> null;
        };

        if (newLanguage == null || newLanguage.equals(chat.getLanguageCode())) {
            return null;
        }

        chat.setLanguageCode(newLanguage);
        chatRepository.save(chat);

        switch (chat.getChatState()) {
            case NORMAL -> {
                if (telegramManagerRepository.existsByChatId(chatId)) {
                    return processLanguageSwitchForManagerRequest(chatId, newLanguage);
                }
                return processLanguageSwitchRequest(chatId, newLanguage);
            }
            case IN_SUPPORT -> {
                String text = telegramBotResponseService.getResponseByLangAndMessageType(newLanguage,
                    MessageType.CLIENT_SUPPORT_MESSAGE_CHANGE_LANGUAGE);
                return MessageFactory.createSupportReplyMarkup(chatId, newLanguage, text);
            }
            default -> {
                return null;
            }
        }
    }

    private SendMessage processLanguageSwitchRequest(String chatId, String lang) {
        String text = telegramBotResponseService.getResponseByLangAndMessageType(lang,
            MessageType.SUPPORTED_COMMANDS);
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory.createAvailableCommandsMessage(chatId, lang, text));
    }

    private SendMessage processLanguageSwitchForManagerRequest(String chatId, String lang) {
        String text = telegramBotResponseService.getResponseByLangAndMessageType(lang,
            MessageType.SUPPORTED_COMMANDS);
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory.createAvailableForManagerCommandsMessage(chatId, lang, text));
    }
}
