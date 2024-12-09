package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.service.notification.NotificationTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static greencity.ModelUtils.getUuid;
import static greencity.enums.NotificationStatus.INACTIVE;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class ManagementNotificationControllerTest {
    private MockMvc mockMvc;
    @InjectMocks
    ManagementNotificationController notificationController;
    @Mock
    NotificationTemplateService notificationTemplateService;
    @Mock
    UserRemoteClient userRemoteClient;

    private static final String ADMIN_NOTIFICATION_LINK = "/admin/notification";
    private static final ErrorAttributes ERROR_ATTRIBUTES = new DefaultErrorAttributes();
    private static final Principal PRINCIPAL = getUuid();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .setControllerAdvice(new CustomExceptionHandler(ERROR_ATTRIBUTES))
            .build();
    }

    @Test
    void getAllTest() throws Exception {
        String responseJSON = OBJECT_MAPPER.writeValueAsString(List.of(ModelUtils.getNotificationTemplateDto()));
        mockMvc.perform(get(ADMIN_NOTIFICATION_LINK + "/get-all-templates")
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void updateNotificationTemplateTest() throws Exception {
        String jsonDto = OBJECT_MAPPER.writeValueAsString(
            ModelUtils.getNotificationTemplateWithPlatformsUpdateDto());
        mockMvc.perform(put(ADMIN_NOTIFICATION_LINK + "/update-template/{id}", 1L)
            .principal(PRINCIPAL)
            .content(jsonDto)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getNotificationTemplateTest() throws Exception {
        String responseJSON = OBJECT_MAPPER.writeValueAsString(ModelUtils.getNotificationTemplateWithPlatformsDto());
        mockMvc.perform(get(ADMIN_NOTIFICATION_LINK + "/get-template/{id}", 1L)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void saveBadRequestTest() throws Exception {
        NotificationTemplateWithPlatformsUpdateDto dto = ModelUtils.getNotificationTemplateWithPlatformsUpdateDto();
        Long id = 1L;
        String jsonDto = OBJECT_MAPPER.writeValueAsString(dto);
        doThrow(NotFoundException.class)
            .when(notificationTemplateService).update(id, dto);
        mockMvc.perform(put(ADMIN_NOTIFICATION_LINK + "/update-template/{id}", id)
            .principal(PRINCIPAL)
            .content(jsonDto)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        verify(notificationTemplateService).update(id, dto);
    }

    @Test
    void deactivateNotificationTemplate() throws Exception {
        Long id = 1L;
        String status = INACTIVE.name();
        mockMvc.perform(put(ADMIN_NOTIFICATION_LINK + "/change-template-status/{id}", id)
            .param("status", status)
            .principal(PRINCIPAL))
            .andExpect(status().isOk());
        verify(notificationTemplateService).changeNotificationStatusById(id, status);
    }

    @Test
    void deactivateNotificationTemplateBadRequestTest() throws Exception {
        Long id = 1L;
        String status = INACTIVE.name();

        doThrow(BadRequestException.class)
            .when(notificationTemplateService).changeNotificationStatusById(id, status);

        mockMvc.perform(put(ADMIN_NOTIFICATION_LINK + "/change-template-status/{id}", id)
            .param("status", status)
            .principal(PRINCIPAL))
            .andExpect(status().isBadRequest());

        verify(notificationTemplateService).changeNotificationStatusById(id, status);
    }

    @Test
    void deactivateNotificationTemplateNotFoundTest() throws Exception {
        Long id = 1L;
        String status = INACTIVE.name();

        doThrow(NotFoundException.class)
            .when(notificationTemplateService).changeNotificationStatusById(id, status);

        mockMvc.perform(MockMvcRequestBuilders.put(ADMIN_NOTIFICATION_LINK + "/change-template-status/{id}", id)
            .param("status", status)
            .principal(PRINCIPAL))
            .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(notificationTemplateService).changeNotificationStatusById(id, status);
    }
}
