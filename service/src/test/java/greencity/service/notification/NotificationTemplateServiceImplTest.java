package greencity.service.notification;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.entity.notifications.NotificationTemplate;
import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationType;
import greencity.enums.UserCategory;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.notification.IncorrectTemplateException;
import greencity.exceptions.notification.TemplateDeleteException;
import greencity.notificator.listener.NotificationPlanner;
import greencity.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import static greencity.ModelUtils.createNotificationTemplate;
import static greencity.ModelUtils.createNotificationTemplateWithPlatformsUpdateDto;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static greencity.ModelUtils.TEST_NOTIFICATION_TEMPLATE;
import static greencity.ModelUtils.TEST_NOTIFICATION_PAGEABLE;
import static greencity.ModelUtils.TEMPLATE_PAGE;
import static greencity.ModelUtils.TEST_NOTIFICATION_TEMPLATE_WITH_PLATFORMS_DTO;
import static greencity.ModelUtils.TEST_NOTIFICATION_TEMPLATE_DTO;
import static greencity.ModelUtils.TEST_NOTIFICATION_TEMPLATE_UPDATE_DTO;
import static greencity.constant.ErrorMessage.NOTIFICATION_STATUS_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.NOTIFICATION_TEMPLATE_NOT_FOUND_BY_ID;
import static greencity.enums.NotificationStatus.INACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceImplTest {
    @Mock
    NotificationTemplateRepository templateRepository;

    @Mock
    NotificationPlanner notificationPlanner;

    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    private NotificationTemplateServiceImpl notificationService;

    @Test
    void findAll() {
        Pageable pageable = TEST_NOTIFICATION_PAGEABLE;
        Page<NotificationTemplate> page = TEMPLATE_PAGE;
        var notification = TEST_NOTIFICATION_TEMPLATE;
        var notificationDto = TEST_NOTIFICATION_TEMPLATE_DTO;

        when(templateRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(notification, NotificationTemplateDto.class)).thenReturn(notificationDto);

        var result = notificationService.findAll(pageable);

        assertEquals(result.getPage(), List.of(notificationDto));
        assertEquals(result.getTotalElements(), page.getTotalElements());
        assertEquals(result.getTotalPages(), page.getTotalPages());

        verify(templateRepository).findAll(pageable);
        verify(modelMapper).map(notification, NotificationTemplateDto.class);
    }

    @Test
    void updateTest() {
        Long id = 1L;

        var updateDto = createNotificationTemplateWithPlatformsUpdateDto();
        var mainInfoDto = updateDto.getNotificationTemplateUpdateInfo();
        var platformDto = updateDto.getPlatforms().getFirst();

        var notification = createNotificationTemplate();

        var platform = notification.getNotificationPlatforms().getFirst();

        when(templateRepository.findById(id)).thenReturn(Optional.of(notification));
        doNothing().when(notificationPlanner).restartNotificator(any());

        notificationService.update(id, updateDto);

        assertEquals(mainInfoDto.getTitle(), notification.getTitle());
        assertEquals(mainInfoDto.getTitleEng(), notification.getTitleEng());
        assertEquals(mainInfoDto.getType(), notification.getNotificationType());
        assertEquals(mainInfoDto.getTrigger(), notification.getTrigger());
        assertEquals(mainInfoDto.getTime(), notification.getTime());
        assertEquals(mainInfoDto.getSchedule(), notification.getSchedule());

        assertEquals(platformDto.getBody(), platform.getBody());
        assertEquals(platformDto.getBodyEng(), platform.getBodyEng());
        assertEquals(platformDto.getStatus(), platform.getNotificationStatus());

        verify(templateRepository).findById(id);
        verify(notificationPlanner).restartNotificator(any());
    }

    @Test
    void updateCustomTestWhenUpdateScheduleColumnIsForbidden() {
        Long id = 1L;

        var updateDto = createNotificationTemplateWithPlatformsUpdateDto();
        var mainInfoDto = updateDto.getNotificationTemplateUpdateInfo();
        mainInfoDto.setUserCategory(UserCategory.ALL_USERS);
        mainInfoDto.setType(NotificationType.CUSTOM);

        var notification = TEST_NOTIFICATION_TEMPLATE;
        notification.setNotificationType(NotificationType.CUSTOM);
        notification.setSchedule("");
        notification.setScheduleUpdateForbidden(true);

        when(templateRepository.findById(id)).thenReturn(Optional.of(notification));
        doNothing().when(notificationPlanner).restartNotificator(notification.getNotificationType());

        notificationService.update(id, updateDto);

        assertNotEquals(mainInfoDto.getSchedule(), notification.getSchedule());
        assertEquals(mainInfoDto.getUserCategory(), notification.getUserCategory());

        verify(templateRepository).findById(id);
        verify(notificationPlanner).restartNotificator(any());
    }

    @Test
    void updateThrowNotFoundExceptionForNotificationTemplateTest() {
        Long id = 1L;

        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
            () -> notificationService.update(id, TEST_NOTIFICATION_TEMPLATE_UPDATE_DTO));
        assertEquals(NOTIFICATION_TEMPLATE_NOT_FOUND_BY_ID + id, exception.getMessage());

        verify(templateRepository).findById(id);
    }

    @Test
    void updateThrowNotFoundExceptionForNotificationPlatform() {
        Long id = 1L;

        var dto = TEST_NOTIFICATION_TEMPLATE_UPDATE_DTO;
        dto.getPlatforms().getFirst().setId(100L);

        when(templateRepository.findById(id)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        var exception = assertThrows(NotFoundException.class, () -> notificationService.update(id, dto));
        assertEquals(ErrorMessage.NOTIFICATION_PLATFORM_NOT_FOUND, exception.getMessage());

        verify(templateRepository).findById(id);
    }

    @Test
    void findByIdTest() {
        Long id = 1L;
        var notification = TEST_NOTIFICATION_TEMPLATE;

        when(templateRepository.findById(id))
            .thenReturn(Optional.of(notification));
        when(modelMapper.map(notification, NotificationTemplateWithPlatformsDto.class))
            .thenReturn(TEST_NOTIFICATION_TEMPLATE_WITH_PLATFORMS_DTO);

        notificationService.findById(id);

        verify(templateRepository).findById(id);
        verify(modelMapper).map(notification, NotificationTemplateWithPlatformsDto.class);
    }

    @Test
    void findByIdThrowNotFoundExceptionTest() {
        Long id = 1L;

        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> notificationService.findById(id));

        assertEquals(NOTIFICATION_TEMPLATE_NOT_FOUND_BY_ID + id, exception.getMessage());

        verify(templateRepository).findById(id);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void changeNotificationStatusByIdTest() {
        Long id = 1L;
        var notificationTemplate = TEST_NOTIFICATION_TEMPLATE;

        when(templateRepository.findById(id)).thenReturn(Optional.of(notificationTemplate));

        notificationService.changeNotificationStatusById(id, INACTIVE.name());

        assertEquals(INACTIVE, notificationTemplate.getNotificationStatus());
        notificationTemplate.getNotificationPlatforms()
            .forEach(platform -> assertEquals(INACTIVE, platform.getNotificationStatus()));

        verify(templateRepository).findById(id);
    }

    @Test
    void changeNotificationStatusByIdThrowBadRequestException() {
        Long id = 1L;
        String status = "FAKE";

        var exception = assertThrows(
            BadRequestException.class, () -> notificationService.changeNotificationStatusById(id, status));
        assertEquals(NOTIFICATION_STATUS_DOES_NOT_EXIST + status, exception.getMessage());

        verify(templateRepository, never()).findById(id);
    }

    @Test
    void changeNotificationStatusByIdThrowNotFoundExceptionTest() {
        Long id = 1L;
        String newStatus = INACTIVE.toString();

        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(
            NotFoundException.class, () -> notificationService.changeNotificationStatusById(id, newStatus));
        assertEquals(NOTIFICATION_TEMPLATE_NOT_FOUND_BY_ID + id, exception.getMessage());

        verify(templateRepository).findById(id);
    }

    @Test
    void createNotificationTemplateTest() {
        var dto = ModelUtils.createAddNotificationTemplateWithPlatforms();
        var template = createNotificationTemplate();
        template.setNotificationType(NotificationType.CUSTOM);
        template.setUserCategory(UserCategory.ALL_USERS);

        when(modelMapper.map(dto, NotificationTemplate.class)).thenReturn(template);

        when(templateRepository.save(any(NotificationTemplate.class)))
            .thenReturn(template);

        doNothing().when(notificationPlanner).restartNotificator(NotificationType.CUSTOM);

        assertDoesNotThrow(() -> notificationService.createNotificationTemplate(dto));

        verify(modelMapper).map(any(), eq(NotificationTemplate.class));
        verify(templateRepository).save(any());
        verify(notificationPlanner).restartNotificator(any(NotificationType.class));
    }

    @Test
    void createNotificationTemplateThrowsIncorrectTemplateExceptionTest() {
        var dto = ModelUtils.createAddNotificationTemplateWithPlatforms();
        var template = createNotificationTemplate();
        template.setNotificationType(NotificationType.CUSTOM);
        template.setUserCategory(UserCategory.ALL_USERS);
        template.getNotificationPlatforms().getFirst().setNotificationReceiverType(NotificationReceiverType.MOBILE);

        when(modelMapper.map(dto, NotificationTemplate.class)).thenReturn(template);

        assertThrows(IncorrectTemplateException.class, () -> notificationService.createNotificationTemplate(dto));
        verify(modelMapper).map(any(), eq(NotificationTemplate.class));
    }

    @Test
    void removeNotificationTemplateTest() {
        var template = createNotificationTemplate();
        template.setNotificationType(NotificationType.CUSTOM);
        template.setUserCategory(UserCategory.ALL_USERS);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        doNothing().when(templateRepository).deleteById(1L);
        doNothing().when(notificationPlanner).restartNotificator(NotificationType.CUSTOM);

        assertDoesNotThrow(() -> notificationService.removeNotificationTemplate(1L));
        verify(templateRepository).findById(anyLong());
        verify(templateRepository).deleteById(anyLong());
        verify(notificationPlanner).restartNotificator(any());
    }

    @Test
    void removeNotificationTemplateThrowsNotFoundTest(){
        when(templateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,() -> notificationService.removeNotificationTemplate(1L));
        verify(templateRepository).findById(anyLong());
    }

    @Test
    void removeNotificationTemplateThrowsTemplateDeleteExceptionTest() {
        var template = createNotificationTemplate();
        template.setNotificationType(NotificationType.COURIER_ITINERARY_FORMED);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

        assertThrows(TemplateDeleteException.class, () -> notificationService.removeNotificationTemplate(1L));
        verify(templateRepository).findById(anyLong());
    }

}
