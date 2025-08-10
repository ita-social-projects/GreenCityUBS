package greencity.service.ubs;

import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.FeedbackDto;
import org.springframework.data.domain.Pageable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramFeedbackService {
    /**
     * Processes a user's comment message during an active feedback session in a
     * Telegram chat.
     *
     * @param message the Telegram {@link Message} containing the user's comment
     * @return a {@link SendMessage} response indicating the result: a thank-you
     *         message
     */
    SendMessage processInputCommentRequest(Message message,  String lang);

    /**
     * Processes the user's rating for a feedback session in a Telegram chat.
     *
     * @param chatId {@link String} the Telegram chat ID
     * @param rating {@link Integer}the integer rating provided by the user (e.g.
     *               1–5)
     * @return a {@link SendMessage} response indicating successful feedback
     *         submission
     */
    SendMessage processRatingFeedbackRequest(String chatId, int rating);

    /**
     * Retrieves a pageable list of all feedback entries.
     *
     * @param pageable {@link Pageable} the pagination information
     * @return a pageable DTO containing feedback data
     */
    PageableDto<FeedbackDto> getAllFeedbacks(Pageable pageable);

    /**
     * Retrieves a pageable list of feedback entries filtered by chat ID.
     *
     * @param chatId   {@link String} the unique identifier of the chat as a String
     * @param pageable {@link Pageable} the pagination information
     * @return a pageable DTO containing feedback data for the specified chat
     */
    PageableDto<FeedbackDto> getAllFeedbacksByChatId(String chatId, Pageable pageable);
}
