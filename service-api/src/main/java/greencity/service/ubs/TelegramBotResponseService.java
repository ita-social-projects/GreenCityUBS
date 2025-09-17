package greencity.service.ubs;

import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.BotResponseDto;
import greencity.dto.telegram.UpdateBotMessageRequestDto;
import greencity.enums.MessageType;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Telegram bot responses.
 *
 * <p>
 * Provides operations for retrieving and updating bot responses that are used
 * by the Telegram bot.
 * </p>
 */
public interface TelegramBotResponseService {
    /**
     * Retrieves a paginated list of all bot responses.
     *
     * @param pageable pagination and sorting information
     * @return a {@link PageableDto} containing a list of {@link BotResponseDto}
     *         objects
     */
    PageableDto<BotResponseDto> getAllBotResponses(Pageable pageable);

    /**
     * Updates an existing bot response.
     *
     * @param dto the request containing updated bot response data
     */
    void updateBotResponse(UpdateBotMessageRequestDto dto);

    String getResponseByLangAndMessageType(String lang, MessageType messageType);
}
