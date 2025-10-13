package greencity.service.ubs;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramLoginService {
    /**
     * Logs out the manager associated with the given Telegram chat ID. If a manager
     * is found for the specified chat ID, their record is deleted from the
     * repository, effectively logging them out.
     *
     * @param chatId {@link String }the Telegram chat ID
     */
    void logoutManager(String chatId);

    /**
     * Processes the manager login request by validating credentials from the
     * message. Verifies format, checks if the user is an employee and a manager,
     * then attempts authentication. If successful, stores the manager's session and
     * returns a success message; otherwise, sends an error response.
     *
     * @param message {@link Message} the Telegram message containing login
     *                credentials in "email:password" format
     * @return a {@link SendMessage} with the result of the login attempt
     */
    SendMessage processInputManagerCredentialsRequest(Message message, String lang);
}
