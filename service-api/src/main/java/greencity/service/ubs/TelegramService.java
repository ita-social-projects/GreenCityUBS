package greencity.service.ubs;

import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.CreateTelegramMessageRequest;
import greencity.dto.telegram.EditTelegramMessageRequest;
import greencity.dto.telegram.MarkMessagesAsReadRequestDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.dto.telegram.ToggleNotificationsRequestDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Service interface for handling Telegram bot operations, including sending
 * messages, retrieving chats and messages, processing updates from Telegram,
 * and managing feedback.
 */
public interface TelegramService {
    /**
     * Sends a message to a user with optional attached files.
     *
     * @param request the DTO containing message details to be sent
     * @param files   an array of files to be attached to the message
     */
    void sendMessageToUser(CreateTelegramMessageRequest request, MultipartFile[] files);

    /**
     * Retrieves chat details by its unique identifier.
     *
     * @param chatId the unique identifier of the chat
     * @return the chat DTO containing chat information
     */
    ChatDto getChatById(Long chatId);

    /**
     * Reads multiple messages based on the specified request. The method performs
     * the reading of several messages according to the parameters provided in the
     * {@link MarkMessagesAsReadRequestDto} object. Since this method returns
     * {@code void}, all results are handled internally (e.g., updating the database
     * or triggering events).
     *
     * @param request the request containing the criteria for reading messages, must
     *                not be {@code null}
     */
    void markMessagesAsRead(MarkMessagesAsReadRequestDto request);

    /**
     * Retrieves a pageable list of user messages associated with a specific chat
     * ID.
     *
     * @param chatId   the unique identifier of the chat
     * @param pageable the pagination information
     * @return a pageable DTO containing Telegram messages for the user
     */
    PageableDto<TelegramMessageDto> findUserMessageByChatId(Long chatId, Pageable pageable);

    /**
     * Retrieves a pageable list of chats optionally filtered by a search term.
     *
     * @param searchTerm the term to filter chats by (e.g., username or chat name)
     * @param pageable   the pagination information
     * @return a pageable DTO containing chat data matching the search criteria
     */
    PageableDto<ChatDto> getChats(String searchTerm, Pageable pageable);

    /**
     * Retrieves the last order data associated with a given chat ID.
     *
     * @param chatId the unique identifier of the chat
     * @return the DTO containing the last order information for the user
     */
    OrdersDataForUserDto getLastOrderByChatId(Long chatId);

    /**
     * Processes an incoming update from Telegram (e.g., message, callback query).
     *
     * @param update the Telegram update object to process
     */
    void processUpdate(Update update);

    /**
     * Toggles the notification setting for a user in the Telegram bot. Depending on
     * the provided {@link ToggleNotificationsRequestDto}, this method enables or
     * disables whether the user with the specified UUID will receive notifications.
     *
     * @param uuid    the unique identifier of the user whose notification setting
     *                should be changed
     * @param request the DTO containing the desired notification state
     *                (enabled/disabled)
     */
    void toggleNotifications(String uuid, ToggleNotificationsRequestDto request);

    /**
     * Checks whether notifications are enabled for a user in the Telegram bot.
     *
     * @param uuid the unique identifier of the user
     * @return {@code true} if the user has notifications enabled, {@code false}
     *         otherwise
     */
    boolean getIsNotificationsEnabled(String uuid);

    /**
     * Edit manager text message.
     *
     * @param request {@link EditTelegramMessageRequest} the DTO containing the chat
     *                ID, message ID and new text.
     */
    void editManagerMessage(EditTelegramMessageRequest request);
}
