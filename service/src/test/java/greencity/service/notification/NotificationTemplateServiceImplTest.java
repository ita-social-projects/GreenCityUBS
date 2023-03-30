package greencity.service.notification;

import greencity.exceptions.NotFoundException;
import greencity.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
    }

    @Test
    void findByIdNotFoundExceptionTest() {
        assertThrows(NotFoundException.class, () -> notificationService.findById(2L));
    }
}
