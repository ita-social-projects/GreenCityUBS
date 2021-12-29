package greencity.service.notification;

import greencity.ModelUtils;
import greencity.dto.NotificationTemplateDto;
import greencity.entity.notifications.NotificationTemplate;
import greencity.exceptions.NotFoundException;
import greencity.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagementNotificationServiceImplTest {
    @Mock
    NotificationTemplateRepository templateRepository;
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    private ManagementNotificationServiceImpl notificationService;

    @Test
    void findAll() {
        NotificationTemplateDto dto = ModelUtils.getNotificationTemplateDto();
        NotificationTemplate template = ModelUtils.getNotificationTemplate();
        when(templateRepository.findAll(ModelUtils.TEST_PAGEABLE_NOTIFICATION_TEMPLATE)).thenReturn(
            ModelUtils.TEST_NOTIFICATION_TEMPLATE_PAGE);
        when(modelMapper.map(template, NotificationTemplateDto.class))
            .thenReturn(dto);
        notificationService.findAll(ModelUtils.TEST_PAGEABLE_NOTIFICATION_TEMPLATE);
        verify(templateRepository).findAll(ModelUtils.TEST_PAGEABLE_NOTIFICATION_TEMPLATE);
        verify(modelMapper).map(template, NotificationTemplateDto.class);
    }

    @Test
    void updateTest() {
        NotificationTemplateDto dto = ModelUtils.getNotificationTemplateDto();
        NotificationTemplate template = ModelUtils.getNotificationTemplate();
        when(templateRepository.findNotificationTemplateById(1L)).thenReturn(
            Optional.of(template));

        notificationService.update(dto);

        verify(templateRepository).findNotificationTemplateById(1L);
        verify(templateRepository).save(template);
    }

    @Test
    void findByIdTest() {
        NotificationTemplateDto dto = ModelUtils.getNotificationTemplateDto();
        NotificationTemplate template = ModelUtils.getNotificationTemplate();
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
