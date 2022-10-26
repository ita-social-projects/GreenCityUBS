package greencity.service.notification;

import greencity.ModelUtils;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateLocalizedDto;
import greencity.dto.pageble.PageableDto;
import greencity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.schedule.NotificationSchedule;
import greencity.exceptions.NotFoundException;
import greencity.repository.NotificationScheduleRepo;
import greencity.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceImplTest {
    @Mock
    NotificationTemplateRepository templateRepository;
    @Mock
    ModelMapper modelMapper;
    @Mock
    NotificationScheduleRepo scheduleRepo;
    @InjectMocks
    private NotificationTemplateServiceImpl notificationService;

    @Test
    void findAll() {
        NotificationTemplate template = ModelUtils.TEST_TEMPLATE;
        NotificationTemplateLocalizedDto templateLocalizedDto = NotificationTemplateLocalizedDto.builder()
            .id(1L)
            .notificationType("UNPAID_ORDER")
            .build();
        NotificationSchedule notificationSchedule = ModelUtils.NOTIFICATION_SCHEDULE
            .setNotificationType(NotificationType.UNPAID_ORDER);
        when(scheduleRepo.findNotificationScheduleByNotificationType(NotificationType.UNPAID_ORDER))
            .thenReturn(notificationSchedule);
        when(templateRepository.findAllTemplates(ModelUtils.TEST_PAGEABLE_NOTIFICATION_TEMPLATE, "ua")).thenReturn(
            ModelUtils.TEST_NOTIFICATION_TEMPLATE_PAGE);
        when(templateRepository.findAllTemplates(ModelUtils.TEST_PAGEABLE_NOTIFICATION_TEMPLATE, "en")).thenReturn(
            ModelUtils.TEST_NOTIFICATION_TEMPLATE_PAGE);
        when(modelMapper.map(template, NotificationTemplateLocalizedDto.class)).thenReturn(templateLocalizedDto);
        PageableDto<NotificationTemplateLocalizedDto> actual = notificationService.findAll(
            ModelUtils.TEST_PAGEABLE_NOTIFICATION_TEMPLATE);
        verify(templateRepository).findAllTemplates(ModelUtils.TEST_PAGEABLE_NOTIFICATION_TEMPLATE, "ua");
        verify(modelMapper).map(template, NotificationTemplateLocalizedDto.class);
        assertEquals(ModelUtils.getNotificationTemplateLocalizedDto(), actual);
    }

    @Test
    void updateTest() {
        NotificationTemplateDto dto = ModelUtils.TEST_NOTIFICATION_TEMPLATE_DTO;
        NotificationTemplate template = ModelUtils.TEST_TEMPLATE;
        when(templateRepository.findNotificationTemplateById(1L)).thenReturn(
            Optional.of(template));
        NotificationSchedule notificationSchedule = ModelUtils.NOTIFICATION_SCHEDULE
            .setNotificationType(NotificationType.UNPAID_ORDER);
        when(scheduleRepo.getOne(NotificationType.UNPAID_ORDER)).thenReturn(notificationSchedule);
        notificationService.update(dto);
        verify(scheduleRepo).save(ModelUtils.NOTIFICATION_SCHEDULE);
        verify(templateRepository).findNotificationTemplateById(1L);
        verify(templateRepository).save(template);
    }

    @Test
    void findByIdTest() {
        NotificationTemplateDto dto = ModelUtils.TEST_NOTIFICATION_TEMPLATE_DTO;
        NotificationTemplate template = ModelUtils.TEST_TEMPLATE;
        when(templateRepository.findNotificationTemplateById(1L)).thenReturn(
            Optional.of(template));
        when(modelMapper.map(template, NotificationTemplateDto.class))
            .thenReturn(dto);
        notificationService.findById(1L);
        verify(templateRepository).findNotificationTemplateById(1L);
        verify(modelMapper).map(template, NotificationTemplateDto.class);
    }

    @Test
    void findByIdNotFoundExceptionTest() {
        assertThrows(NotFoundException.class, () -> notificationService.findById(2L));
    }
}
