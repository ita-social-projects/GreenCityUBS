package greencity.ubstelegrambot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.enums.MessageViewingStatus;
import greencity.producers.TelegramChatProducer;
import greencity.repository.TelegramMessageRepository;
import greencity.ubstelegrambot.service.TelegramUnreadCountNotifierImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramUnreadCountNotifierTest {
    @Mock
    private TelegramMessageRepository telegramMessageRepository;
    @Mock
    private TelegramChatProducer telegramChatProducer;
    @InjectMocks
    private TelegramUnreadCountNotifierImpl telegramUnreadCountNotifier;

    @Test
    void testNotifyUnreadCount() {
        when(telegramMessageRepository.countByMessageViewingStatus(MessageViewingStatus.UNREAD)).thenReturn(2L);
        doNothing().when(telegramChatProducer).notifyUnreadMessages(any());

        telegramUnreadCountNotifier.notifyUnreadCount();

        verify(telegramChatProducer).notifyUnreadMessages(any());

    }
}
