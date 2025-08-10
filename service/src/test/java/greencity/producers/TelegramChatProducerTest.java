package greencity.producers;

import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.TelegramMessageDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TelegramChatProducerTest {
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

}
