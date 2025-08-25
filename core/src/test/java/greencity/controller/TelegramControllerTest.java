package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.UserRemoteClient;
import greencity.converters.UserArgumentResolver;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.CreateTelegramMessageRequest;
import greencity.dto.telegram.DeleteTelegramMessageRequest;
import greencity.dto.telegram.EditTelegramMessageRequest;
import greencity.dto.telegram.FeedbackDto;
import greencity.dto.telegram.MarkMessagesAsReadRequest;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.service.ubs.TelegramService;
import greencity.ubstelegrambot.service.TelegramFeedbackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TelegramControllerTest {

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private TelegramController telegramChatController;

    @Mock
    private TelegramFeedbackServiceImpl telegramFeedbackService;

    private PageableDto<TelegramMessageDto> messageDtoPage;
    private PageableDto<ChatDto> chatDtoPage;
    private OrdersDataForUserDto orderDto;
    private PageableDto<FeedbackDto> feedbackDtoPage;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(telegramChatController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .build();

        messageDtoPage = new PageableDto<>(Collections.emptyList(), 0, 0, 0);
        chatDtoPage = new PageableDto<>(Collections.emptyList(), 0, 0, 0);
        orderDto = new OrdersDataForUserDto();
        feedbackDtoPage = new PageableDto<>(Collections.emptyList(), 0, 0, 0);
    }

    @Test
    void getUserMessages_ShouldReturnOk() throws Exception {
        Mockito.when(telegramService.findUserMessageByChatId(eq(1L), any(Pageable.class)))
            .thenReturn(messageDtoPage);

        mockMvc.perform(get("/ubs/telegram/messages/1"))
            .andExpect(status().isOk());
    }

    @Test
    void getChats_ShouldReturnOk() throws Exception {
        Mockito.when(telegramService.getChats(eq("test"), any(Pageable.class)))
            .thenReturn(chatDtoPage);

        mockMvc.perform(get("/ubs/telegram/chats")
            .param("search", "test"))
            .andExpect(status().isOk());
    }

    @Test
    void getLastOrderByChatId_ShouldReturnOk() throws Exception {
        Mockito.when(telegramService.getLastOrderByChatId(1L))
            .thenReturn(orderDto);

        mockMvc.perform(get("/ubs/telegram/last-order")
            .param("chatId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    void getChat_ShouldReturnOk() throws Exception {
        ChatDto chatDto = new ChatDto();
        Mockito.when(telegramService.getChatById(1L)).thenReturn(chatDto);

        mockMvc.perform(get("/ubs/telegram/chat/1"))
            .andExpect(status().isOk());
    }

    @Test
    void getAllFeedbacks_ShouldReturnOk() throws Exception {
        Mockito.when(telegramFeedbackService.getAllFeedbacks(any(Pageable.class)))
            .thenReturn(feedbackDtoPage);

        mockMvc.perform(get("/ubs/telegram/feedbacks"))
            .andExpect(status().isOk());
    }

    @Test
    void getAllFeedbacksByChatId_ShouldReturnOk() throws Exception {
        Mockito.when(telegramFeedbackService.getAllFeedbacksByChatId(eq(123L), any(Pageable.class)))
            .thenReturn(feedbackDtoPage);

        mockMvc.perform(get("/ubs/telegram/feedbacks/123"))
            .andExpect(status().isOk());
    }

    @Test
    void markMessagesAsRead_ShouldReturnOk() throws Exception {
        MarkMessagesAsReadRequest request = MarkMessagesAsReadRequest
            .builder()
            .messagesIds(List.of(1L, 2L, 3L))
            .build();

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(put("/ubs/telegram/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(telegramService).markMessagesAsRead(request);
    }

    @Test
    void editMessage_ShouldReturnNoContent() throws Exception {
        EditTelegramMessageRequest request = new EditTelegramMessageRequest(1L, 2L, "new text");

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(put("/ubs/telegram/message/edit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(telegramService).editManagerMessage(request);
    }

    @Test
    void deleteMessage_ShouldReturnNoContent() throws Exception {
        DeleteTelegramMessageRequest request = new DeleteTelegramMessageRequest(1L, 123L, null);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(delete("/ubs/telegram/message/delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(telegramService).deleteManagerMessage(request);
    }

    @Test
    void sendMessage_ShouldReturnOk() throws Exception {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest(123L, "hello");

        ObjectMapper objectMapper = new ObjectMapper();

        MockMultipartFile jsonPart = new MockMultipartFile(
            "data", "", "application/json", objectMapper.writeValueAsBytes(request));

        MockMultipartFile filePart = new MockMultipartFile(
            "files", "test.png", "image/png", "fake image".getBytes());

        mockMvc.perform(multipart("/ubs/telegram/messages")
            .file(jsonPart)
            .file(filePart)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk());

        verify(telegramService).sendMessageToUser(eq(request), any(MultipartFile[].class));
    }

}
