package greencity.service.ubs;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramService {
    /**
     * Method saves UnknownTelegramUser and update in case if user already present
     * but was changed.
     *
     * @param update {@link Update};
     */
    void handleUnknownTelegramUser(Update update);

    /**
     * Method saves TelegramBot(authorized telegram user) and in case if user was
     * saved before as UnknownTelegramUser, method removes from unknown_telegram_user
     * and saves it in telegram_bot table.
     *
     * @param uuId     {@link String} is users uuid;
     * @param tgUserId {@link Long is user telegram or chat id};
     */
    void handleAuthorizedUser(String uuId, Long tgUserId);
}
