package greencity.service.ubs;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramFeedbackService {
    SendMessage processInputCommentRequest(Message message);

    SendMessage processRatingFeedbackRequest(String chatId, int rating);
}
