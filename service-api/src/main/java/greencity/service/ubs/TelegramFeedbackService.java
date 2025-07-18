package greencity.service.ubs;

import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.FeedbackDto;
import org.springframework.data.domain.Pageable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramFeedbackService {
    SendMessage processInputCommentRequest(Message message);

    SendMessage processRatingFeedbackRequest(String chatId, int rating);

    /**
     * Retrieves a pageable list of all feedback entries.
     *
     * @param pageable the pagination information
     * @return a pageable DTO containing feedback data
     */
    PageableDto<FeedbackDto> getAllFeedbacks(Pageable pageable);

    /**
     * Retrieves a pageable list of feedback entries filtered by chat ID.
     *
     * @param chatId   the unique identifier of the chat as a String
     * @param pageable the pagination information
     * @return a pageable DTO containing feedback data for the specified chat
     */
    PageableDto<FeedbackDto> getAllFeedbacksByChatId(String chatId, Pageable pageable);
}
