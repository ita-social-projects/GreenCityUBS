package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.constant.TelegramBotConstants;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.FeedbackDto;
import greencity.dto.telegram.UserTelegramFeedbackDto;
import greencity.entity.telegram.ChatFeedback;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.enums.FeedbackState;
import greencity.enums.MessageType;
import greencity.repository.ChatFeedbackRepository;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.TelegramLanguageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramFeedbackServiceTest {
    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private ChatFeedbackRepository chatFeedbackRepository;

    @InjectMocks
    private TelegramFeedbackServiceImpl telegramFeedbackService;

    @Mock
    private TelegramLanguageService telegramLanguageService;

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private TelegramBotResponseServiceImpl telegramBotResponseService;

    @BeforeEach
    void setUp() {
        lenient().when(telegramLanguageService.getChatLanguage(anyString()))
            .thenReturn(TelegramBotConstants.UK);
    }

    @Test
    void processInputCommentRequest_shouldProcessFeedbackSuccessfullyAndSendEmail() {
        Long chatId = 12345L;
        Long chatDbId = 1L;
        String comment = "This is a detailed feedback comment";
        Integer rating = 4;
        String username = "testUser";

        Message message = new Message();
        message.setChat(new Chat(chatId, "private"));
        message.setText(comment);

        TelegramChat telegramChat = TelegramChat.builder()
            .id(chatDbId)
            .chatId(chatId.toString())
            .username(username)
            .chatState(ChatState.MAKING_FEEDBACK)
            .chatStateUpdatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
            .isNotify(true)
            .build();

        ChatFeedback chatFeedback = new ChatFeedback();
        chatFeedback.setId(10L);
        chatFeedback.setChat(telegramChat);
        chatFeedback.setFeedbackState(FeedbackState.IN_PROGRESS);
        chatFeedback.setRating(rating);

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.FEEDBACK_THANK_YOU_MESSAGE)).thenReturn("text");

        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(telegramChat));
        when(chatFeedbackRepository.findByChatIdAndFeedbackState(chatDbId, FeedbackState.IN_PROGRESS))
            .thenReturn(Optional.of(chatFeedback));

        ArgumentCaptor<UserTelegramFeedbackDto> feedbackDtoCaptor =
            ArgumentCaptor.forClass(UserTelegramFeedbackDto.class);

        SendMessage result = telegramFeedbackService.processInputCommentRequest(message, TelegramBotConstants.UK);

        assertEquals(comment, chatFeedback.getComment());
        assertEquals(FeedbackState.CLOSED, chatFeedback.getFeedbackState());
        assertEquals(ChatState.NORMAL, telegramChat.getChatState());
        assertNotNull(telegramChat.getChatStateUpdatedAt());
        assertTrue(telegramChat.getChatStateUpdatedAt().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)));

        verify(chatFeedbackRepository).save(chatFeedback);
        verify(telegramChatRepository).save(telegramChat);

        assertEquals(chatId.toString(), result.getChatId());
        assertEquals("text", result.getText());

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.FEEDBACK_THANK_YOU_MESSAGE);

        verify(userRemoteClient).sendTelegramFeedback(feedbackDtoCaptor.capture());
        UserTelegramFeedbackDto capturedDto = feedbackDtoCaptor.getValue();
        assertEquals(telegramChat.getChatId(), capturedDto.getChatId());
        assertEquals(rating, capturedDto.getRating());
        assertEquals(comment, capturedDto.getComment());
        assertEquals(username, capturedDto.getName());
        assertEquals("Новий відгук з Telegram", capturedDto.getSubject());
    }

    @Test
    void processInputCommentRequest_shouldReturnUnknownErrorMessage_whenChatNotFound() {
        Long chatId = 12345L;
        Message message = new Message();
        message.setChat(new Chat(chatId, "private"));
        message.setText("Some comment");

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_ERROR)).thenReturn("text");

        when(telegramChatRepository.findByChatId(chatId.toString())).thenReturn(Optional.empty());

        // when
        SendMessage result = telegramFeedbackService.processInputCommentRequest(message, TelegramBotConstants.UK);

        // then
        assertEquals(chatId.toString(), result.getChatId());
        assertEquals("text", result.getText());

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_ERROR);
    }

    @Test
    void processInputCommentRequest_shouldReturnUnknownErrorMessage_whenFeedbackNotFound() {
        // given
        Long chatId = 12345L;
        Long chatDbId = 1L;
        Message message = new Message();
        message.setChat(new Chat(chatId, "private"));
        message.setText("user comment");

        TelegramChat telegramChat = TelegramChat.builder()
            .id(chatDbId)
            .chatId(chatId.toString())
            .chatState(ChatState.NORMAL)
            .chatStateUpdatedAt(Instant.now())
            .isNotify(true)
            .build();

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_ERROR)).thenReturn("text");

        when(telegramChatRepository.findByChatId(chatId.toString())).thenReturn(Optional.of(telegramChat));
        when(chatFeedbackRepository.findByChatIdAndFeedbackState(chatDbId, FeedbackState.IN_PROGRESS))
            .thenReturn(Optional.empty());

        // when
        SendMessage result = telegramFeedbackService.processInputCommentRequest(message, TelegramBotConstants.UK);

        // then
        assertEquals(chatId.toString(), result.getChatId());
        assertEquals("text", result.getText());

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_ERROR);
    }

    @Test
    void processInputCommentRequest_shouldHandleNullCommentText() {
        // given
        Long chatId = 12345L;
        Long chatDbId = 1L;

        Message message = new Message();
        message.setChat(new Chat(chatId, "private"));
        message.setText(null);

        TelegramChat telegramChat = TelegramChat.builder()
            .id(chatDbId)
            .chatId(chatId.toString())
            .chatState(ChatState.MAKING_FEEDBACK)
            .chatStateUpdatedAt(Instant.now())
            .isNotify(true)
            .build();

        ChatFeedback chatFeedback = new ChatFeedback();
        chatFeedback.setChat(telegramChat);
        chatFeedback.setFeedbackState(FeedbackState.IN_PROGRESS);

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.FEEDBACK_THANK_YOU_MESSAGE)).thenReturn("text");

        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(telegramChat));
        when(chatFeedbackRepository.findByChatIdAndFeedbackState(chatDbId, FeedbackState.IN_PROGRESS))
            .thenReturn(Optional.of(chatFeedback));

        // when
        SendMessage result = telegramFeedbackService.processInputCommentRequest(message, TelegramBotConstants.UK);

        // then
        assertEquals("text", result.getText());
        assertNull(chatFeedback.getComment());

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.FEEDBACK_THANK_YOU_MESSAGE);
    }

    @Test
    void processInputCommentRequest_shouldThrowException_whenFeedbackSaveFails() {
        // given
        Long chatId = 12345L;
        Long chatDbId = 1L;

        Message message = new Message();
        message.setChat(new Chat(chatId, "private"));
        message.setText("test");

        TelegramChat telegramChat = TelegramChat.builder()
            .id(chatDbId)
            .chatId(chatId.toString())
            .chatState(ChatState.MAKING_FEEDBACK)
            .chatStateUpdatedAt(Instant.now())
            .isNotify(true)
            .build();

        ChatFeedback chatFeedback = new ChatFeedback();
        chatFeedback.setChat(telegramChat);
        chatFeedback.setFeedbackState(FeedbackState.IN_PROGRESS);

        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(telegramChat));
        when(chatFeedbackRepository.findByChatIdAndFeedbackState(chatDbId, FeedbackState.IN_PROGRESS))
            .thenReturn(Optional.of(chatFeedback));
        doThrow(new RuntimeException("DB error")).when(chatFeedbackRepository).save(any());

        // when/then
        assertThrows(RuntimeException.class,
            () -> telegramFeedbackService.processInputCommentRequest(message, TelegramBotConstants.UK));
    }

    @Test
    void processRatingFeedbackRequest_shouldReturnErrorMessage_whenChatNotFound() {
        // given
        String chatId = "12345";

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_ERROR)).thenReturn("text");

        when(telegramChatRepository.findByChatId(chatId))
            .thenReturn(Optional.empty());

        // when
        SendMessage result = telegramFeedbackService.processRatingFeedbackRequest(chatId, 5);

        // then
        assertEquals(chatId, result.getChatId());
        assertEquals("text", result.getText());

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_ERROR);
    }

    @Test
    void processRatingFeedbackRequest_shouldCloseExistingFeedback_ifPresent() {
        // given
        String chatId = "12345";
        Long chatDbId = 1L;
        int rating = 3;

        TelegramChat chat = TelegramChat.builder()
            .id(chatDbId)
            .chatId(chatId)
            .chatState(ChatState.NORMAL)
            .chatStateUpdatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
            .isNotify(true)
            .build();

        ChatFeedback existingFeedback = new ChatFeedback();
        existingFeedback.setId(10L);
        existingFeedback.setFeedbackState(FeedbackState.IN_PROGRESS);
        existingFeedback.setChat(chat);

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.BAD_FEEDBACK_MESSAGE)).thenReturn("text");

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(chatFeedbackRepository.findByChatIdAndFeedbackState(chatDbId, FeedbackState.IN_PROGRESS))
            .thenReturn(Optional.of(existingFeedback));

        // when
        SendMessage result = telegramFeedbackService.processRatingFeedbackRequest(chatId, rating);

        // then
        assertEquals("text", result.getText());
        assertEquals(FeedbackState.CLOSED, existingFeedback.getFeedbackState());

        verify(chatFeedbackRepository).save(existingFeedback);
        verify(chatFeedbackRepository)
            .save(argThat(fb -> fb.getRating() == rating && fb.getFeedbackState() == FeedbackState.IN_PROGRESS));
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.BAD_FEEDBACK_MESSAGE);
    }

    @Test
    void processRatingFeedbackRequest_shouldCreateNewFeedback_whenNoExistingFeedback() {
        // given
        String chatId = "12345";
        Long chatDbId = 1L;
        int rating = 5;

        TelegramChat chat = TelegramChat.builder()
            .id(chatDbId)
            .chatId(chatId)
            .chatState(ChatState.NORMAL)
            .chatStateUpdatedAt(Instant.now())
            .isNotify(true)
            .build();

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.GREAT_FEEDBACK_MESSAGE)).thenReturn("text");

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(chatFeedbackRepository.findByChatIdAndFeedbackState(chatDbId, FeedbackState.IN_PROGRESS))
            .thenReturn(Optional.empty());

        // when
        SendMessage result = telegramFeedbackService.processRatingFeedbackRequest(chatId, rating);

        // then
        assertEquals("text", result.getText());

        verify(chatFeedbackRepository).save(argThat(fb -> fb.getRating() == rating &&
            fb.getFeedbackState() == FeedbackState.IN_PROGRESS &&
            fb.getChat() == chat));
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.GREAT_FEEDBACK_MESSAGE);
    }

    @Test
    void processRatingFeedbackRequest_shouldUpdateChatStateTo_MAKING_FEEDBACK() {
        // given
        String chatId = "12345";
        Long chatDbId = 1L;

        TelegramChat chat = TelegramChat.builder()
            .id(chatDbId)
            .chatId(chatId)
            .chatState(ChatState.NORMAL)
            .chatStateUpdatedAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .isNotify(true)
            .build();

        when(telegramBotResponseService.getResponseByLangAndMessageType(anyString(), any())).thenReturn("text");

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(chatFeedbackRepository.findByChatIdAndFeedbackState(chatDbId, FeedbackState.IN_PROGRESS))
            .thenReturn(Optional.empty());

        // when
        telegramFeedbackService.processRatingFeedbackRequest(chatId, 4);

        // then
        assertEquals(ChatState.MAKING_FEEDBACK, chat.getChatState());
        assertTrue(chat.getChatStateUpdatedAt().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)));
        verify(telegramChatRepository).save(chat);
    }

    @Test
    void testGetAllFeedbacksByChatId_FeedbacksFound_FeedbackDtoReturned() {
        Long id = 1L;
        String chatId = "123456789";
        Pageable pageable = PageRequest.of(0, 5);

        TelegramChat chat = TelegramChat.builder()
            .chatId(chatId)
            .build();

        ChatFeedback feedback = ChatFeedback.builder()
            .id(1L)
            .chat(chat)
            .rating(5)
            .comment("Great service")
            .build();

        Page<ChatFeedback> feedbackPage = new PageImpl<>(List.of(feedback), pageable, 1);

        when(chatFeedbackRepository.findByChatId(id, pageable)).thenReturn(feedbackPage);

        PageableDto<FeedbackDto> result = telegramFeedbackService.getAllFeedbacksByChatId(id, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getPage().size());

        FeedbackDto dto = result.getPage().getFirst();
        assertEquals(Optional.of(1L).get(), dto.id());
        assertEquals("123456789", dto.chatId());
        assertEquals(5, dto.rating());
        assertEquals("Great service", dto.comment());
    }

    @Test
    void testGetAllFeedbacksByChatId_ChatNotFound_EmptyPageableDtoReturned() {
        Long id = 1L;
        Pageable pageable = PageRequest.of(0, 5);
        when(chatFeedbackRepository.findByChatId(id, pageable)).thenReturn(Page.empty());

        PageableDto<FeedbackDto> result = telegramFeedbackService.getAllFeedbacksByChatId(id, pageable);

        Assertions.assertTrue(result.getPage().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getAllFeedbacks_shouldReturnMappedDtos_whenFeedbacksExist() {
        // arrange
        Pageable pageable = PageRequest.of(0, 10);

        TelegramChat chat = TelegramChat.builder()
            .id(100L)
            .build();

        ChatFeedback feedback = ChatFeedback.builder()
            .id(1L)
            .chat(chat)
            .rating(5)
            .comment("Great job")
            .build();

        Page<ChatFeedback> page = new PageImpl<>(List.of(feedback), pageable, 1);

        when(chatFeedbackRepository.findAll(pageable)).thenReturn(page);

        // act
        PageableDto<FeedbackDto> result = telegramFeedbackService.getAllFeedbacks(pageable);

        // assert
        assertEquals(1, result.getPage().size());

        FeedbackDto dto = result.getPage().getFirst();
        assertEquals(1L, dto.id());
        assertEquals("100", dto.chatId()); // .toString()
        assertEquals(5, dto.rating());
        assertEquals("Great job", dto.comment());

        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getAllFeedbacks_shouldReturnEmptyList_whenNoFeedbacksExist() {
        // arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatFeedback> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(chatFeedbackRepository.findAll(pageable)).thenReturn(emptyPage);

        // act
        PageableDto<FeedbackDto> result = telegramFeedbackService.getAllFeedbacks(pageable);

        // assert
        assertTrue(result.getPage().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

}
