package greencity.ubstelegrambot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.dto.telegram.MarkMessagesAsReadRequestDto;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import greencity.enums.MessageViewingStatus;
import greencity.repository.TelegramMessageRepository;
import greencity.ubstelegrambot.service.TelegramMessageStatusHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class TelegramMessageStatusHandlerTest {
    @Mock
    private TelegramMessageRepository telegramMessageRepository;
    @InjectMocks
    private TelegramMessageStatusHandler telegramMessageStatusHandler;

    @Test
    void testMarkMessagesAsRead_IdsSpecified_MessagesMarkedAsRead() {
        List<Long> messageIds = List.of(1L, 2L);

        TelegramChat chat1 = TelegramChat.builder()
            .id(10L)
            .unreadMessagesCount(1)
            .build();

        TelegramChat chat2 = TelegramChat.builder()
            .id(20L)
            .unreadMessagesCount(0)
            .build();

        TelegramMessage message1 = TelegramMessage.builder()
            .id(1L)
            .messageViewingStatus(MessageViewingStatus.UNREAD)
            .chat(chat1)
            .build();

        TelegramMessage message2 = TelegramMessage.builder()
            .id(2L)
            .messageViewingStatus(MessageViewingStatus.READ)
            .chat(chat2)
            .build();

        when(telegramMessageRepository.findAllById(messageIds))
            .thenReturn(List.of(message1, message2));

        telegramMessageStatusHandler.markMessagesAsRead(
            MarkMessagesAsReadRequestDto.builder().messagesIds(messageIds).build());

        verify(telegramMessageRepository).findAllById(messageIds);
        verify(telegramMessageRepository).saveAll(List.of(message1, message2));

        assertEquals(MessageViewingStatus.READ, message1.getMessageViewingStatus());
        assertEquals(MessageViewingStatus.READ, message2.getMessageViewingStatus());

        assertEquals(0, chat1.getUnreadMessagesCount());
        assertEquals(0, chat2.getUnreadMessagesCount());
    }
}
