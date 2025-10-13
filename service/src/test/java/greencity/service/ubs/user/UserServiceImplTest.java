package greencity.service.ubs.user;

import static greencity.ModelUtils.TEST_EMAIL;
import static greencity.ModelUtils.addressDtoList;
import static greencity.ModelUtils.addressDtoListWithNullPlaceId;
import static greencity.ModelUtils.addressList;
import static greencity.ModelUtils.botList;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getPositionAuthoritiesDto;
import static greencity.ModelUtils.getTelegramBotNotifyTrue;
import static greencity.ModelUtils.getTestOrderAddressDtoRequest;
import static greencity.ModelUtils.getTestOrderAddressDtoRequestWithNullPlaceId;
import static greencity.ModelUtils.getUBSuser;
import static greencity.ModelUtils.getUbsCustomer;
import static greencity.ModelUtils.getUbsCustomersDtoUpdate;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserProfileCreateDto;
import static greencity.ModelUtils.getUserProfileUpdateDto;
import static greencity.ModelUtils.getUserProfileUpdateDtoWithBotsIsNotifyFalse;
import static greencity.ModelUtils.getUserWithBotNotifyTrue;
import static greencity.constant.AppConstant.USER_WITH_PREFIX;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.address.AddressDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.UserActivationDto;
import greencity.dto.user.UserDeactivationReasonDto;
import greencity.dto.user.UserDeletionReasonDto;
import greencity.dto.user.UserExternalDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import greencity.entity.user.UserDeactivationReason;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.ForbiddenException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.exceptions.user.UserStatusUpdateException;
import greencity.repository.AddressRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserDeactivationRepo;
import greencity.repository.UserRepository;
import greencity.service.ubs.AddressService;
import greencity.service.ubs.EventService;
import greencity.util.Bot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private static MockedStatic<SecurityContextHolder> mockedContextHolder;

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UBSUserRepository ubsUserRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private EventService eventService;
    @Mock
    private UserRemoteClient userRemoteClient;
    @Mock
    private TelegramChatRepository telegramChatRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private AddressService addressService;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserDeactivationRepo userDeactivationRepo;

    private User testUser;
    private UserExternalDto currentUserDto;
    private UserExternalDto targetUserDto;

    @BeforeAll
    static void setUp() {
        mockedContextHolder = mockStatic(SecurityContextHolder.class);
    }

    @AfterAll
    static void tearDown() {
        mockedContextHolder.close();
    }

    @BeforeEach
    void setUpEach() {
        testUser = User.builder()
            .id(1L)
            .uuid("test-uuid-123")
            .status(UserStatus.ACTIVATED)
            .recipientEmail("test@example.com")
            .recipientName("Test User")
            .build();

        currentUserDto = UserExternalDto.builder()
            .uuid("current-user-uuid")
            .name("Current User")
            .role(Role.ROLE_ADMIN)
            .build();

        targetUserDto = UserExternalDto.builder()
            .uuid("test-uuid-123")
            .name("Target User")
            .role(Role.ROLE_USER)
            .build();
    }

    @Test
    void getsUserAndUserUbsAndViolationsInfoByValidOrderIdTest() {
        User user = getUser();
        UBSuser ubsUser = getUBSuser();
        ubsUser.setUser(user);
        UserInfoDto expectedResult = UserInfoDto.builder()
            .customerName(ubsUser.getFirstName())
            .customerSurname(ubsUser.getLastName())
            .customerEmail(ubsUser.getEmail())
            .customerPhoneNumber(ubsUser.getPhoneNumber())
            .customerId(1L)
            .senderName(ubsUser.getSenderFirstName())
            .senderSurname(ubsUser.getSenderLastName())
            .senderEmail(ubsUser.getSenderEmail())
            .senderPhoneNumber(ubsUser.getSenderPhoneNumber())
            .totalUserViolations(user.getViolations())
            .build();
        when(ubsUserRepository.findUbsUserByOrderId(1L)).thenReturn(Optional.of(ubsUser));
        when(userRepository.checkIfUserHasViolationForCurrentOrder(1L, 1L))
            .thenReturn(expectedResult.getUserViolationForCurrentOrder());
        UserInfoDto actual = userService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, user.getUuid());

        verify(ubsUserRepository, times(1)).findUbsUserByOrderId(1L);
        verify(userRepository, times(1)).checkIfUserHasViolationForCurrentOrder(1L, 1L);

        assertEquals(expectedResult, actual);
    }

    @Test
    void getsUserAndUserUbsAndViolationsInfoByOrderIdWithoutSenderTest() {
        User user = getUser();
        UBSuser ubsUser = getUBSuser();
        ubsUser.setUser(user);
        ubsUser.setSenderFirstName(null);
        ubsUser.setSenderLastName(null);
        ubsUser.setSenderPhoneNumber(null);
        ubsUser.setSenderEmail(null);
        UserInfoDto expectedResult = UserInfoDto.builder()
            .customerName(ubsUser.getFirstName())
            .customerSurname(ubsUser.getLastName())
            .customerEmail(ubsUser.getEmail())
            .customerPhoneNumber(ubsUser.getPhoneNumber())
            .customerId(1L)
            .senderName(ubsUser.getFirstName())
            .senderSurname(ubsUser.getLastName())
            .senderEmail(ubsUser.getEmail())
            .senderPhoneNumber(ubsUser.getPhoneNumber())
            .totalUserViolations(user.getViolations())
            .build();
        when(ubsUserRepository.findUbsUserByOrderId(1L)).thenReturn(Optional.of(ubsUser));
        UserInfoDto actual = userService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, user.getUuid());

        verify(ubsUserRepository, times(1)).findUbsUserByOrderId(1L);
        verify(userRepository, times(1)).checkIfUserHasViolationForCurrentOrder(1L, 1L);

        assertEquals(expectedResult, actual);
    }

    @Test
    void getUserAndUserUbsAndViolationsInfoByOrderIdOrderNotFoundException() {
        assertThrows(NotFoundException.class,
            () -> userService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, "abc"));
    }

    @Test
    void getUserAndUserUbsAndViolationsInfoByOrderIdThrowsAnAccessDeniedExceptionForNonEqualUserUuidTest() {
        UBSuser ubSuser = getUBSuser();
        ubSuser.setUser(getUser());
        when(ubsUserRepository.findUbsUserByOrderId(1L)).thenReturn(Optional.of(ubSuser));

        assertThrows(AccessDeniedException.class,
            () -> userService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, "abc"));
        verify(ubsUserRepository, times(1)).findUbsUserByOrderId(1L);
    }

    @Test
    void updateUbsUserInfoInOrderThrowUBSuserNotFoundExceptionTest() {
        UbsCustomersDtoUpdate request = getUbsCustomersDtoUpdate();

        when(ubsUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UBSuserNotFoundException.class,
            () -> userService.updateUbsUserInfoInOrder(request, "abc"));
        verify(ubsUserRepository).findById(1L);
    }

    @Test
    void updateUbsUserInfoInOrderTest() {
        UbsCustomersDtoUpdate request = getUbsCustomer();
        Optional<UBSuser> ubsUserOptional = Optional.of(getUBSuser());
        UBSuser ubsUser = ubsUserOptional.get();
        User user = getUser();
        ubsUser.setUser(user);

        mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        doNothing().when(eventService).save(anyString(), anyString(), any());
        when(ubsUserRepository.findById(1L)).thenReturn(ubsUserOptional);
        when(ubsUserRepository.save(ubsUser)).thenReturn(ubsUser);

        UbsCustomersDto expected = UbsCustomersDto.builder()
            .name("Anatolii Anatolii")
            .email("anatolii.andr@gmail.com")
            .phoneNumber("095123456")
            .build();

        UbsCustomersDto actual = userService.updateUbsUserInfoInOrder(request, user.getUuid());
        assertEquals(expected, actual);

        verify(ubsUserRepository).findById(1L);
        verify(ubsUserRepository).save(ubsUserOptional.get());
        verify(eventService).save(anyString(), anyString(), any());

        mockedContextHolder.verify(SecurityContextHolder::getContext, atLeastOnce());
        verify(securityContext).getAuthentication();
        verify(authentication, times(2)).getAuthorities();
    }

    @Test
    void updateUbsUserInfoInOrderWithWrongAccessThrowsExceptionTest() {
        UbsCustomersDtoUpdate request = getUbsCustomer();

        Optional<UBSuser> ubsUserOptional = Optional.of(getUBSuser());
        UBSuser ubsUser = ubsUserOptional.get();
        User user = getUser();
        ubsUser.setUser(user);
        String userUuid = user.getUuid() + "test";

        mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority(USER_WITH_PREFIX)))
            .when(authentication).getAuthorities();
        when(ubsUserRepository.findById(1L)).thenReturn(ubsUserOptional);

        assertThrows(AccessDeniedException.class,
            () -> userService.updateUbsUserInfoInOrder(request, userUuid));

        verify(ubsUserRepository).findById(1L);

        mockedContextHolder.verify(SecurityContextHolder::getContext, atLeastOnce());
        verify(securityContext).getAuthentication();
        verify(authentication).getAuthorities();
    }

    @Test
    void updateUbsUserInfoInOrder_asAdmin_triggersElseBranch() {
        UbsCustomersDtoUpdate request = getUbsCustomer();
        UBSuser ubsUser = getUBSuser();
        ubsUser.setUser(getUser());

        mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_UBS_EMPLOYEE")))
            .when(authentication).getAuthorities();

        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubsUser));
        when(ubsUserRepository.save(ubsUser)).thenReturn(ubsUser);

        userService.updateUbsUserInfoInOrder(request, ubsUser.getUser().getUuid());

        verify(eventService).save(OrderHistory.CHANGED_SENDER_UK, OrderHistory.UBS_ADMIN,
            ubsUser.getOrders().getFirst());
        verify(ubsUserRepository).save(ubsUser);
    }

    @Test
    void updateRecipientDataInOrder_withAllNullFields_doesNotChangeUser() {
        UbsCustomersDtoUpdate request = new UbsCustomersDtoUpdate();
        request.setCustomerId(1L);
        UBSuser ubsUser = getUBSuser();
        UBSuser originalCopy = new UBSuser();
        originalCopy.setSenderEmail(ubsUser.getSenderEmail());
        originalCopy.setSenderFirstName(ubsUser.getSenderFirstName());
        originalCopy.setSenderLastName(ubsUser.getSenderLastName());
        originalCopy.setSenderPhoneNumber(ubsUser.getSenderPhoneNumber());
        ubsUser.setUser(getUser());

        mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubsUser));
        when(ubsUserRepository.save(ubsUser)).thenReturn(ubsUser);

        userService.updateUbsUserInfoInOrder(request, ubsUser.getUser().getUuid());

        assertEquals(originalCopy.getSenderEmail(), ubsUser.getSenderEmail());
        assertEquals(originalCopy.getSenderFirstName(), ubsUser.getSenderFirstName());
        assertEquals(originalCopy.getSenderLastName(), ubsUser.getSenderLastName());
        assertEquals(originalCopy.getSenderPhoneNumber(), ubsUser.getSenderPhoneNumber());
    }

    @Test
    void testCreateUserProfileIfProfileDoesNotExist() {
        UserProfileCreateDto userProfileCreateDto = getUserProfileCreateDto();
        User userForSave = User.builder()
            .uuid(userProfileCreateDto.getUuid())
            .recipientEmail(userProfileCreateDto.getEmail())
            .recipientName(userProfileCreateDto.getName())
            .status(UserStatus.ACTIVATED)
            .currentPoints(0)
            .violations(0)
            .dateOfRegistration(LocalDate.now()).build();
        User user = getUser();
        when(userRemoteClient.checkIfUserExistsByUuid(userProfileCreateDto.getUuid())).thenReturn(true);
        when(userRepository.findByUuid(userProfileCreateDto.getUuid())).thenReturn(null);
        when(userRepository.save(userForSave)).thenReturn(user);
        Long actualId = userService.createUserProfile(userProfileCreateDto);
        verify(userRepository, times(1)).findByUuid(userProfileCreateDto.getUuid());
        verify(userRepository, times(1)).save(userForSave);
        assertEquals(user.getId(), actualId);
    }

    @Test
    void testCreateUserProfileIfProfileExists() {
        UserProfileCreateDto userProfileCreateDto = getUserProfileCreateDto();
        User user = getUser();
        when(userRemoteClient.checkIfUserExistsByUuid(userProfileCreateDto.getUuid())).thenReturn(true);
        when(userRepository.findByUuid(userProfileCreateDto.getUuid())).thenReturn(user);
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> userService.createUserProfile(userProfileCreateDto));
        assertEquals(USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS, ex.getMessage());
        verify(userRemoteClient, times(1)).checkIfUserExistsByUuid(userProfileCreateDto.getUuid());
        verify(userRepository, times(1)).findByUuid(userProfileCreateDto.getUuid());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void testCreateUserProfileIfUserByUuidDoesNotExist() {
        UserProfileCreateDto userProfileCreateDto = getUserProfileCreateDto();
        when(userRemoteClient.checkIfUserExistsByUuid(userProfileCreateDto.getUuid())).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.createUserProfile(userProfileCreateDto));
    }

    @Test
    void updateProfileData() {
        UserServiceImpl userServiceImplSpy = spy(userService);

        User user = getUserWithBotNotifyTrue();
        TelegramChat telegramBot = getTelegramBotNotifyTrue();
        List<AddressDto> addressDto = addressDtoList();
        List<Bot> botList = botList();
        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDto();
        String uuid = UUID.randomUUID().toString();
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(telegramChatRepository.findByUser(user)).thenReturn(Optional.of(telegramBot));
        when(modelMapper.map(addressDto.get(0), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        when(modelMapper.map(addressDto.get(1), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        doReturn(new OrderWithAddressesResponseDto())
            .when(addressService).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        userServiceImplSpy.updateProfileData(uuid, userProfileUpdateDto);

        for (Bot bot : botList) {
            Assertions.assertNotNull(bot);
        }
        Assertions.assertNotNull(userProfileUpdateDto.getAddressDto());
        Assertions.assertNotNull(userProfileUpdateDto);
        Assertions.assertNotNull(addressDto);
        Assertions.assertTrue(userProfileUpdateDto.getTelegramIsNotify());

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramChatRepository).findByUser(user);
        verify(modelMapper).map(addressDto.get(0), OrderAddressDtoRequest.class);
        verify(modelMapper).map(addressDto.get(1), OrderAddressDtoRequest.class);
        verify(addressService, times(2)).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        verify(userRepository).save(user);
        verify(modelMapper).map(user, UserProfileUpdateDto.class);
    }

    @Test
    void updateProfileDataThrowNotFoundException() {
        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDto();
        String uuid = UUID.randomUUID().toString();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> userService.updateProfileData(uuid, userProfileUpdateDto));
        verify(userRepository).findUserByUuid(uuid);

    }

    @Test
    void updateProfileDataWhenAddressPlaceIdIsNull() {
        UserServiceImpl userServiceImplSpy = spy(userService);

        User user = getUserWithBotNotifyTrue();
        TelegramChat telegramBot = getTelegramBotNotifyTrue();
        List<AddressDto> addressDto = addressDtoListWithNullPlaceId();

        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDto();
        userProfileUpdateDto.getAddressDto().get(0).setPlaceId(null);
        userProfileUpdateDto.getAddressDto().get(1).setPlaceId(null);

        String uuid = UUID.randomUUID().toString();
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequestWithNullPlaceId();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(telegramChatRepository.findByUser(user)).thenReturn(Optional.of(telegramBot));
        when(modelMapper.map(addressDto.get(0), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        when(modelMapper.map(addressDto.get(1), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        doReturn(new OrderWithAddressesResponseDto()).when(addressService)
            .updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        userServiceImplSpy.updateProfileData(uuid, userProfileUpdateDto);

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramChatRepository).findByUser(user);
        verify(modelMapper).map(addressDto.get(0), OrderAddressDtoRequest.class);
        verify(modelMapper).map(addressDto.get(1), OrderAddressDtoRequest.class);
        verify(addressService, times(2)).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        verify(userRepository).save(user);
        verify(modelMapper).map(user, UserProfileUpdateDto.class);
    }

    @Test
    void updateProfileDataIfTelegramBotNotExists() {
        UserServiceImpl userServiceImplSpy = spy(userService);

        User user = getUserWithBotNotifyTrue();
        List<AddressDto> addressDto = addressDtoList();
        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDtoWithBotsIsNotifyFalse();
        String uuid = UUID.randomUUID().toString();
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(telegramChatRepository.findByUser(user)).thenReturn(Optional.empty());
        when(modelMapper.map(addressDto.get(0), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        when(modelMapper.map(addressDto.get(1), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        doReturn(new OrderWithAddressesResponseDto())
            .when(addressService).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        userServiceImplSpy.updateProfileData(uuid, userProfileUpdateDto);

        assertFalse(userProfileUpdateDto.getTelegramIsNotify());

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramChatRepository).findByUser(user);
        verify(modelMapper).map(addressDto.get(0), OrderAddressDtoRequest.class);
        verify(modelMapper).map(addressDto.get(1), OrderAddressDtoRequest.class);
        verify(addressService, times(2)).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        verify(userRepository).save(user);
        verify(modelMapper).map(user, UserProfileUpdateDto.class);
    }

    @Test
    void getProfileData() {
        User user = getUser();
        String uuid = UUID.randomUUID().toString();
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        UserProfileDto userProfileDto = new UserProfileDto();
        List<AddressDto> addressDto = addressDtoList();
        userProfileDto.setAddressDto(addressDto);
        List<Address> address = addressList();
        List<Bot> botList = botList();
        userProfileDto.setBotList(botList);
        when(modelMapper.map(user, UserProfileDto.class)).thenReturn(userProfileDto);
        when(userRemoteClient.getPasswordStatus()).thenReturn(new PasswordStatusDto(true));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(address);
        assertEquals(userProfileDto, userService.getProfileData(uuid));
        for (Bot bot : botList) {
            Assertions.assertNotNull(bot);
        }
        Assertions.assertNotNull(addressDto);
        Assertions.assertNotNull(userProfileDto);
        Assertions.assertNotNull(address);
    }

    @Test
    void getProfileDataNotFoundException() {
        String uuid = UUID.randomUUID().toString();
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userService.getProfileData(uuid));
        verify(userRepository).findUserByUuid(uuid);
    }

    @Test
    void getUserPointTest() {
        when(userRepository.findByUuid("uuid")).thenReturn(User.builder().id(1L).currentPoints(100).build());

        userService.getUserPoint("uuid");

        verify(userRepository).findByUuid("uuid");
    }

    @Test
    void getPositionsAndRelatedAuthoritiesTest() {
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.ofNullable(getEmployee()));
        when(userRemoteClient.getPositionsAndRelatedAuthorities(TEST_EMAIL))
            .thenReturn(getPositionAuthoritiesDto());

        PositionAuthoritiesDto actual = userService.getPositionsAndRelatedAuthorities(TEST_EMAIL);
        assertEquals(getPositionAuthoritiesDto(), actual);

        verify(employeeRepository).findByEmail(TEST_EMAIL);
        verify(userRemoteClient).getPositionsAndRelatedAuthorities(TEST_EMAIL);
    }

    @Test
    void getPositionsAndRelatedAuthoritiesThrowsNotFoundExceptionTest() {
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getPositionsAndRelatedAuthorities(TEST_EMAIL));
        verify(employeeRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void getAllAuthorities() {
        Set<String> authorities = new HashSet<>();
        when(userRemoteClient.getAllAuthorities(anyString())).thenReturn(authorities);
        userRemoteClient.getAllAuthorities("test@mail.com");
        verify(userRemoteClient, times(1)).getAllAuthorities("test@mail.com");
    }

    @Test
    void getAllAuthoritiesService() {
        Optional<Employee> employeeOptional = Optional.ofNullable(getEmployee());
        when(employeeRepository.findByEmail(anyString())).thenReturn(employeeOptional);
        Employee employee = employeeOptional.orElseThrow(() -> new IllegalStateException("Employee not found"));
        when(userRemoteClient.getAllAuthorities(employee.getEmail()))
            .thenReturn(Set.copyOf(ModelUtils.getAllAuthorities()));
        Set<String> authoritiesResult = userService.getAllAuthorities(employeeOptional.get().getEmail());
        Set<String> authExpected = Set.of("SEE_CLIENTS_PAGE");
        assertEquals(authExpected, authoritiesResult);

        verify(employeeRepository, times(1)).findByEmail(anyString());
        verify(userRemoteClient, times(1)).getAllAuthorities(any());
    }

    @Test
    void updateEmployeesAuthorities() {
        UserEmployeeAuthorityDto dto = ModelUtils.getUserEmployeeAuthorityDto();
        doNothing().when(userRemoteClient).updateEmployeesAuthorities(dto);
        userService.updateEmployeesAuthorities(dto);
        verify(userRemoteClient, times(1)).updateEmployeesAuthorities(dto);
    }

    @Test
    void getUserStatusByUuid_ShouldReturnUserStatus_WhenUserExists() {
        String uuid = "test-uuid-123";
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(testUser));

        UserStatus result = userService.getUserStatusByUuid(uuid);

        assertEquals(UserStatus.ACTIVATED, result);
        verify(userRepository).findUserByUuid(uuid);
    }

    @Test
    void getUserStatusByUuid_ShouldThrowNotFoundException_WhenUserNotExists() {
        String uuid = "non-existent-uuid";
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.getUserStatusByUuid(uuid));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_UUID + uuid, exception.getMessage());
        verify(userRepository).findUserByUuid(uuid);
    }

    @Test
    void deleteUserByUuid_ShouldSetStatusToDeleted_WhenUserIsActivated() {
        String uuid = "test-uuid-123";
        testUser.setStatus(UserStatus.ACTIVATED);
        UserDeletionReasonDto dto = UserDeletionReasonDto.builder()
            .reason("test reason")
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(testUser));

        userService.deleteUserByUuid(uuid, dto);

        assertEquals(UserStatus.DELETED, testUser.getStatus());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUserByUuid_ShouldThrowForbiddenException_WhenUserIsDeactivated() {
        String uuid = "test-uuid-123";
        testUser.setStatus(UserStatus.DEACTIVATED);
        UserDeletionReasonDto dto = UserDeletionReasonDto.builder()
            .reason("test reason")
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(testUser));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userService.deleteUserByUuid(uuid, dto));

        assertEquals(ErrorMessage.FORBIDDEN_USER_DELETION, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserByUuid_ShouldThrowForbiddenException_WhenUserIsBlocked() {
        String uuid = "test-uuid-123";
        testUser.setStatus(UserStatus.BLOCKED);
        UserDeletionReasonDto dto = UserDeletionReasonDto.builder()
            .reason("test reason")
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(testUser));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userService.deleteUserByUuid(uuid, dto));

        assertEquals(ErrorMessage.FORBIDDEN_USER_DELETION, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserByUuid_ShouldThrowNotFoundException_WhenUserNotExists() {
        String uuid = "non-existent-uuid";
        UserDeletionReasonDto dto = UserDeletionReasonDto.builder()
            .reason("test reason")
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.deleteUserByUuid(uuid, dto));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_UUID + uuid, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatusById_ShouldDeactivateUser_WhenStatusIsDeactivated() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.DEACTIVATED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(userRemoteClient.findByUuid(currentUserUuid)).thenReturn(currentUserDto);
        when(userRemoteClient.findByUuid(testUser.getUuid())).thenReturn(targetUserDto);
        when(userRemoteClient.findUserLanguageByUuid(testUser.getUuid())).thenReturn("en");

        userService.updateUserStatusById(currentUserUuid, targetUserId, status);

        assertEquals(UserStatus.DEACTIVATED, testUser.getStatus());
        verify(userRepository).save(testUser);

        ArgumentCaptor<UserDeactivationReason> deactivationCaptor =
            ArgumentCaptor.forClass(UserDeactivationReason.class);
        verify(userDeactivationRepo).save(deactivationCaptor.capture());

        UserDeactivationReason savedReason = deactivationCaptor.getValue();
        assertTrue(savedReason.getReason().contains("Current User"));
        assertTrue(savedReason.getReason().contains(currentUserUuid));
        assertEquals(testUser, savedReason.getUser());

        ArgumentCaptor<UserDeactivationReasonDto> notificationCaptor =
            ArgumentCaptor.forClass(UserDeactivationReasonDto.class);
        verify(userRemoteClient).sendReasonOfDeactivation(notificationCaptor.capture());

        UserDeactivationReasonDto notification = notificationCaptor.getValue();
        assertEquals(testUser.getRecipientEmail(), notification.getEmail());
        assertEquals(testUser.getRecipientName(), notification.getName());
        assertEquals("en", notification.getLang());
    }

    @Test
    void updateUserStatusById_ShouldThrowException_WhenAdminTriesToDeactivateAdmin() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.DEACTIVATED;

        targetUserDto.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(userRemoteClient.findByUuid(currentUserUuid)).thenReturn(currentUserDto);
        when(userRemoteClient.findByUuid(testUser.getUuid())).thenReturn(targetUserDto);

        UserStatusUpdateException exception = assertThrows(UserStatusUpdateException.class,
            () -> userService.updateUserStatusById(currentUserUuid, targetUserId, status));

        assertEquals(ErrorMessage.ADMIN_CANNOT_DEACTIVATE_OTHER_ADMIN, exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(userDeactivationRepo, never()).save(any());
    }

    @Test
    void updateUserStatusById_ShouldActivateUser_WhenStatusIsActivated() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.ACTIVATED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(userRemoteClient.findUserLanguageByUuid(testUser.getUuid())).thenReturn("uk");

        userService.updateUserStatusById(currentUserUuid, targetUserId, status);

        assertEquals(UserStatus.ACTIVATED, testUser.getStatus());
        verify(userRepository).save(testUser);

        ArgumentCaptor<UserActivationDto> activationCaptor = ArgumentCaptor.forClass(UserActivationDto.class);
        verify(userRemoteClient).sendMessageOfActivation(activationCaptor.capture());

        UserActivationDto notification = activationCaptor.getValue();
        assertEquals(testUser.getRecipientEmail(), notification.getEmail());
        assertEquals(testUser.getRecipientName(), notification.getName());
        assertEquals("uk", notification.getLang());
    }

    @Test
    void updateUserStatusById_ShouldThrowException_WhenUserTriesToUpdateThemselves() {
        String currentUserUuid = "test-uuid-123";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.DEACTIVATED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));

        UserStatusUpdateException exception = assertThrows(UserStatusUpdateException.class,
            () -> userService.updateUserStatusById(currentUserUuid, targetUserId, status));

        assertEquals(ErrorMessage.USER_CANNOT_DEACTIVATE_YOURSELF, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatusById_ShouldThrowNotFoundException_WhenTargetUserNotExists() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 999L;
        UserStatus status = UserStatus.ACTIVATED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.updateUserStatusById(currentUserUuid, targetUserId, status));

        assertEquals(ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST + targetUserId, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatusById_ShouldHandleDefaultStatus_WhenStatusIsOther() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.BLOCKED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));

        userService.updateUserStatusById(currentUserUuid, targetUserId, status);

        assertEquals(UserStatus.BLOCKED, testUser.getStatus());
        verify(userRepository).save(testUser);
        verify(userRemoteClient, never()).sendReasonOfDeactivation(any());
        verify(userRemoteClient, never()).sendMessageOfActivation(any());
    }

    @Test
    void getDeactivationReasons_ShouldReturnEnglishReasons_WhenLangIsEn() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Reason 1 {en} / Reason 2 {uk} / Another reason {en}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReason(userId))
            .thenReturn(Optional.of(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("en");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertEquals(2, result.size());
        assertTrue(result.contains("Reason 1"));
        assertTrue(result.contains("Another reason"));
        assertFalse(result.get(0).contains("{en}"));
        assertFalse(result.get(1).contains("{en}"));
    }

    @Test
    void getDeactivationReasons_ShouldReturnUkrainianReasons_WhenLangIsUk() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Reason 1 {en} / Reason 2 {uk} / Another reason {uk}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReason(userId))
            .thenReturn(Optional.of(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("uk");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertEquals(2, result.size());
        assertTrue(result.contains("Reason 2"));
        assertTrue(result.contains("Another reason"));
        assertFalse(result.get(0).contains("{uk}"));
        assertFalse(result.get(1).contains("{uk}"));
    }

    @Test
    void getDeactivationReasons_ShouldThrowNotFoundException_WhenNoReasonsFound() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";

        when(userDeactivationRepo.getLastDeactivationReason(userId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.getDeactivationReasons(userId, currentUserUuid));

        assertEquals(ErrorMessage.USER_DEACTIVATION_REASON_IS_EMPTY, exception.getMessage());
    }

    @Test
    void getDeactivationReasons_ShouldHandleUkLanguageCorrectly() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Reason 1 {uk}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReason(userId))
            .thenReturn(Optional.of(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("uk");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertEquals(1, result.size());
        assertEquals("Reason 1", result.getFirst());
    }

    @Test
    void getActivatedUsersAmount_ShouldReturnCorrectCount() {
        long expectedCount = 42L;
        when(userRepository.countAllByStatus(UserStatus.ACTIVATED)).thenReturn(expectedCount);

        long result = userService.getActivatedUsersAmount();

        assertEquals(expectedCount, result);
        verify(userRepository).countAllByStatus(UserStatus.ACTIVATED);
    }

    @Test
    void getActivatedUsersAmount_ShouldReturnZero_WhenNoActivatedUsers() {
        when(userRepository.countAllByStatus(UserStatus.ACTIVATED)).thenReturn(0L);

        long result = userService.getActivatedUsersAmount();

        assertEquals(0L, result);
        verify(userRepository).countAllByStatus(UserStatus.ACTIVATED);
    }

    @Test
    void getDeactivationReasons_ShouldReturnEmptyList_WhenNoMatchingLanguageReasons() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Reason 1 {fr} / Reason 2 {de}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReason(userId))
            .thenReturn(Optional.of(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("en");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getDeactivationReasons_ShouldHandleSingleReason_WithoutSlash() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Single reason {en}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReason(userId))
            .thenReturn(Optional.of(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("en");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertEquals(1, result.size());
        assertEquals("Single reason", result.getFirst());
    }
}