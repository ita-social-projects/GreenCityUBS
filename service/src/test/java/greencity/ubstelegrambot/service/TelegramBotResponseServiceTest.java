package greencity.ubstelegrambot.service;

import greencity.dto.telegram.BotResponse;
import greencity.entity.telegram.BotResponseProjection;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.UpdateBotMessageRequestDto;
import greencity.entity.telegram.BotMessage;
import greencity.enums.MessageType;
import greencity.exceptions.NotFoundException;
import greencity.repository.TelegramBotMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotResponseServiceTest {
    @InjectMocks
    private TelegramBotResponseServiceImpl telegramBotResponseServiceImpl;

    @Mock
    private TelegramBotMessageRepository telegramBotMessageRepository;

    @Test
    void testGetAllBotResponses_WithDataInRepository_ShouldReturnPageableDto() {
        Pageable pageable = PageRequest.of(0, 2);

        BotResponseProjection response1 = mock(BotResponseProjection.class);
        BotResponseProjection response2 = mock(BotResponseProjection.class);

        Page<BotResponseProjection> page = new PageImpl<>(
            List.of(response1, response2),
            pageable,
            2);

        when(telegramBotMessageRepository.findAllWithLang(pageable)).thenReturn(page);

        PageableDto<BotResponse> result = telegramBotResponseServiceImpl.getAllBotResponses(pageable);

        assertThat(result.getPage()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getCurrentPage()).isZero();
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void testGetAllBotResponses_WithEmptyRepository_ShouldReturnEmptyPageableDto() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<BotResponseProjection> emptyPage = Page.empty(pageable);

        when(telegramBotMessageRepository.findAllWithLang(pageable)).thenReturn(emptyPage);

        PageableDto<BotResponse> result = telegramBotResponseServiceImpl.getAllBotResponses(pageable);

        assertThat(result.getPage()).isEmpty();
        assertThat(result.getCurrentPage()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    void testUpdateBotResponse_WithExistingBotMessage_ShouldUpdateText() {
        UpdateBotMessageRequestDto dto = new UpdateBotMessageRequestDto();
        dto.setId(1L);
        dto.setText("Updated text");

        BotMessage message = new BotMessage();
        message.setId(1L);
        message.setText("Old text");

        when(telegramBotMessageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(telegramBotMessageRepository.save(any(BotMessage.class))).thenAnswer(i -> i.getArgument(0));

        telegramBotResponseServiceImpl.updateBotResponse(dto);

        assertThat(message.getText()).isEqualTo("Updated text");
        verify(telegramBotMessageRepository, times(1)).save(message);
    }

    @Test
    void testUpdateBotResponse_WithNonExistingBotMessage_ShouldThrowNotFoundException() {
        UpdateBotMessageRequestDto dto = new UpdateBotMessageRequestDto();
        dto.setId(1L);
        dto.setText("Text");

        when(telegramBotMessageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> telegramBotResponseServiceImpl.updateBotResponse(dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Bot response with ID 1 not found");

        verify(telegramBotMessageRepository, never()).save(any());
    }

    @Test
    void testGetResponseByLangAndMessageType_WithExistingMessage_ShouldReturnText() {
        String lang = "en";
        MessageType messageType = MessageType.LOGIN_MESSAGE;

        BotMessage botMessage = new BotMessage();
        botMessage.setText("Enter your login and password in the format login:password");

        when(telegramBotMessageRepository.findByLangAndMessageType(lang, messageType))
            .thenReturn(Optional.of(botMessage));

        String result = telegramBotResponseServiceImpl.getResponseByLangAndMessageType(lang, messageType);

        assertThat(result).isEqualTo("Enter your login and password in the format login:password");
        verify(telegramBotMessageRepository, times(1))
            .findByLangAndMessageType(lang, messageType);
    }

    @Test
    void testGetResponseByLangAndMessageType_WithNonExistingMessage_ShouldThrowNotFoundException() {
        String lang = "en";
        MessageType messageType = MessageType.LOGIN_MESSAGE;

        when(telegramBotMessageRepository.findByLangAndMessageType(lang, messageType))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> telegramBotResponseServiceImpl.getResponseByLangAndMessageType(lang, messageType))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Bot message with LOGIN_MESSAGE type and en language not found");

        verify(telegramBotMessageRepository, times(1))
            .findByLangAndMessageType(lang, messageType);
    }
}
