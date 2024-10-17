package greencity.client.config;

import greencity.client.UserRemoteClient;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.employee.EmployeeSignUpDto;
import greencity.dto.employee.EmployeePositionsDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.notification.EmailNotificationDto;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void checkIfUserExistsByUuid() {
        assertThrows(RemoteServerUnavailableException.class, () -> client.checkIfUserExistsByUuid(USER_UUID));
    }

    @Test
    void markUserDeactivated() {
        DeactivateUserRequestDto request = DeactivateUserRequestDto.builder()
            .reason("test")
            .build();
        assertThrows(RemoteServerUnavailableException.class, () -> client.markUserDeactivated(USER_UUID, request));
    }

    @Test
    void getPositionsAndRelatedAuthorities() {
        assertThrows(RemoteServerUnavailableException.class,
            () -> client.getPositionsAndRelatedAuthorities(USER_EMAIL));
    }

    @Test
    void getPasswordStatus() {
        assertThrows(RemoteServerUnavailableException.class, () -> client.getPasswordStatus());
    }

    @Test
    void sendEmailNotification() {
        EmailNotificationDto dto = EmailNotificationDto.builder().email(USER_EMAIL).build();
        assertDoesNotThrow(() -> client.sendEmailNotification(dto));
    }

    @Test
    void getAllAuthorities() {
        assertDoesNotThrow(() -> client.getAllAuthorities(USER_EMAIL));
    }

    @Test
    void updateEmployeesAuthorities() {
        UserEmployeeAuthorityDto dto = UserEmployeeAuthorityDto.builder().build();
        assertThrows(RemoteServerUnavailableException.class, () -> client.updateEmployeesAuthorities(dto));
    }

    @Test
    void signUpEmployee() {
        EmployeeSignUpDto dto = EmployeeSignUpDto.builder().build();
        assertThrows(RemoteServerUnavailableException.class, () -> client.signUpEmployee(dto));
    }

    @Test
    void updateEmployeeEmailTest() {
        String newEmail = "new@mail.com";
        assertThrows(RemoteServerUnavailableException.class, () -> client.updateEmployeeEmail(newEmail, USER_UUID));
    }

    @Test
    void updateAuthoritiesToRelatedPositionsTest() {
        EmployeePositionsDto dto = EmployeePositionsDto.builder().build();
        assertThrows(RemoteServerUnavailableException.class, () -> client.updateAuthoritiesToRelatedPositions(dto));
    }

    @Test
    void deactivateEmployee() {
        String uuid = "87df9ad5-6393-441f-8423-8b2e770b01a8";
        assertThrows(RemoteServerUnavailableException.class, () -> client.deactivateEmployee(uuid));
    }

    @Test
    void activateEmployee() {
        String uuid = "87df9ad5-6393-441f-8423-8b2e770b01a8";
        assertThrows(RemoteServerUnavailableException.class, () -> client.activateEmployee(uuid));
    }
}