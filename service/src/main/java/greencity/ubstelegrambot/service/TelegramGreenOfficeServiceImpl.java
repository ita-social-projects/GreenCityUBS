package greencity.ubstelegrambot.service;

import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.TelegramGreenOfficeService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramGreenOfficeServiceImpl implements TelegramGreenOfficeService {
    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processGreenOfficeEmail(Message message, String lang) {
        String email = message.getText();
        if (!TelegramUtils.isValidEmail(email)) {
            return MessageFactory.createInvalidEmailMessage(message.getChatId().toString(),  lang);
        }

        Optional<TelegramChat> optChat = telegramChatRepository.findByChatId(message.getFrom().getId().toString());

        if (optChat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString(), lang);
        }

        TelegramChat chat = optChat.get();

        String username =
            chat.getUser() != null ? chat.getUser().getRecipientName() + " " + chat.getUser().getRecipientSurname()
                : message.getFrom().getUserName();

        notificationService.notifyManagerWithNewGreenOfficeRequestFromTelegramBot(email, username, lang);
        chat.setChatState(ChatState.NORMAL);
        chat.setChatStateUpdatedAt(LocalDateTime.now());
        telegramChatRepository.save(chat);
        return MessageFactory.createGreenOfficeThanksMessage(message.getChatId().toString(), lang);
    }
}
