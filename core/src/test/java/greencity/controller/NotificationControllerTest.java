package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.configuration.SecurityConfig;
import greencity.dto.notification.NotificationDto;
import greencity.service.ubs.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.List;
import static greencity.ModelUtils.getNotificationDto;
import static greencity.ModelUtils.getUuid;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {
    private static final String notificationLink = "/notifications";

    private MockMvc mockMvc;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    NotificationController notificationController;

    private final Principal principal = getUuid();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getNotification() throws Exception {
        NotificationDto dto = getNotificationDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(List.of(dto));

        mockMvc.perform(get(notificationLink + "/" + 1L + "?lang=ua")
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    }

    @Test
    void getNotificationsForCurrentUser() throws Exception {
        mockMvc.perform(get(notificationLink)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getUnreadenNotificationsTest() throws Exception {
        mockMvc.perform(get(notificationLink + "/quantityUnreadenNotifications")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void viewNotificationTest() throws Exception {
        mockMvc.perform(patch(notificationLink + "/{notificationId}/viewNotification", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void unreadNotificationTest() throws Exception {
        mockMvc.perform(patch(notificationLink + "/{notificationId}/unreadNotification", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void deleteNotificationTest() throws Exception {
        mockMvc.perform(delete(notificationLink + "/{notificationId}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
