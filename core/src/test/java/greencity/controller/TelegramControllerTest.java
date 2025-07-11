package greencity.controller;

import greencity.client.UserRemoteClient;
import greencity.converters.UserArgumentResolver;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.FeedbackDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.service.ubs.TelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TelegramControllerTest {

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private TelegramController telegramChatController;

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
        Mockito.when(telegramService.getAllFeedbacks(any(Pageable.class)))
            .thenReturn(feedbackDtoPage);

        mockMvc.perform(get("/ubs/telegram/feedbacks"))
            .andExpect(status().isOk());
    }

    @Test
    void getAllFeedbacksByChatId_ShouldReturnOk() throws Exception {
        Mockito.when(telegramService.getAllFeedbacksByChatId(eq("123"), any(Pageable.class)))
            .thenReturn(feedbackDtoPage);

        mockMvc.perform(get("/ubs/telegram/feedbacks/123"))
            .andExpect(status().isOk());
    }
}
