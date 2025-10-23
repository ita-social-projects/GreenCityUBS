package greencity.ubstelegrambot.service;

import greencity.constant.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.enums.MessageType;
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
    private final TelegramBotResponseServiceImpl telegramBotResponseService;

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processCommand(Message message, String lang) {
        String chatId = message.getChatId().toString();

        if (message.getText() == null) {
            String text = telegramBotResponseService.getResponseByLangAndMessageType(lang,
                MessageType.UNKNOWN_COMMAND);
            return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                MessageFactory.createUnknownCommandMessage(chatId, lang, text));
        }
        String content = message.getText().split(" ")[0];

        switch (content) {
            case TelegramBotConstants.START_COMMAND, TelegramBotConstants.HELP_COMMAND -> {
                String text = telegramBotResponseService.getResponseByLangAndMessageType(lang,
                    MessageType.SUPPORTED_COMMANDS);
                return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                    MessageFactory.createAvailableCommandsMessage(chatId, lang, text));
            }
            case TelegramBotConstants.SUPPORT_COMMAND -> {
                String text = telegramBotResponseService.getResponseByLangAndMessageType(lang,
                    MessageType.CLIENT_SUPPORT_MESSAGE_CALLBACK_QUERY);
                return telegramUtils.updateChatStateAndRespond(chatId, ChatState.IN_SUPPORT,
                    MessageFactory.createSupportMessageCallBackQuery(chatId, lang, text));
            }
            case TelegramBotConstants.LOGIN_COMMAND -> {
                String text = telegramBotResponseService.getResponseByLangAndMessageType(lang,
                    MessageType.LOGIN_MESSAGE);
                return telegramUtils.updateChatStateAndRespond(chatId, ChatState.LOGGING_AS_MANAGER,
                    MessageFactory.createLoginMessage(chatId, lang, text));
            }
            default -> {
                String text = telegramBotResponseService.getResponseByLangAndMessageType(lang,
                    MessageType.UNKNOWN_COMMAND);
                return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                    MessageFactory.createUnknownCommandMessage(chatId, lang, text));
            }
        }
    }
}
