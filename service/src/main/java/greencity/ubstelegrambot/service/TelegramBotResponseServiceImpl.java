package greencity.ubstelegrambot.service;

import greencity.constant.ErrorMessage;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.BotResponseDto;
import greencity.dto.telegram.UpdateBotMessageRequestDto;
import greencity.entity.telegram.BotMessage;
import greencity.exceptions.NotFoundException;
import greencity.repository.TelegramBotMessageRepository;
import greencity.service.ubs.TelegramBotResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramBotResponseServiceImpl implements TelegramBotResponseService {
    private final TelegramBotMessageRepository telegramBotMessageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<BotResponseDto> getAllBotResponses(Pageable pageable) {
        Page<BotMessage> botMessages = telegramBotMessageRepository.findAll(pageable);
        List<BotResponseDto> botResponseDtos = botMessages
            .getContent()
            .stream()
            .map(message -> BotResponseDto
                .builder()
                .id(message.getId())
                .messageEn(message.getMessageEn())
                .messageUk(message.getMessageUk())
                .messageType(message.getMessageType())
                .build())
            .toList();

        return new PageableDto<>(
            botResponseDtos,
            botMessages.getTotalElements(),
            botMessages.getNumber(),
            botMessages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBotResponse(UpdateBotMessageRequestDto dto) {
        BotMessage botMessage = telegramBotMessageRepository
            .findById(dto.getId())
            .orElseThrow(() -> new NotFoundException(String.format(ErrorMessage.BOT_RESPONSE_NOT_FOUND, dto.getId())));

        botMessage.setMessageEn(dto.getMessageEn());
        botMessage.setMessageUk(dto.getMessageUk());
        telegramBotMessageRepository.save(botMessage);
    }
}
