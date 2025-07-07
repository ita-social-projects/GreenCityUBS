package greencity.service.ubs;

import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramService {
    void sendMessageToUser(CreateTelegramMessageRequest request, MultipartFile[] files);

    ChatDto getChatById(Long chatId);

    /**
     * Retrieves all TelegramUserMessages by chatId.
     *
     * @param chatId the telegram chat ID
     *
     * @return a list of TelegramUserMessages associated with the specified chatId
     */
    PageableDto<TelegramMessageDto> findUserMessageByChatId(Long chatId, Pageable pageable);

    /**
     * Retrieves a paginated list of authorized users, optionally filtered by a
     * search term.
     *
     * @param searchTerm optional keyword to filter users by recipient name or
     *                   surname; if null or empty, all users are returned
     * @param pageable   pagination and sorting information
     * @return a page of {@link AuthorizedUserDto} matching the given criteria
     */
    PageableDto<ChatDto> getChats(String searchTerm, Pageable pageable);

    /**
     * Retrieves the most recent order data for the user identified by the given
     * chat ID.
     *
     * @param chatId the chat identifier of the user
     * @return {@link OrdersDataForUserDto} containing details of the latest order,
     *         or throws an exception / returns null if no order is found (depending
     *         on implementation)
     */
    OrdersDataForUserDto getLastOrderByChatId(Long chatId);

    PageableDto<FeedbackDto> getAllFeedbacks(Pageable pageable);

    PageableDto<FeedbackDto> getAllFeedbacksByChatId(String chatId, Pageable pageable);

    void processUpdate(Update update);
}
