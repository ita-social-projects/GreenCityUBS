package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import greencity.ModelUtils;
import greencity.configuration.SecurityConfig;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.UpdateNotificationTemplatesDto;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.NotificationTemplatesService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static greencity.ModelUtils.getNotificationDto;
import static greencity.ModelUtils.getUuid;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {
    private static final String notificationLink = "/notifications";

    private MockMvc mockMvc;

    @Mock
    NotificationService notificationService;
    @Mock
    NotificationTemplatesService notificationTemplatesService;

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

        mockMvc.perform(post(notificationLink + "/" + 1L + "?lang=ua")
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    }

    @Test
    void getNotificationsForCurrentUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(notificationLink)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getUnreadenNotificationsTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(notificationLink + "/quantityUnreadenNotifications")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void updateNotificationTemplateForSITE() throws Exception {
        UpdateNotificationTemplatesDto updateNotificationTemplatesDto = ModelUtils.getUpdateNotificationTemplatesDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(updateNotificationTemplatesDto);

        mockMvc.perform(put(notificationLink + "/updateTemplateForSITE")
            .content(responseJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void updateNotificationTemplateForOTHER() throws Exception {
        UpdateNotificationTemplatesDto updateNotificationTemplatesDto = ModelUtils.getUpdateNotificationTemplatesDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(updateNotificationTemplatesDto);

        mockMvc.perform(put(notificationLink + "/updateTemplateForOTHER")
            .content(responseJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
