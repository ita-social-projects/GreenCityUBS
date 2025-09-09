package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.service.ubs.TelegramCommandsService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class TelegramCommandsServiceImpl implements TelegramCommandsService {
    private final TelegramUtils telegramUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processCommand(Message message, String lang) {
        String chatId = message.getChatId().toString();

        if (message.getText() == null) {
            return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                MessageFactory.createUnknownCommandMessage(chatId, TelegramBotConstants.UK));
        }
        String text = message.getText().split(" ")[0];

        switch (text) {
            case TelegramBotConstants.START_COMMAND, TelegramBotConstants.HELP_COMMAND -> {
                return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                    MessageFactory.createAvailableCommandsMessage(chatId, lang));
            }
            case TelegramBotConstants.SUPPORT_COMMAND -> {
                return telegramUtils.updateChatStateAndRespond(chatId, ChatState.IN_SUPPORT,
                    MessageFactory.createSupportMessageCallBackQuery(chatId, lang));
            }
            case TelegramBotConstants.LOGIN_COMMAND -> {
                return telegramUtils.updateChatStateAndRespond(chatId, ChatState.LOGGING_AS_MANAGER,
                    MessageFactory.createLoginMessage(chatId, lang));
            }
            default -> {
                return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                    MessageFactory.createUnknownCommandMessage(chatId, lang));
            }
        }
    }
}
