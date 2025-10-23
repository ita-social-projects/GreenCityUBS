package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.constant.TelegramBotConstants;
import greencity.dto.SuccessSignInDto;
import greencity.dto.TestersSignInRequest;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.user.employee.Employee;
import greencity.enums.MessageType;
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

    @Mock
    private TelegramBotResponseServiceImpl telegramBotResponseService;

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

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR)).thenReturn("login error text");
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_SUCCESS)).thenReturn("login success text");

        SendMessage result =
            telegramLoginService.processInputManagerCredentialsRequest(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertEquals("login success text", result.getText());

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR);
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_SUCCESS);
        verify(telegramManagerRepository).save(any(TelegramManager.class));
        verify(userRemoteClient).signIn(any(TestersSignInRequest.class));
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithIncorrectLoginFormat_ShouldReturnFailMessage() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("wrongformat");
        when(message.getChatId()).thenReturn(123L);

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR)).thenReturn("login error text");
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.INCORRECT_LOGIN_FORMAT)).thenReturn("incorrect login format text");

        SendMessage result =
            telegramLoginService.processInputManagerCredentialsRequest(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("login error text"));

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR);
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.INCORRECT_LOGIN_FORMAT);
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithNonExistingUser_ShouldReturnUserIsNotEmployeeMessage() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("test@test.com:password");
        when(message.getChatId()).thenReturn(123L);

        when(employeeRepository.findByEmailWithPositions("test@test.com")).thenReturn(Optional.empty());

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR)).thenReturn("login error text");
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.USER_NOT_EMPLOYEE)).thenReturn("user is not employee text");

        SendMessage result =
            telegramLoginService.processInputManagerCredentialsRequest(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("login error text"));

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR);
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.USER_NOT_EMPLOYEE);
    }

    @Test
    void testProcessInputManagerCredentialsRequest_WithNonManagerUser_ShouldReturnNotManagerMessage() {
        Message message = mock(Message.class);
        when(message.getText()).thenReturn("manager@test.com:password");
        when(message.getChatId()).thenReturn(123L);

        Employee employee = new Employee();
        when(employeeRepository.findByEmailWithPositions("manager@test.com")).thenReturn(Optional.of(employee));
        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(false);

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR)).thenReturn("login error text");
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.EMPLOYEE_NOT_MANAGER)).thenReturn("employee is not manager text");

        SendMessage result =
            telegramLoginService.processInputManagerCredentialsRequest(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("login error text"));

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR);
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.EMPLOYEE_NOT_MANAGER);
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

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR)).thenReturn("login error text");
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_FAILED)).thenReturn("login failed text");

        SendMessage result =
            telegramLoginService.processInputManagerCredentialsRequest(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("login error text"));

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR);
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_FAILED);
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

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR)).thenReturn("login error text");
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.SOMETHING_WENT_WRONG)).thenReturn("something went wrong text");

        SendMessage result =
            telegramLoginService.processInputManagerCredentialsRequest(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("login error text"));

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR);
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.SOMETHING_WENT_WRONG);
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

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR)).thenReturn("login error text");
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_SUCCESS)).thenReturn("username");

        SendMessage result =
            telegramLoginService.processInputManagerCredentialsRequest(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("username"));

        verify(telegramManagerRepository).save(any(TelegramManager.class));
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_SUCCESS);
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

        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_ERROR)).thenReturn("login error text");
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_SUCCESS)).thenReturn("username");

        SendMessage result =
            telegramLoginService.processInputManagerCredentialsRequest(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("username"));

        verify(telegramManagerRepository).save(any(TelegramManager.class));
        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_SUCCESS);
    }
}
