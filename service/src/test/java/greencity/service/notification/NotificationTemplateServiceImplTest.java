package greencity.service.notification;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.entity.notifications.NotificationTemplate;
import greencity.exceptions.NotFoundException;
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

import static greencity.constant.ErrorMessage.NOTIFICATION_TEMPLATE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceImplTest {
    @Mock
    NotificationTemplateRepository templateRepository;
    @Mock
    ModelMapper modelMapper;
    @InjectMocks
    private NotificationTemplateServiceImpl notificationService;

    @Test
    void findAll() {
        Pageable pageable = ModelUtils.TEST_NOTIFICATION_PAGEABLE;
        Page<NotificationTemplate> page = ModelUtils.TEMPLATE_PAGE;
        var notification = ModelUtils.TEST_NOTIFICATION_TEMPLATE;
        var notificationDto = ModelUtils.TEST_NOTIFICATION_TEMPLATE_DTO;

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

        var dto = ModelUtils.TEST_NOTIFICATION_TEMPLATE_UPDATE_DTO;
        var platformDto = dto.getPlatforms().get(0);

        var notification = ModelUtils.TEST_NOTIFICATION_TEMPLATE;
        var platform = notification.getNotificationPlatforms().get(0);

        when(templateRepository.findById(id)).thenReturn(Optional.of(notification));

        notificationService.update(id, dto);

        assertEquals(dto.getTitle(), notification.getTitle());
        assertEquals(dto.getTitleEng(), notification.getTitleEng());
        assertEquals(dto.getType(), notification.getNotificationType());
        assertEquals(dto.getTrigger(), notification.getTrigger());
        assertEquals(dto.getTime(), notification.getTime());
        assertEquals(dto.getSchedule(), notification.getSchedule());

        assertEquals(platformDto.getBody(), platform.getBody());
        assertEquals(platformDto.getBodyEng(), platform.getBodyEng());
        assertEquals(platformDto.getStatus(), platform.getNotificationStatus());

        verify(templateRepository).findById(id);
    }

    @Test
    void updateThrowNotFoundExceptionForNotificationTemplateTest() {
        Long id = 1L;

        var dto = ModelUtils.TEST_NOTIFICATION_TEMPLATE_UPDATE_DTO;

        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> notificationService.update(id, dto));
        assertEquals(NOTIFICATION_TEMPLATE_NOT_FOUND, exception.getMessage());

        verify(templateRepository).findById(id);
    }

    @Test
    void updateThrowNotFoundExceptionForNotificationPlatform() {
        Long id = 1L;

        var dto = ModelUtils.TEST_NOTIFICATION_TEMPLATE_UPDATE_DTO;
        dto.getPlatforms().get(0).setId(100L);

        var notification = ModelUtils.TEST_NOTIFICATION_TEMPLATE;

        when(templateRepository.findById(id)).thenReturn(Optional.of(notification));

        var exception = assertThrows(NotFoundException.class, () -> notificationService.update(id, dto));
        assertEquals(ErrorMessage.NOTIFICATION_PLATFORM_NOT_FOUND, exception.getMessage());

        verify(templateRepository).findById(id);
    }

    @Test
    void findByIdTest() {
        Long id = 1L;
        var notification = ModelUtils.TEST_NOTIFICATION_TEMPLATE;

        when(templateRepository.findById(id))
            .thenReturn(Optional.of(notification));
        when(modelMapper.map(notification, NotificationTemplateWithPlatformsDto.class))
            .thenReturn(ModelUtils.TEST_NOTIFICATION_TEMPLATE_WITH_PLATFORMS_DTO);

        notificationService.findById(id);

        verify(templateRepository).findById(id);
        verify(modelMapper).map(notification, NotificationTemplateWithPlatformsDto.class);
    }

    @Test
    void findByIdThrowNotFoundExceptionTest() {
        Long id = 1L;

        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> notificationService.findById(id));

        assertEquals(NOTIFICATION_TEMPLATE_NOT_FOUND, exception.getMessage());

        verify(templateRepository).findById(id);
        verify(modelMapper, never()).map(any(), any());
    }
}
