package greencity.controller;

import greencity.dto.NotificationDto;
import greencity.service.ubs.NotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class NotificationControllerTest {
    @Mock
    private NotificationService notificationService;

    private NotificationController notificationController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.notificationController = new NotificationController(notificationService);
    }

    @Test
    public void testGetNotificationsForCurrentUser() {
        when(notificationService.getAllNotificationsForUser("123", "en"))
            .thenReturn(Collections.singletonList(new NotificationDto("Title", "Body")));

        ResponseEntity<List<NotificationDto>> expectedResponse = ResponseEntity.status(HttpStatus.OK)
            .body(Collections.singletonList(new NotificationDto("Title", "Body")));

        ResponseEntity<List<NotificationDto>> actualResponse =
            notificationController.getNotificationsForCurrentUser("123", Locale.ENGLISH);

        assertEquals(expectedResponse, actualResponse);
        verify(notificationService).getAllNotificationsForUser("123", "en");
    }
}
