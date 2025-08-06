package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.SuccessSignInDto;
import greencity.dto.TestersSignInRequest;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.http.RemoteServerUnavailableException;
import greencity.repository.EmployeeRepository;
import greencity.repository.TelegramManagerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramLoginServiceTest {
    @InjectMocks
    private TelegramLoginServiceImpl telegramLoginService;

    @Mock
    private TelegramManagerRepository telegramManagerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TelegramUtils telegramUtils;

    @Mock
    private UserRemoteClient userRemoteClient;

    @Test
    void testLogoutManager_WithExistingManager_ShouldDeleteManager() {
        String chatId = "123";
        TelegramManager manager = new TelegramManager();
        when(telegramManagerRepository.findByChatId(chatId))
            .thenReturn(Optional.of(manager));

        telegramLoginService.logoutManager(chatId);

        verify(telegramManagerRepository).delete(manager);
    }

    @Test
    void testLogoutManager_WithNonExistingManager_ShouldDoNothing() {
        String chatId = "123";
        when(telegramManagerRepository.findByChatId(chatId))
            .thenReturn(Optional.empty());

        telegramLoginService.logoutManager(chatId);

        verify(telegramManagerRepository, never()).delete(any());
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithValidCredentials_ShouldReturnSuccessMessageAndSaveManager() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("manager@test.com:password");
        when(message.getChatId()).thenReturn(123L);

        Employee employee = mock(Employee.class);
        when(employeeRepository.findByEmailWithPositions("manager@test.com"))
            .thenReturn(Optional.of(employee));
        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(true);

        SuccessSignInDto successDto = new SuccessSignInDto(
            1L,
            "access-token",
            "refresh-token",
            "Manager Name",
            false);

        ResponseEntity<SuccessSignInDto> responseEntity = new ResponseEntity<>(successDto, HttpStatus.OK);
        when(userRemoteClient.signIn(any(TestersSignInRequest.class))).thenReturn(responseEntity);

        when(telegramManagerRepository.save(any(TelegramManager.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        SendMessage result = telegramLoginService.processInputManagerCredentialsRequest(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("Manager Name"));
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithIncorrectLoginFormat_ShouldReturnFailMessage() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("wrongformat");
        when(message.getChatId()).thenReturn(123L);

        SendMessage result = telegramLoginService.processInputManagerCredentialsRequest(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains(TelegramBotConstants.INCORRECT_LOGIN_FORMAT));
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithNonExistingUser_ShouldReturnUserIsNotEmployeeMessage() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("test@test.com:password");
        when(message.getChatId()).thenReturn(123L);

        when(employeeRepository.findByEmailWithPositions("test@test.com")).thenReturn(Optional.empty());

        SendMessage result = telegramLoginService.processInputManagerCredentialsRequest(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains(TelegramBotConstants.USER_IS_NOT_EMPLOYEE));
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithNonManagerUser_ShouldReturnNotManagerMessage() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("manager@test.com:password");
        when(message.getChatId()).thenReturn(123L);

        Employee employee = new Employee();
        when(employeeRepository.findByEmailWithPositions("manager@test.com")).thenReturn(Optional.of(employee));
        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(false);

        SendMessage result = telegramLoginService.processInputManagerCredentialsRequest(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains(TelegramBotConstants.EMPLOYEE_IS_NOT_MANAGER));
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithInvalidPassword_ShouldReturnWrongPasswordMessage() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("manager@test.com:password");
        when(message.getChatId()).thenReturn(123L);

        Employee employee = new Employee();
        when(employeeRepository.findByEmailWithPositions("manager@test.com")).thenReturn(Optional.of(employee));
        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(true);

        when(userRemoteClient.signIn(any()))
            .thenThrow(new BadRequestException("{\"name\":\"password\",\"message\":\"Bad password\"}"));

        SendMessage result = telegramLoginService.processInputManagerCredentialsRequest(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains(TelegramBotConstants.LOGIN_FAILED));
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithGenericException_ShouldReturnTryAgainMessage() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("manager@test.com:anypassword");
        when(message.getChatId()).thenReturn(123L);

        Employee employee = new Employee();
        when(employeeRepository.findByEmailWithPositions("manager@test.com")).thenReturn(Optional.of(employee));
        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(true);

        when(userRemoteClient.signIn(any()))
            .thenThrow(new RemoteServerUnavailableException("Server is down"));

        SendMessage result = telegramLoginService.processInputManagerCredentialsRequest(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains(TelegramBotConstants.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN));
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithNullResponseBody_ShouldUseDefaultUsername() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("test@test.com:password");
        when(message.getChatId()).thenReturn(123L);

        Employee employee = new Employee();
        when(employeeRepository.findByEmailWithPositions("test@test.com"))
            .thenReturn(Optional.of(employee));

        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(true);

        ResponseEntity<SuccessSignInDto> responseEntity =
            new ResponseEntity<>(null, HttpStatus.OK);

        when(userRemoteClient.signIn(any())).thenReturn(responseEntity);

        SendMessage result = telegramLoginService.processInputManagerCredentialsRequest(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("username"));

        verify(telegramManagerRepository).save(any(TelegramManager.class));
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithNullNameInResponse_ShouldUseDefaultUsername() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("test@test.com:password");
        when(message.getChatId()).thenReturn(123L);

        Employee employee = new Employee();
        when(employeeRepository.findByEmailWithPositions("test@test.com"))
            .thenReturn(Optional.of(employee));

        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(true);

        SuccessSignInDto successDto = new SuccessSignInDto(
            1L,
            "token",
            "refresh",
            null,
            true);

        ResponseEntity<SuccessSignInDto> responseEntity =
            new ResponseEntity<>(successDto, HttpStatus.OK);

        when(userRemoteClient.signIn(any())).thenReturn(responseEntity);

        SendMessage result = telegramLoginService.processInputManagerCredentialsRequest(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("username"));

        verify(telegramManagerRepository).save(any(TelegramManager.class));
    }
}
