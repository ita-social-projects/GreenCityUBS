package greencity.service.ubs;

import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
     */
    void processStartCommand(Message message);

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
     * @param message {@link Message} chat ID
     */
    SendMessage stopSupportMode(Message message);

    /**
     * Activates manager mode for the specified user.
     *
     * @param chatId {@link String} chat ID
     */
    void managerMode(String chatId);

    void sendMessageToUser(CreateTelegramMessageRequest request, MultipartFile[] files);

    /**
     * Retrieves all TelegramUserMessages by chatId.
     *
     * @param chatId the telegram chat ID
     *
     * @return a list of TelegramUserMessages associated with the specified chatId
     */
    PageableDto<TelegramMessageDto> findUserMessageByChatId(Long chatId, Pageable pageable);

    /**
     * Retrieves a paginated list of authorized users, optionally filtered by a search term.
     *
     * @param searchTerm optional keyword to filter users by recipient name or surname;
     *                   if null or empty, all users are returned
     * @param pageable   pagination and sorting information
     * @return a page of {@link AuthorizedUserDto} matching the given criteria
     */
    PageableDto<ChatDto> getChats(String searchTerm, Pageable pageable);


    /**
     * Retrieves the most recent order data for the user identified by the given chat ID.
     *
     * @param chatId the chat identifier of the user
     * @return {@link OrdersDataForUserDto} containing details of the latest order,
     *         or throws an exception / returns null if no order is found (depending on implementation)
     */
    OrdersDataForUserDto getLastOrderByChatId(String chatId);

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
    SendMessage handleUserChatScope(String data, String chatId, Integer messageId);

//    /**
//     * Retrieves all TelegramUserPhotos by chatId.
//     *
//     * @param chatId the telegram chat ID
//     * @param page   the page to retrieve
//     *
//     * @return a list of TelegramUserPhotos associated with the specified chatId
//     */
//    PageableDto<TelegramImageDto> findUserPhotosByChatId(String chatId, Pageable page);

    PageableDto<FeedbackDto> getAllFeedbacks(Pageable pageable);

    PageableDto<FeedbackDto> getAllFeedbacksByChatId(String chatId, Pageable pageable);

    void processUpdate(Update update);
}
