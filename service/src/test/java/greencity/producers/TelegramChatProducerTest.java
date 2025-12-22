package greencity.producers;

import static org.mockito.Mockito.verify;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.dto.telegram.UnreadMessagesDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class TelegramChatProducerTest {
    @InjectMocks
    private TelegramChatProducer telegramChatProducer;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void testNotifyNewMessage_CorrectDestination_MessageSent() {
        Long chatId = 123L;
        TelegramMessageDto messageDto = new TelegramMessageDto();

        telegramChatProducer.notifyNewMessage(messageDto, chatId);

        verify(messagingTemplate).convertAndSend("/topic/messages/" + chatId, messageDto);
    }

    @Test
    void testNotifyNewChat_CorrectDestination_MessageSent() {
        ChatDto chatDto = new ChatDto();

        telegramChatProducer.notifyNewChat(chatDto);

        verify(messagingTemplate).convertAndSend("/topic/chats", chatDto);
    }

    @Test
    void testNotifyUnreadMessages_CorrectDestination_MessageSent() {
        UnreadMessagesDto unreadMessagesDto = new UnreadMessagesDto();

        telegramChatProducer.notifyUnreadMessages(unreadMessagesDto);

        verify(messagingTemplate).convertAndSend("/topic/unread", unreadMessagesDto);
    }

}
