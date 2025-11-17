package greencity.ubstelegrambot.service;

import greencity.dto.telegram.MarkMessagesAsReadRequestDto;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import greencity.enums.MessageViewingStatus;
import greencity.repository.TelegramMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramMessageStatusHandler {
    private final TelegramMessageRepository telegramMessageRepository;

    @Transactional
    public void markMessagesAsRead(MarkMessagesAsReadRequestDto request) {
        List<TelegramMessage> messages = telegramMessageRepository.findAllById(request.getMessagesIds());

        for (TelegramMessage message : messages) {
            if (message.getMessageViewingStatus() == MessageViewingStatus.UNREAD) {
                message.setMessageViewingStatus(MessageViewingStatus.READ);

                TelegramChat chat = message.getChat();
                int currentUnread = chat.getUnreadMessagesCount();
                if (currentUnread > 0) {
                    chat.setUnreadMessagesCount(currentUnread - 1);
                }
            }
        }

        telegramMessageRepository.saveAll(messages);
    }
}
