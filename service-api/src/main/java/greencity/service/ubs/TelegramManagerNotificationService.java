package greencity.service.ubs;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public interface TelegramManagerNotificationService {
    /**
     * Checks if manager should be notified about new user message.
     *
     * @param chatId {@link String} chat ID
     */
    void shouldNotifyManager(String chatId);

    /**
     * Checks if there are any pending messages. If there are, sends a notification
     * to managers.
     */
    void checkPendingMessages();

    /**
     * Sends a notification to all managers about new user message.
     *
     * @param telegramLongPollingBot {@link TelegramLongPollingBot} telegram bot
     *                               instance
     * @param chatId                 {@link String} chat ID of the user who sent the
     *                               message
     */
    void sendSupportNotificationMessageToManagers(TelegramLongPollingBot telegramLongPollingBot, String chatId,
        int messageCount);

    /**
     * Sends a notification to all managers about the end of support mode for the
     * specified user.
     *
     * @param chatId {@link String} chat ID
     *
     */
    void notifyManagerAboutEndSupportModeFromUser(String chatId);
}
