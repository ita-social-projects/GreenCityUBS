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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class ManagementNotificationControllerTest {
    private static final String url = "/admin/notification";
    private MockMvc mockMvc;
    @InjectMocks
    ManagementNotificationController notificationController;
    @Mock
    NotificationTemplateService notificationTemplateService;
    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();
    private final Principal principal = getUuid();
    @Mock
    UserRemoteClient userRemoteClient;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes))
            .build();
    }

    @Test
    void getAllTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(List.of(ModelUtils.getNotificationTemplateDto()));
        mockMvc.perform(get(url + "/get-all-templates")
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void updateNotificationTemplateTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonDto = objectMapper.writeValueAsString(
            ModelUtils.getNotificationTemplateWithPlatformsUpdateDto());
        mockMvc.perform(put(url + "/update-template/{id}", 1L)
            .principal(principal)
            .content(jsonDto)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getNotificationTemplateTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(ModelUtils.getNotificationTemplateWithPlatformsDto());
        mockMvc.perform(get(url + "/get-template/{id}", 1L)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void saveBadRequestTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationTemplateWithPlatformsUpdateDto dto = ModelUtils.getNotificationTemplateWithPlatformsUpdateDto();
        Long id = 1L;
        String JsonDto = objectMapper.writeValueAsString(dto);
        doThrow(NotFoundException.class)
            .when(notificationTemplateService).update(id, dto);
        mockMvc.perform(put(url + "/update-template/{id}", id)
            .principal(principal)
            .content(JsonDto)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        verify(notificationTemplateService).update(id, dto);
    }

    @Test
    void deactivateNotificationTemplate() throws Exception {
        Long id = 1L;
        String status = INACTIVE.name();
        mockMvc.perform(put(url + "/change-template-status/{id}", id)
            .param("status", status)
            .principal(principal))
            .andExpect(status().isOk());
        verify(notificationTemplateService).changeNotificationStatusById(id, status);
    }

    @Test
    void deactivateNotificationTemplateBadRequestTest() throws Exception {
        Long id = 1L;
        String status = INACTIVE.name();

        doThrow(BadRequestException.class)
            .when(notificationTemplateService).changeNotificationStatusById(id, status);

        mockMvc.perform(put(url + "/change-template-status/{id}", id)
            .param("status", status)
            .principal(principal))
            .andExpect(status().isBadRequest());

        verify(notificationTemplateService).changeNotificationStatusById(id, status);
    }

    @Test
    void deactivateNotificationTemplateNotFoundTest() throws Exception {
        Long id = 1L;
        String status = INACTIVE.name();

        doThrow(NotFoundException.class)
            .when(notificationTemplateService).changeNotificationStatusById(id, status);

        mockMvc.perform(MockMvcRequestBuilders.put(url + "/change-template-status/{id}", id)
            .param("status", status)
            .principal(principal))
            .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(notificationTemplateService).changeNotificationStatusById(id, status);
    }
}
