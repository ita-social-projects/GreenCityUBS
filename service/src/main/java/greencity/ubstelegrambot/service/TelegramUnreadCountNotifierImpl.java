package greencity.ubstelegrambot.service;

import greencity.dto.telegram.UnreadMessagesDto;
import greencity.enums.MessageViewingStatus;
import greencity.producers.TelegramChatProducer;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.TelegramUnreadCountNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramUnreadCountNotifierImpl implements TelegramUnreadCountNotifier {
    private final TelegramMessageRepository telegramMessageRepository;
    private final TelegramChatProducer telegramChatProducer;

    @Override
    public void notifyUnreadCount() {
        Long unreadCount = telegramMessageRepository.countByMessageViewingStatus(MessageViewingStatus.UNREAD);

        UnreadMessagesDto unreadMessagesDto = UnreadMessagesDto.builder()
            .unreadMessagesCount(unreadCount)
            .build();

        telegramChatProducer.notifyUnreadMessages(unreadMessagesDto);
    }
}
