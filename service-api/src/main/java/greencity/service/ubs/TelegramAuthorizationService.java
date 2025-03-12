package greencity.service.ubs;

import greencity.enums.TelegramUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramAuthorizationService {
    /**
     * Method saves TelegramBot(authorized telegram user) and in case if user was
     * saved before as UnknownTelegramUser, method removes from
     * unknown_telegram_user and saves it in telegram_bot table.
     *
     * @param uuId     {@link String} is users uuid;
     * @param tgUserId {@link String is user telegram or chat id};
     *
     * @return {@link TelegramUser} authorized user;
     */
    TelegramUser handleAuthorizedUser(String uuId, String tgUserId);

    /**
     * Method saves UnknownTelegramUser and update in case if user already present
     * but was changed.
     *
     * @param message {@link Message};
     */
    void handleUnknownTelegramUser(Message message);
}
