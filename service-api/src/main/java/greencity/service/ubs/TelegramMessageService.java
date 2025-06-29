package greencity.service.ubs;

import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.TelegramMessageDto;
import org.springframework.data.domain.Pageable;


public interface TelegramMessageService {
    /**
     * Retrieves a paginated list of messages in chat via sendAt desc
     *
     * @param chatId    the telegram chat ID
     * @param pageable   pagination and sorting information
     *
     * @return a page of {@link TelegramMessageDto} matching the given criteria
     */
    PageableDto<TelegramMessageDto> getMessagesByChatId(String chatId, Pageable pageable);
}
