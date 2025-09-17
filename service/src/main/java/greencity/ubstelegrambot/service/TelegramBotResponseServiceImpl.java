package greencity.ubstelegrambot.service;

import greencity.constant.ErrorMessage;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.BotResponseDto;
import greencity.dto.telegram.UpdateBotMessageRequestDto;
import greencity.entity.telegram.BotMessage;
import greencity.enums.MessageType;
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
                .lang(message.getLang())
                .text(message.getText())
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

        botMessage.setText(dto.getText());
        telegramBotMessageRepository.save(botMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponseByLangAndMessageType(String lang, MessageType messageType) {
        return telegramBotMessageRepository
                .findByLangAndMessageType(lang, messageType)
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessage.BOT_MESSAGE_WITH_TYPE_AND_LANG_NOT_FOUND, messageType, lang)))
                .getText();
    }
}
