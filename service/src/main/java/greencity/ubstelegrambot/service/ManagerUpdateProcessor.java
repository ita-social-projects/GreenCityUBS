package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.service.ubs.TelegramLanguageService;
import greencity.service.ubs.TelegramLoginService;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service("managerUpdateProcessor")
@RequiredArgsConstructor
public class ManagerUpdateProcessor implements TelegramUpdateProcessor {
    private final TelegramLoginService telegramLoginService;
    private final TelegramUtils telegramUtils;
    private final TelegramLanguageService telegramLanguageService;

    /**
     * Handles incoming updates related to manager interactions in Telegram.
     * {@inheritDoc}
     */
    @Override
    public SendMessage process(Update update) {
        String chatId = update.hasCallbackQuery() ?
                update.getCallbackQuery().getMessage().getChatId().toString()
                : update.getMessage().getChatId().toString();
        String lang = telegramLanguageService.getChatLanguage(chatId);
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals(TelegramBotConstants.LOGOUT_MANAGER_CALLBACK)) {
                telegramLoginService.logoutManager(chatId);
                return processMainMenuRequest(chatId, lang);
            } else {
                return processManagerCallBackQueryRequest(chatId, lang);
            }
        }
        return processManagerMessageRequest(chatId, lang);
    }

    private SendMessage processMainMenuRequest(String chatId,  String lang) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory.createAvailableCommandsMessage(chatId, lang));
    }

    private SendMessage processManagerMessageRequest(String chatId,  String lang) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory.createAvailableForManagerCommandsMessage(chatId, lang));
    }

    private SendMessage processManagerCallBackQueryRequest(String chatId, String lang) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory.createForbiddenCommandsManagerMessage(chatId, lang));
    }
}
