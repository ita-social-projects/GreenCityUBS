package greencity.service.ubs;

import org.springframework.http.ResponseEntity;

public interface ViberService {
    /**
     * The method sets the URL of the Viber bot to which Viber requests will be
     * sent.
     *
     * @return {@link String} - which contains the status of success or failure.
     */
    ResponseEntity<String> setWebhook();

    /**
     * The method removes Viber bot url.
     *
     * @return {@link String} - which contains the status of success or failure.
     */
    ResponseEntity<String> removeWebHook();

    /**
     * The method allows to see info about Viber bot.
     *
     * @return @return {@link String} - which contains the status of success or
     *         failure.
     */
    ResponseEntity<String> getAccountInfo();

    /**
     * The method sends a welcome message to user and is performed pre-registration
     * of user.
     *
     * @param receiverId - indicates which user to send the greeting message.
     * @param context    - contains uuid user.
     */
    void sendWelcomeMessageAndPreRegisterViberBotForUser(String receiverId, String context);

    /**
     * The method sends a message to user and is performed registration of user.
     *
     * @param receiverId - indicates which user to send the greeting message.
     */
    void sendMessageAndRegisterViberBotForUser(String receiverId);
}
