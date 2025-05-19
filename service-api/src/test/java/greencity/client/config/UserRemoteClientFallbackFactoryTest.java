package greencity.client.config;

import feign.FeignException;
import feign.Request;
import greencity.client.UserRemoteClient;
import greencity.dto.notification.ScheduledEmailMessage;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.employee.EmployeeSignUpDto;
import greencity.dto.employee.EmployeePositionsDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.RemoteServerUnavailableException;
import greencity.exceptions.user.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void sendScheduledEmailNotification() {
        ScheduledEmailMessage dto = ScheduledEmailMessage.builder().userUuid(USER_UUID).build();
        assertDoesNotThrow(() -> client.sendScheduledEmailNotification(dto));
    }

    @Test
    void findUserLanguageByUuid() {
        assertDoesNotThrow(() -> client.findUserLanguageByUuid(USER_UUID));
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

    @Test
    void findLanguageByEmailUserNotFoundTest() {
        FeignException.NotFound exception = mock(FeignException.NotFound.class);
        when(exception.contentUTF8()).thenReturn("user does not exist");

        UserRemoteClient fallback = new UserRemoteClientFallbackFactory().create(exception);

        assertThrows(UserNotFoundException.class, () -> fallback.findLanguageByEmail("test@example.com"));
    }

    @Test
    void findLanguageByEmailLanguageNotSetTest() {
        FeignException.NotFound exception = mock(FeignException.NotFound.class);
        when(exception.contentUTF8()).thenReturn("Language not set");

        UserRemoteClient fallback = new UserRemoteClientFallbackFactory().create(exception);

        assertThrows(NotFoundException.class, () -> fallback.findLanguageByEmail("test@example.com"));
    }

    @Test
    void findLanguageByEmailOther404Test() {
        FeignException.NotFound exception = mock(FeignException.NotFound.class);
        when(exception.contentUTF8()).thenReturn("404 from user service");

        UserRemoteClient fallback = new UserRemoteClientFallbackFactory().create(exception);

        assertThrows(NotFoundException.class, () -> fallback.findLanguageByEmail("test@example.com"));
    }

    @Test
    void existsNotDeactivatedByEmailTest() {
        UserRemoteClient fallback = new UserRemoteClientFallbackFactory()
                .create(new FeignException.NotFound("404", mock(Request.class), null, null));

        boolean result = fallback.existsNotDeactivatedByEmail("test@example.com");

        assertFalse(result);
    }
}