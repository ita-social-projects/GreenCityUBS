package greencity.client.config;

import greencity.client.UserRemoteClient;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.notification.NotificationDto;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserRemoteClientFallbackFactoryTest {
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_UUID = "849446d9-186f-4386-b76e-32aed1c3b1aa";
    @InjectMocks
    private UserRemoteClientFallbackFactory fallbackFactory;

    private UserRemoteClient client;

    @BeforeEach
    void setUp() {
        Throwable throwable = new RuntimeException();
        client = fallbackFactory.create(throwable);
    }

    @Test
    void findUuidByEmail() {
        assertThrows(RemoteServerUnavailableException.class, () -> client.findUuidByEmail(USER_EMAIL));
    }

    @Test
    void findNotDeactivatedByEmail() {
        assertEquals(Optional.empty(), client.findNotDeactivatedByEmail(USER_EMAIL));
    }

    @Test
    void findByUuid() {
        assertEquals(Optional.empty(), client.findByUuid(USER_EMAIL));
    }

    @Test
    void markUserDeactivated() {
        assertThrows(RemoteServerUnavailableException.class, () -> client.markUserDeactivated(USER_UUID));
    }

    @Test
    void getPasswordStatus() {
        assertThrows(RemoteServerUnavailableException.class, () -> client.getPasswordStatus());
    }

    @Test
    void sendEmailNotification() {
        NotificationDto dto = NotificationDto.builder().build();
        assertDoesNotThrow(() -> client.sendEmailNotification(dto, USER_EMAIL));
    }

    @Test
    void getAllAuthorities() {
        assertDoesNotThrow(() -> client.getAllAuthorities(USER_EMAIL));
    }

    @Test
    void updateEmployeesAuthorities() {
        UserEmployeeAuthorityDto dto = UserEmployeeAuthorityDto.builder().build();
        assertThrows(RemoteServerUnavailableException.class, () -> client.updateEmployeesAuthorities(dto, USER_EMAIL));
    }
}