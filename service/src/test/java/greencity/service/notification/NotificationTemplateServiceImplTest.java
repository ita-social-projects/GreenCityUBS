package greencity.service.notification;

import greencity.ModelUtils;
import greencity.dto.notification.NotificationTemplateDto;
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
class NotificationTemplateServiceImplTest {
    @Mock
    NotificationTemplateRepository templateRepository;
    @Mock
    ModelMapper modelMapper;
    @InjectMocks
    private NotificationTemplateServiceImpl notificationService;

    @Test
    void findAll() {

    }

    @Test
    void updateTest() {

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
