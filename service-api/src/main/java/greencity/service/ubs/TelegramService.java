package greencity.service.ubs;

import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.AuthorizedUserDto;
import greencity.dto.telegram.TelegramImageDto;
import greencity.dto.telegram.TelegramTextMessageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.UUID;

public interface TelegramService {
    /**
     * Method processes login command.
     *
     * @param message {@link Message}
     *
     * @return {@link SendMessage}
     */
    SendMessage processLoginCommand(Message message);

    /**
     * Method processes support command.
     *
     * @param message {@link Message}
     *
     * @return {@link SendMessage}
     */
    SendMessage processSupportCommand(Message message);

    /**
     * Method processes start command.
     *
     * @param message {@link Message}
     *
     * @return {@link SendMessage}
     */
    /**
     * Method processes start command.
     *
     * @param message {@link Message}
     *
     * @return {@link SendMessage}
     */
    SendMessage processStartCommand(Message message);

    /**
     * Method processes failed login attempt.
     *
     * @param errorMessage {@link String} error message
     *
     * @return {@link SendMessage}
     */
    SendMessage processFailLogin(String errorMessage);

    /**
     * Saves user message.
     *
     * @param chatId  {@link String} chat ID
     * @param message {@link String} message content
     */
    void saveManagerMessage(String chatId, String message);

    /**
     * Saves user message with specified manager status.
     *
     * @param chatId    {@link String} chat ID
     * @param message   {@link String} message content
     * @param isManager {@link boolean} true if message is manager's, false
     *                  otherwise
     */
    void saveManagerMessage(String chatId, String message, boolean isManager);

    /**
     * Checks if user is in support mode.
     *
     * @param chatId {@link String} chat ID
     *
     * @return {@link boolean} true if in support mode, false otherwise
     */
    boolean isUserInSupportMode(String chatId);

    /**
     * Starts support mode for user.
     *
     * @param chatId {@link String} chat ID
     */
    void startSupportMode(String chatId);

    /**
     * Stops support mode for user.
     *
     * @param chatId {@link String} chat ID
     */
    SendMessage stopSupportMode(String chatId);

    /**
     * Checks if user is manager.
     *
     * @param chatId {@link String} chat ID
     *
     * @return {@link boolean} true if user is manager, false otherwise
     */
    boolean isManager(String chatId);

    /**
     * Activates manager mode for the specified user.
     *
     * @param chatId {@link String} chat ID
     */
    void managerMode(String chatId);

    /**
     * Sends message to user.
     *
     * @param chatId  {@link String} chat ID
     * @param message {@link String} message content
     *
     */
    void sendMessageToUser(String chatId, String message);

    /**
     * Retrieves all TelegramUserMessages by chatId.
     *
     * @param chatId the telegram chat ID
     *
     * @return a list of TelegramUserMessages associated with the specified chatId
     */
    PageableDto<TelegramTextMessageDto> findUserMessageByChatId(String chatId, Pageable pageable);

    /**
     * Retrieves all authorized users.
     *
     * @param pageable the page to retrieve
     *
     * @return a list of TelegramBotDto associated with the specified pageable
     */
    PageableDto<AuthorizedUserDto> getAllUsers(Pageable pageable);

    /**
     * Generates the start link for a manager based on the user's UUID.
     *
     * @param userUUID {@link UUID} of the user.
     * @return {@link String} the manager start link.
     */
    String generateManagerStartLink(String userUUID);

    /**
     * Handles user chat scope.
     *
     * @param data   the command data
     * @param chatId the telegram chat ID
     *
     * @return a SendMessage with the response
     */
    SendMessage handleUserChatScope(String data, String chatId);

    /**
     * Stream messages to client for specified chatId.
     *
     * @param chatId  the telegram chat ID
     * @param message the message to stream
     */
    void streamMessages(String chatId, TelegramTextMessageDto message);

    /**
     * Streams a photo message to client for specified chatId.
     *
     * @param chatId  the telegram chat ID
     * @param message the message to stream
     */
    void streamMessages(String chatId, TelegramImageDto message);

    /**
     * Adds an emitter to the map of emitters for the specified chatId.
     *
     * @param emitter the emitter to add
     * @param chatId  the telegram chat ID
     */
    void addEmitter(SseEmitter emitter, String chatId);

    /**
     * Removes an emitter from the map of emitters for the specified chatId.
     *
     * @param emitter the emitter to remove
     */
    void removeEmitter(SseEmitter emitter);

    /**
     * Retrieves all TelegramUserPhotos by chatId.
     *
     * @param chatId the telegram chat ID
     * @param page   the page to retrieve
     *
     * @return a list of TelegramUserPhotos associated with the specified chatId
     */
    PageableDto<TelegramImageDto> findUserPhotosByChatId(String chatId, Pageable page);
}
