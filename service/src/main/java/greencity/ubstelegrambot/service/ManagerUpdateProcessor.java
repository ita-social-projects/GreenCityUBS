package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.service.ubs.TelegramLoginService;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service("managerUpdateProcessor")
@RequiredArgsConstructor
public class ManagerUpdateProcessor implements TelegramUpdateProcessor {
    private final TelegramLoginService telegramLoginService;
    private final TelegramUtils telegramUtils;

    /**
     * Handles incoming updates related to manager interactions in Telegram.
     * {@inheritDoc}
     */
    @Override
    public SendMessage process(Update update) {
        if (update.hasCallbackQuery()) {
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            CallbackQuery callBackQuery = update.getCallbackQuery();
            if (callBackQuery.getData().equals(TelegramBotConstants.LOGOUT_MANAGER_CALLBACK)) {
                telegramLoginService.logoutManager(chatId);
                return processMainMenuRequest(chatId);
            } else {
                return processManagerCallBackQueryRequest(chatId);
            }
        }
        return processManagerMessageRequest(update.getMessage().getChatId().toString());
    }

    private SendMessage processMainMenuRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createAvailableCommandsMessage);
    }

    private SendMessage processManagerMessageRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createAvailableForManagerCommandsMessage);
    }

    private SendMessage processManagerCallBackQueryRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createForbiddenCommandsManagerMessage);
    }
}
