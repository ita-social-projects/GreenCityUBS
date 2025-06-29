package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.TelegramMessage;
import greencity.exceptions.BadRequestException;
import greencity.mapping.telegrammessage.TelegramMessageMapper;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.TelegramMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TelegramMessageServiceImpl implements TelegramMessageService {
    private final TelegramMessageRepository messageRepository;
    private final TelegramMessageMapper mapper;

    /**
     * Retrieves a paginated list of messages in chat via sendAt desc
     *
     * @param chatId   the telegram chat ID
     * @param pageable pagination and sorting information
     * @return a page of {@link TelegramMessageDto} matching the given criteria
     */
    @Override
    public PageableDto<TelegramMessageDto> getMessagesByChatId(String chatId, Pageable pageable) {
        Page<TelegramMessage> telegramMessages = messageRepository.findByChatIdOrderBySendAtDesc(chatId, pageable);
        if(telegramMessages.isEmpty()){
            throw new BadRequestException(String.format(TelegramBotConstants.CHAT_IS_EMPTY, chatId));
        }

        List<TelegramMessageDto> telegramMessageDtos = telegramMessages.stream()
                .map(mapper::map)
                .toList();
        return new PageableDto<>(
                telegramMessageDtos,
                telegramMessages.getTotalElements(),
                telegramMessages.getPageable().getPageNumber(),
                telegramMessages.getTotalPages()
        );
    }
}
