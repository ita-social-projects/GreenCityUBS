package greencity.service;


import greencity.repository.NotificationTemplateRepository;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void testGetNotificationTemplates(){
        notificationService.getNotificationTemplates();
    }

}
