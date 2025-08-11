package greencity.service.ubs;

/**
 * Service for sending various types of notifications related to Telegram chats
 * and messages.
 */
public interface TelegramNotificationService {
    /**
     * Sends a notification to a manager about new messages received from a user.
     *
     * @param username    the username of the sender, must not be {@code null}
     * @param messageText the text of the new message, must not be {@code null}
     * @param innerChatId the internal ID of the chat, must not be {@code null}
     */
    void notifyManagerAboutNewMessagesFromUser(String username, String messageText, Long innerChatId);

    /**
     * Sends a notification to a manager when a user exits support mode.
     *
     * @param username the username of the user who exited support mode, must not be
     *                 {@code null}
     */
    void notifyManagerAboutEndSupportModeFromUser(String username);
}
