package greencity.ubstelegrambot.service;

import greencity.ModelUtils;
import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.telegram.UnknownTelegramUser;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.TelegramUser;
import greencity.exceptions.NotFoundException;
import greencity.repository.AuthorizedUserRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.repository.UnknownTelegramUserRepository;
import greencity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramAuthorizationServiceImplTest {

    @Mock
    private UnknownTelegramUserRepository unknownTelegramUserRepository;

    @Mock
    private AuthorizedUserRepository authorizedUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private TelegramManagerRepository telegramManagerRepository;

    @InjectMocks
    private TelegramAuthorizationServiceImpl telegramAuthorizationService;

    private Long tgUserId;
    private String uuId;
    private String chatId;
    private greencity.entity.user.User user;
    private Employee employee;
    private Position serviceManager;
    private Position manager;

    @BeforeEach
    void setUp() {
        tgUserId = 12345L;
        uuId = UUID.randomUUID().toString();
        chatId = "123";
        user = ModelUtils.getUser();
        employee = spy(ModelUtils.getEmployee());
        serviceManager = Mockito.mock(Position.class);
        manager = Mockito.mock(Position.class);
    }

    @Test
    void handleAuthorizedUserTest_WhenEmployeeIsPresent_AndServiceManagerIsFound_AndManagerIsFound_AndEmployeePositionsContainServiceManager() {
        Set<Position> setContainingServiceManagerPosition = Set.of(serviceManager);
        TelegramUser expectedResult = TelegramUser.MANAGER;

        when(userRepository.findUserByUuid(uuId))
            .thenReturn(Optional.of(user));
        when(employeeRepository.findByUuid(uuId))
            .thenReturn(Optional.of(employee));
        when(employee.getEmployeePosition())
            .thenReturn(setContainingServiceManagerPosition);
        when(positionRepository.findById(1L))
            .thenReturn(Optional.of(serviceManager));
        when(positionRepository.findById(2L))
            .thenReturn(Optional.of(manager));

        TelegramUser actualResult = telegramAuthorizationService.handleAuthorizedUser(
            uuId,
            chatId);

        assertEquals(expectedResult, actualResult);
        verify(userRepository).findUserByUuid(uuId);
        verify(employeeRepository).findByUuid(uuId);
        verify(positionRepository).findById(1L);
        verify(positionRepository).findById(2L);
        verify(telegramManagerRepository).save(any(TelegramManager.class));
    }

    @Test
    void handleAuthorizedUserTest_WhenEmployeeIsPresent_AndServiceManagerIsFound_AndManagerIsFound_AndEmployeePositionsContainManager() {
        Set<Position> setContainingManagerPosition = Set.of(manager);
        TelegramUser expectedResult = TelegramUser.MANAGER;

        when(userRepository.findUserByUuid(uuId))
            .thenReturn(Optional.of(user));
        when(employeeRepository.findByUuid(uuId))
            .thenReturn(Optional.of(employee));
        when(employee.getEmployeePosition())
            .thenReturn(setContainingManagerPosition);
        when(positionRepository.findById(1L))
            .thenReturn(Optional.of(serviceManager));
        when(positionRepository.findById(2L))
            .thenReturn(Optional.of(manager));

        TelegramUser actualResult = telegramAuthorizationService.handleAuthorizedUser(
            uuId,
            chatId);

        assertEquals(expectedResult, actualResult);
        verify(userRepository).findUserByUuid(uuId);
        verify(employeeRepository).findByUuid(uuId);
        verify(positionRepository).findById(1L);
        verify(positionRepository).findById(2L);
        verify(telegramManagerRepository).save(any(TelegramManager.class));
    }

    @Test
    void handleAuthorizedUserTest_WhenEmployeeIsPresent_AndServiceManagerIsNotFound() {
        when(userRepository.findUserByUuid(uuId))
                .thenReturn(Optional.of(user));
        when(employeeRepository.findByUuid(uuId))
                .thenReturn(Optional.of(employee));
        when(positionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> telegramAuthorizationService.handleAuthorizedUser(
                        uuId,
                        chatId
                )
        );
        verify(userRepository).findUserByUuid(uuId);
        verify(employeeRepository).findByUuid(uuId);
        verify(positionRepository).findById(1L);
    }

    @Test
    void handleAuthorizedUserTest_WhenEmployeeIsPresent_AndServiceManagerIsFound_AndManagerIsNotFound() {
        when(userRepository.findUserByUuid(uuId))
                .thenReturn(Optional.of(user));
        when(employeeRepository.findByUuid(uuId))
                .thenReturn(Optional.of(employee));
        when(positionRepository.findById(1L))
                .thenReturn(Optional.of(serviceManager));
        when(positionRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> telegramAuthorizationService.handleAuthorizedUser(
                        uuId,
                        chatId
                )
        );
        verify(userRepository).findUserByUuid(uuId);
        verify(employeeRepository).findByUuid(uuId);
        verify(positionRepository).findById(1L);
        verify(positionRepository).findById(2L);
    }

    @Test
    void handleAuthorizedUserTest_WhenEmployeeIsPresent_AndServiceManagerIsFound_AndManagerIsFound_AndEmployeeIsNotManager() {
        Set<Position> setThatDoesNotContainManagerPosition = Collections.emptySet();
        TelegramUser expectedResult = TelegramUser.USER;

        when(userRepository.findUserByUuid(uuId))
            .thenReturn(Optional.of(user));
        when(employeeRepository.findByUuid(uuId))
            .thenReturn(Optional.of(employee));
        when(employee.getEmployeePosition())
            .thenReturn(setThatDoesNotContainManagerPosition);
        when(positionRepository.findById(1L))
            .thenReturn(Optional.of(serviceManager));
        when(positionRepository.findById(2L))
            .thenReturn(Optional.of(manager));

        TelegramUser actualResult = telegramAuthorizationService.handleAuthorizedUser(
            uuId,
            chatId);

        assertEquals(expectedResult, actualResult);
        verify(userRepository).findUserByUuid(uuId);
        verify(employeeRepository).findByUuid(uuId);
        verify(positionRepository).findById(1L);
        verify(positionRepository).findById(2L);
        verify(telegramManagerRepository, never()).save(any());
    }

    @Test
    void handleAuthorizedUserTest_WhenEmployeeIsAbsent_AndUnknownSavedTelegramUserIsPresent() {
        Optional<Employee> emptyEmployeeOptional = Optional.empty();
        Optional<UnknownTelegramUser> optionalUnknownTelegramUser =
            Optional.of(Mockito.mock(UnknownTelegramUser.class));
        UnknownTelegramUser unknownTelegramUser = optionalUnknownTelegramUser.get();
        TelegramUser expectedResult = TelegramUser.UNKNOWN_USER;

        lenient().when(userRepository.findUserByUuid(uuId))
            .thenReturn(Optional.of(user));
        lenient().when(employeeRepository.findByUuid(uuId))
            .thenReturn(emptyEmployeeOptional);
        lenient().when(unknownTelegramUserRepository.findById(tgUserId.toString()))
            .thenReturn(optionalUnknownTelegramUser);

        TelegramUser actualResult = telegramAuthorizationService.handleAuthorizedUser(
            uuId,
            tgUserId.toString());

        assertEquals(expectedResult, actualResult);
        verify(userRepository).findUserByUuid(uuId);
        verify(employeeRepository).findByUuid(uuId);
        verify(unknownTelegramUserRepository).findById(tgUserId.toString());
        verify(unknownTelegramUserRepository).delete(unknownTelegramUser);
        verify(authorizedUserRepository).save(any(AuthorizedUser.class));
    }

    @Test
    void handleAuthorizedUserTest_WhenEmployeeIsAbsent_AndUnknownSavedTelegramUserIsAbsent_AndRegisteredUserBotIsEmpty() {
        Optional<Employee> emptyEmployeeOptional = Optional.empty();
        Optional<UnknownTelegramUser> emptyUnknownTelegramUserOptional = Optional.empty();
        Optional<AuthorizedUser> emptyRegisteredUserBotOptional = Optional.empty();
        TelegramUser expectedResult = TelegramUser.USER;

        lenient().when(userRepository.findUserByUuid(uuId))
            .thenReturn(Optional.of(user));
        lenient().when(employeeRepository.findByUuid(uuId))
            .thenReturn(emptyEmployeeOptional);
        lenient().when(unknownTelegramUserRepository.findById(tgUserId.toString()))
            .thenReturn(emptyUnknownTelegramUserOptional);
        lenient().when(authorizedUserRepository.findByChatId(tgUserId.toString()))
            .thenReturn(emptyRegisteredUserBotOptional);

        TelegramUser actualResult = telegramAuthorizationService.handleAuthorizedUser(
            uuId,
            tgUserId.toString());

        assertEquals(expectedResult, actualResult);
        verify(userRepository).findUserByUuid(uuId);
        verify(employeeRepository).findByUuid(uuId);
        verify(unknownTelegramUserRepository).findById(tgUserId.toString());
        verify(authorizedUserRepository).findByChatId(tgUserId.toString());
        verify(authorizedUserRepository).save(any(AuthorizedUser.class));
    }

    @Test
    void handleAuthorizedUserTest_WhenEmployeeIsAbsent_AndUnknownSavedTelegramUserIsAbsent_AndRegisteredUserBotIsPresent() {
        Optional<Employee> emptyEmployeeOptional = Optional.empty();
        Optional<UnknownTelegramUser> emptyUnknownTelegramUserOptional = Optional.empty();
        Optional<AuthorizedUser> registeredUserBotOptional = Optional.of(Mockito.mock(AuthorizedUser.class));
        TelegramUser expectedResult = TelegramUser.USER;

        lenient().when(userRepository.findUserByUuid(uuId))
            .thenReturn(Optional.of(user));
        lenient().when(employeeRepository.findByUuid(uuId))
            .thenReturn(emptyEmployeeOptional);
        lenient().when(unknownTelegramUserRepository.findById(tgUserId.toString()))
            .thenReturn(emptyUnknownTelegramUserOptional);

        lenient().when(authorizedUserRepository.findByChatId(tgUserId.toString()))
            .thenReturn(registeredUserBotOptional);

        TelegramUser actualResult = telegramAuthorizationService.handleAuthorizedUser(
            uuId,
            tgUserId.toString());

        assertEquals(expectedResult, actualResult);
        verify(userRepository).findUserByUuid(uuId);
        verify(employeeRepository).findByUuid(uuId);
        verify(unknownTelegramUserRepository).findById(tgUserId.toString());
        verify(authorizedUserRepository).findByChatId(tgUserId.toString());
    }

    @Test
    void handleUnknownTelegramUserWithUnknownUserTest() {
        Message message = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        Contact contact = Mockito.mock(Contact.class);

        String firstName = "firstName";
        String lastName = "lastName";
        String userName = "username";
        String phoneNumber = "380501234567";
        ArgumentCaptor<UnknownTelegramUser> unknownTelegramUserCaptor =
            ArgumentCaptor.forClass(UnknownTelegramUser.class);

        when(message.getFrom()).thenReturn(user);
        when(message.getContact()).thenReturn(contact);

        when(user.getId()).thenReturn(tgUserId);
        when(user.getFirstName()).thenReturn(firstName);
        when(user.getLastName()).thenReturn(lastName);
        when(user.getUserName()).thenReturn(userName);
        when(contact.getPhoneNumber()).thenReturn(phoneNumber);
        when(authorizedUserRepository.findByChatId(tgUserId.toString())).thenReturn(Optional.empty());

        telegramAuthorizationService.handleUnknownTelegramUser(message);

        verify(authorizedUserRepository).findByChatId(tgUserId.toString());
        verify(unknownTelegramUserRepository).save(unknownTelegramUserCaptor.capture());

        UnknownTelegramUser unknownTelegramUser = unknownTelegramUserCaptor.getValue();
        unknownTelegramUser.setId(tgUserId);
        assertEquals(tgUserId, unknownTelegramUser.getId());
        assertEquals(firstName, unknownTelegramUser.getFirstName());
        assertEquals(lastName, unknownTelegramUser.getLastName());
        assertEquals(userName, unknownTelegramUser.getUserName());
        assertEquals(phoneNumber, unknownTelegramUser.getMobileNumber());
    }

    @Test
    void handleUnknownTelegramUserWithUnknownReturnedUserTest() {
        String phoneNumber = "380501234567";
        Message message = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        AuthorizedUser authorizedUser = new AuthorizedUser();
        Contact contact = Mockito.mock(Contact.class);

        when(message.getFrom())
            .thenReturn(user);
        when(user.getId())
            .thenReturn(tgUserId);
        when(message.getContact()).thenReturn(contact);
        when(contact.getPhoneNumber()).thenReturn(phoneNumber);
        when(authorizedUserRepository.findByChatId(tgUserId.toString()))
            .thenReturn(Optional.of(authorizedUser));

        telegramAuthorizationService.handleUnknownTelegramUser(message);

        verify(authorizedUserRepository, times(1)).findByChatId(tgUserId.toString());
        verify(unknownTelegramUserRepository, never()).save(any());
    }
}
