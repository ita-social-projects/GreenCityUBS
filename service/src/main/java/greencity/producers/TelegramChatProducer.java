package greencity.producers;

import greencity.dto.telegram.TelegramMessageDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.UnreadMessagesDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import io.github.springwolf.bindings.stomp.annotations.StompAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramChatProducer {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a notification about a new message in a specific chat.
     *
     * @param messageDto the DTO of the new message, must not be {@code null}
     * @param chatId     the ID of the chat where the message was received, must not
     *                   be {@code null}
     */
    @AsyncPublisher(
        operation = @AsyncOperation(
            channelName = "/topic/messages/{chatId}",
            description = "Subscription for new messages in chat by id"))
    @StompAsyncOperationBinding
    public void notifyNewMessage(@Payload TelegramMessageDto messageDto, Long chatId) {
        log.debug("Publish to /topic/messages/{}", chatId);
        messagingTemplate.convertAndSend("/topic/messages/" + chatId, messageDto);
    }

    /**
     * Sends a notification about the creation of a new chat.
     *
     * @param chatDto the DTO of the new chat, must not be {@code null}
     */
    @AsyncPublisher(
        operation = @AsyncOperation(
            channelName = "/topic/chats",
            description = "Subscription for new chat created"))
    @StompAsyncOperationBinding
    public void notifyNewChat(ChatDto chatDto) {
        log.debug("Publish to /topic/chats");
        messagingTemplate.convertAndSend("/topic/chats", chatDto);
    }

    /**
     * Sends a notification about the count of unread messages for users.
     * <p>
     * This event is published to clients subscribed to the <b>/topic/unread</b> destination.
     * It can be used to update the unread message counters in the user interface.
     * </p>
     *
     * @param unreadMessagesDto the DTO containing unread message counts for users, must not be {@code null}
     */
    @AsyncPublisher(
            operation = @AsyncOperation(
                    channelName = "/topic/unread",
                    description = "Subscription for unread message count updates"))
    @StompAsyncOperationBinding
    public void notifyUnreadMessages(@Payload UnreadMessagesDto unreadMessagesDto) {
        messagingTemplate.convertAndSend("/topic/unread", unreadMessagesDto);
    }
}
