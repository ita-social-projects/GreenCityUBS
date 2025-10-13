package greencity.ubstelegrambot.service;

import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.enums.MessageType;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.TelegramBotResponseService;
import greencity.service.ubs.TelegramGreenOfficeService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramGreenOfficeServiceImpl implements TelegramGreenOfficeService {
    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;
    private final TelegramBotResponseService telegramBotResponseService;

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processGreenOfficeEmail(Message message, String lang) {
        String email = message.getText();
        if (!TelegramUtils.isValidEmail(email)) {
            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.INVALID_EMAIL_MESSAGE);
            return MessageFactory.createInvalidEmailMessage(message.getChatId().toString(), lang, text);
        }

        Optional<TelegramChat> optChat = telegramChatRepository.findByChatId(message.getFrom().getId().toString());

        if (optChat.isEmpty()) {
            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.UNKNOWN_ERROR);
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString(), lang, text);
        }

        TelegramChat chat = optChat.get();

        String username =
            chat.getUser() != null ? chat.getUser().getRecipientName() + " " + chat.getUser().getRecipientSurname()
                : message.getFrom().getUserName();

        notificationService.notifyManagerWithNewGreenOfficeRequestFromTelegramBot(email, username, lang);
        chat.setChatState(ChatState.NORMAL);
        chat.setChatStateUpdatedAt(Instant.now());
        telegramChatRepository.save(chat);
        String text = telegramBotResponseService.getResponseByLangAndMessageType(
            lang, MessageType.GREEN_OFFICE_THANK_YOU_MESSAGE);
        return MessageFactory.createGreenOfficeThanksMessage(message.getChatId().toString(), lang, text);
    }
}
