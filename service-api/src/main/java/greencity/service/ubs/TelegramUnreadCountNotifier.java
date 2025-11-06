package greencity.service.ubs;

import greencity.enums.MessageViewingStatus;

public interface TelegramUnreadCountNotifier {
    /**
     * Counts messages with {@link MessageViewingStatus#UNREAD} and publishes the
     * total.
     */
    void notifyUnreadCount();
}
