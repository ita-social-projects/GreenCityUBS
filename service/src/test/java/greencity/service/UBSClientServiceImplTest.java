package greencity.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import com.liqpay.LiqPay;
import greencity.ModelUtils;
import greencity.client.FondyClient;
import greencity.client.UserRemoteClient;
import greencity.config.GoogleApiConfiguration;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.*;
import greencity.entity.order.*;
import greencity.entity.user.LocationTranslation;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.service.ubs.EventService;
import greencity.service.ubs.LiqPayService;
import greencity.service.ubs.UBSClientServiceImpl;
import greencity.service.ubs.UBSManagementService;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;
import org.bouncycastle.math.raw.Mod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ModuleUtils;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static greencity.ModelUtils.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@RunWith(PowerMockRunner.class)
@PrepareForTest({GeocodingApi.class, GeocodingApiRequest.class})
class UBSClientServiceImplTest {
    @Mock
    private BagTranslationRepository bagTranslationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BagRepository bagRepository;
    @Mock
    private UBSuserRepository ubsUserRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    private LiqPayService liqPayService;
    @Mock
    private UserRemoteClient userRemoteClient;
    @Mock
    private FondyClient fondyClient;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    UBSClientServiceImpl ubsService;
    @Mock
    EncryptionUtil encryptionUtil;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PhoneNumberFormatterService phoneNumberFormatterService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private LiqPay liqPay;
    @Mock
    private EventService eventService;
    @Mock
    private OrdersForUserRepository ordersForUserRepository;
    @Mock
    private LocationTranslationRepository locationTranslationRepository;
    @Mock
    private CourierRepository courierRepository;
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private UBSManagementService ubsManagementService;
    @Mock
    private CourierLocationRepository courierLocationRepository;
    @Mock
    private OrderStatusTranslationRepository orderStatusTranslationRepository;
    @Mock
    private OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    @Mock
    private GoogleApiService googleApiService;


    @Test
    @Transactional
    void testValidatePayment() {
        PaymentResponseDto dto = new PaymentResponseDto();
        Order order = getOrder();
        dto.setOrder_id(order.getId().toString());
        dto.setResponse_status("approved");
        dto.setOrder_status("approved");
        dto.setAmount(95000);
        dto.setPayment_id(1);
        dto.setCurrency("UAH");
        dto.setSettlement_date("");
        dto.setFee(0);
        Payment payment = getPayment();
        when(encryptionUtil.checkIfResponseSignatureIsValid(dto, null)).thenReturn(true);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        ubsService.validatePayment(dto);
        verify(eventService, times(1))
            .save("Замовлення Оплачено", "Система", order);
        verify(paymentRepository, times(1)).save(payment);

    }

    @Test
    void unvalidValidatePayment() {
        PaymentResponseDto dto = new PaymentResponseDto();
        Order order = getOrder();
        dto.setOrder_id(order.getId().toString());
        dto.setResponse_status("approved");
        dto.setOrder_status("approved");
        dto.setAmount(95000);
        dto.setPayment_id(1);
        dto.setCurrency("UAH");
        dto.setSettlement_date("");
        dto.setFee(0);
        lenient().when(encryptionUtil.checkIfResponseSignatureIsValid(dto, null)).thenReturn(false);
        assertThrows(PaymentValidationException.class, () -> ubsService.validatePayment(dto));
    }

    @Test
    void getFirstPageData() {
        UserPointsAndAllBagsDto userPointsAndAllBagsDtoExpected =
            new UserPointsAndAllBagsDto(new ArrayList<BagTranslationDto>(), 600);

        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(600);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);

        UserPointsAndAllBagsDto userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageData("35467585763t4sfgchjfuyetf");

        assertEquals(userPointsAndAllBagsDtoExpected.getBags(), userPointsAndAllBagsDtoActual.getBags());
        assertEquals(userPointsAndAllBagsDtoExpected.getPoints(), userPointsAndAllBagsDtoActual.getPoints());
    }

    @Test
    void testSaveToDB() throws InvocationTargetException, IllegalAccessException {

        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        Address address = ubSuser.getAddress();
        address.setUser(user);
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<Payment>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        Field merchantId = null;
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, null))
            .thenReturn(ModelUtils.getCourierLocations());
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());

        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
        assertNotNull(result);

    }

    @Test
    void testSaveToDBThrowsException() throws InvocationTargetException, IllegalAccessException {
        Service service = new Service();
        Courier courier = new Courier();
        LocationStatus locationStatus = LocationStatus.ACTIVE;
        Bag bags = new Bag();
        LocationTranslation locationTranslation = new LocationTranslation();
        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(35);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        Address address = ubSuser.getAddress();
        address.setUser(user);
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<Payment>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        Field merchantId = null;
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, null))
            .thenReturn(ModelUtils.getCourierLocations());
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        Assertions.assertThrows(NotEnoughBagsException.class, () -> {
            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
        });
    }

    @Test
    void getSecondPageData() {
        String uuid = "35467585763t4sfgchjfuyetf";
        PersonalDataDto expected = ModelUtils.getOrderResponseDto().getPersonalData();

        User user = User.builder()
            .uuid(uuid)
            .recipientName("oleh")
            .recipientSurname("ivanov")
            .id(1L).recipientEmail("mail@mail.ua")
            .recipientPhone("067894522")
            .build();
        List<UBSuser> ubsUser = List.of(UBSuser.builder()
            .user(user)
            .id(1l)
            .build());
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(ubsUser);
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(expected);
        PersonalDataDto actual = ubsService.getSecondPageData("35467585763t4sfgchjfuyetf");

        assertEquals(expected, actual);
    }

    @Test
    void checkCertificate() {
        when(certificateRepository.findById("certificate")).thenReturn(Optional.of(Certificate.builder()
            .code("certificate")
            .certificateStatus(CertificateStatus.ACTIVE)
            .build()));

        assertEquals("ACTIVE", ubsService.checkCertificate("certificate").getCertificateStatus());
    }

    @Test
    void checkCertificateWithNoAvailable() {
        Assertions.assertThrows(CertificateNotFoundException.class, () -> {
            ubsService.checkCertificate("randomstring");
        });
    }

    @Test
    void getAllOrdersDoneByUser() {
        Order order = getOrderDoneByUser();
        OrderClientDto dto = getOrderClientDto();
        List<Order> orderList = Collections.singletonList(order);
        List<OrderClientDto> expected = Collections.singletonList(dto);

        when(orderRepository.getAllOrdersOfUser(any())).thenReturn(orderList);
        when(modelMapper.map(order, OrderClientDto.class)).thenReturn(dto);

        List<OrderClientDto> result = ubsService.getAllOrdersDoneByUser(anyString());

        assertEquals(expected, result);
    }

    @Test
    void makeOrderAgain() {
        MakeOrderAgainDto dto = MakeOrderAgainDto.builder()
            .orderId(1L)
            .orderAmount(350L)
            .bagOrderDtoList(List.of(getBagOrderDto()))
            .build();
        Order order = getOrderDoneByUser();
        order.setAmountOfBagsOrdered(Collections.singletonMap(1, 1));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(bagTranslationRepository.findAllByOrder(1L))
            .thenReturn(List.of(getBagTranslation()));

        MakeOrderAgainDto result = ubsService.makeOrderAgain(new Locale("en"), 1L);

        assertEquals(dto, result);
        verify(orderRepository, times(1)).findById(1L);
        verify(bagTranslationRepository, times(1)).findAllByOrder(1L);
    }

    @Test
    void makeOrderAgainShouldThrowOrderNotFoundException() {
        Locale locale = new Locale("en");
        Exception thrown = assertThrows(OrderNotFoundException.class,
            () -> ubsService.makeOrderAgain(locale, 1L));
        assertEquals(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    void makeOrderAgainShouldThrowBadOrderStatusException() {
        Order order = getOrderDoneByUser();
        order.setOrderStatus(OrderStatus.CANCELED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Locale locale = new Locale("en");
        Exception thrown = assertThrows(BadOrderStatusRequestException.class,
            () -> ubsService.makeOrderAgain(locale, 1L));
        assertEquals(thrown.getMessage(), ErrorMessage.BAD_ORDER_STATUS_REQUEST
            + order.getOrderStatus());
    }

    @Test
    void findUserByUuid() {
        String uuid = "87df9ad5-6393-441f-8423-8b2e770b01a8";
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(ModelUtils.getUser()));
        ubsService.findAllCurrentPointsForUser(uuid);
        verify(userRepository).findUserByUuid(uuid);
    }

    @Test
    void findUserNotFoundException() {
        Exception thrown = assertThrows(UserNotFoundException.class,
            () -> ubsService.findAllCurrentPointsForUser("87df9ad5-6393-441f-8423-8b2e770b01a8"));
        assertEquals(ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    void markUserAsDeactivatedByIdThrowsNotFoundException() {
        Exception thrown = assertThrows(NotFoundException.class,
            () -> ubsService.markUserAsDeactivated(1L));
        assertEquals(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    void markUserAsDeactivatedById() {
        User user = ModelUtils.getUser();
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        ubsService.markUserAsDeactivated(1L);
        verify(userRepository).findById(1L);
        verify(userRemoteClient).markUserDeactivated(user.getUuid());
    }

    @Test
    void getsUserAndUserUbsAndViolationsInfoByOrderId() {
        UserInfoDto expectedResult = ModelUtils.getUserInfoDto();
        expectedResult.setRecipientId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrderDetails()));
        when(userRepository.findByUuid(anyString())).thenReturn(getOrderDetails().getUser());
        when(userRepository.countTotalUsersViolations(1L)).thenReturn(expectedResult.getTotalUserViolations());
        when(userRepository.checkIfUserHasViolationForCurrentOrder(1L, 1L))
            .thenReturn(expectedResult.getUserViolationForCurrentOrder());
        UserInfoDto actual = ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, anyString());

        verify(orderRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).countTotalUsersViolations(1L);
        verify(userRepository, times(1)).checkIfUserHasViolationForCurrentOrder(1L, 1L);

        assertEquals(expectedResult, actual);
    }

    @Test
    void getUserAndUserUbsAndViolationsInfoByOrderIdOrderNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class,
            () -> ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, "abc"));
    }

    @Test
    void getUserAndUserUbsAndViolationsInfoByOrderIdAccessDeniedException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrder()));
        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());
        assertThrows(AccessDeniedException.class,
            () -> ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, "abc"));
    }

    @Test
    void updatesUbsUserInfoInOrderShouldThrowUBSuserNotFoundException() {
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
            .recipientId(1l)
            .recipientName("Anatolii Petyrov")
            .recipientEmail("anatolii.andr@gmail.com")
            .recipientPhoneNumber("095123456").build();

        when(ubsUserRepository.findById(1L))
            .thenThrow(UBSuserNotFoundException.class);
        assertThrows(UBSuserNotFoundException.class,
            () -> ubsService.updateUbsUserInfoInOrder(request, "abc"));
    }

    @Test
    void updatesUbsUserInfoInOrder() {
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
            .recipientId(1l)
            .recipientName("Anatolii")
            .recipientSurName("Anatolii")
            .recipientEmail("anatolii.andr@gmail.com")
            .recipientPhoneNumber("095123456").build();

        Optional<UBSuser> user = Optional.of(ModelUtils.getUBSuser());
        when(ubsUserRepository.findById(1L)).thenReturn(user);
        when(ubsUserRepository.save(user.get())).thenReturn(user.get());

        UbsCustomersDto expected = UbsCustomersDto.builder()
            .name("Anatolii Anatolii")
            .email("anatolii.andr@gmail.com")
            .phoneNumber("095123456")
            .build();

        UbsCustomersDto actual = ubsService.updateUbsUserInfoInOrder(request, "abc");
        assertEquals(expected, actual);
    }

    @Test
    void saveProfileData() {
        User user = ModelUtils.getUser();

        when(userRepository.findByUuid("87df9ad5-6393-441f-8423-8b2e770b01a8")).thenReturn(user);

        List<AddressDto> addressDto = ModelUtils.addressDtoList();
        List<Address> address = ModelUtils.addressList();
        List<Bot> botList = ModelUtils.botList();

        UserProfileUpdateDto userProfileUpdateDto =
            UserProfileUpdateDto.builder().addressDto(addressDto)
                .recipientName(user.getRecipientName()).recipientSurname(user.getRecipientSurname())
                .recipientPhone(user.getRecipientPhone())
                .build();

        when(modelMapper.map(addressDto.get(0), Address.class)).thenReturn(address.get(0));
        when(modelMapper.map(addressDto.get(1), Address.class)).thenReturn(address.get(1));
        when(userRepository.save(user)).thenReturn(user);
        for (Address address1 : address) {
            when(addressRepository.save(address1)).thenReturn(address1);
        }
        when(modelMapper.map(address.get(0), AddressDto.class)).thenReturn(addressDto.get(0));
        when(modelMapper.map(address.get(1), AddressDto.class)).thenReturn(addressDto.get(1));
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);
        ubsService.updateProfileData("87df9ad5-6393-441f-8423-8b2e770b01a8", userProfileUpdateDto);
        for (Bot bot : botList) {
            assertNotNull(bot);
        }
        assertNotNull(userProfileUpdateDto.getAddressDto());
        assertNotNull(userProfileUpdateDto);
        assertNotNull(address);
    }

    @Test
    void getProfileData() {
        User user = ModelUtils.getUser();
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        UserProfileDto userProfileDto = new UserProfileDto();
        List<AddressDto> addressDto = ModelUtils.addressDtoList();
        userProfileDto.setAddressDto(addressDto);
        List<Address> address = ModelUtils.addressList();
        List<Bot> botList = ModelUtils.botList();
        userProfileDto.setBotList(botList);
        when(modelMapper.map(user, UserProfileDto.class)).thenReturn(userProfileDto);
        assertEquals(userProfileDto, ubsService.getProfileData(user.getUuid()));
        for (Bot bot : botList) {
            assertNotNull(bot);
        }
        assertNotNull(addressDto);
        assertNotNull(userProfileDto);
        assertNotNull(address);
    }

    @Test
    void testFindAllAddressesForCurrentOrder() {
        String uuid = "35467585763t4sfgchjfuyetf";
        User user = new User();
        user.setId(13L);
        when(userRepository.findByUuid(uuid)).thenReturn(user);

        List<AddressDto> testAddressesDto = getTestAddressesDto();

        OrderWithAddressesResponseDto expected = new OrderWithAddressesResponseDto(testAddressesDto);

        List<Address> addresses = getTestAddresses(user);

        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
        when(modelMapper.map(addresses.get(0), AddressDto.class)).thenReturn(testAddressesDto.get(0));
        when(modelMapper.map(addresses.get(1), AddressDto.class)).thenReturn(testAddressesDto.get(1));

        OrderWithAddressesResponseDto actual = ubsService.findAllAddressesForCurrentOrder(uuid);

        assertEquals(actual, expected);
        verify(userRepository, times(2)).findByUuid(uuid);
        verify(addressRepository, times(1)).findAllByUserId(user.getId());
    }

    private List<Address> getTestAddresses(User user) {
        Address address1 = Address.builder()
            .addressStatus(AddressStatus.NEW).id(13L).city("Kyiv").district("Svyatoshyn")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Peremohy av.")
            .user(user).actual(true).coordinates(new Coordinates(12.5, 34.5)).ubsUsers(new LinkedList<>())
            .build();

        Address address2 = Address.builder()
            .addressStatus(AddressStatus.NEW).id(42L).city("Lviv").district("Syhiv")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Lvivska st.")
            .user(user).actual(true).coordinates(new Coordinates(13.5, 36.5)).ubsUsers(new LinkedList<>())
            .build();

        return Arrays.asList(address1, address2);
    }

    private List<AddressDto> getTestAddressesDto() {
        AddressDto addressDto1 = AddressDto.builder().actual(true).id(13L).city("Kyiv").district("Svyatoshyn")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Peremohy av.")
            .coordinates(new Coordinates(12.5, 34.5)).build();

        AddressDto addressDto2 = AddressDto.builder().actual(true).id(42L).city("Lviv").district("Syhiv")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Lvivska st.")
            .coordinates(new Coordinates(13.5, 36.5)).build();
        return Arrays.asList(addressDto1, addressDto2);
    }

    @Test
    void testSaveCurrentAddressForOrder() {
        User user = ModelUtils.getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = ModelUtils.getTestOrderAddressDtoRequest();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode("fake address")).thenReturn(ModelUtils.getGeocodingResult());
        when(modelMapper.map(any(),
                eq(OrderAddressDtoRequest.class)))
                .thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);

        addresses.get(0).setActual(false);

        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();

        when(addressRepository.save(addresses.get(0))).thenReturn(addresses.get(0));
        when(modelMapper.map(dtoRequest,
                Address.class)).thenReturn(new Address());

        addresses.get(0).setAddressStatus(AddressStatus.NEW);

        ubsService.saveCurrentAddressForOrder(createAddressRequestDto, uuid);

        verify(addressRepository, times(1)).save(addresses.get(0));
    }

    @Test
    void testSaveCurrentAddressForOrderAlreadyExistException() {
        User user = ModelUtils.getUserForCreate();

        List<Address> addresses = user.getAddresses();

        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = ModelUtils.getTestOrderAddressDtoRequest();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode("fake address"))
                .thenReturn(ModelUtils.getGeocodingResult());
        when(modelMapper.map(any(),
                eq(OrderAddressDtoRequest.class)))
                .thenReturn(dtoRequest);

        addresses.get(0).setActual(false);

        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();

        addresses.get(0).setAddressStatus(AddressStatus.NEW);

        assertThrows(AddressAlreadyExistException.class, () ->
                ubsService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));
    }

    @Test
    void testUpdateCurrentAddressForOrder() {
        User user = ModelUtils.getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = ModelUtils.getTestOrderAddressDtoRequest();
        dtoRequest.setId(1L);
        dtoRequest.setSearchAddress(null);
        OrderAddressDtoRequest updateAddressRequestDto = ModelUtils.getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode("fake address")).thenReturn(ModelUtils.getGeocodingResult());
        when(addressRepository.findById(user.getId())).thenReturn(Optional.ofNullable(addresses.get(0)));
        when(modelMapper.map(any(),
                eq(OrderAddressDtoRequest.class)))
                .thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);

        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.IN_ORDER);
        addresses.get(0).setUser(user);

        when(addressRepository.save(addresses.get(0))).thenReturn(addresses.get(0));
        when(modelMapper.map(dtoRequest,
                Address.class)).thenReturn(addresses.get(0));

        ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);

        verify(addressRepository, times(1)).save(addresses.get(0));
    }

     /*
     * @Test void testSaveCurrentAddressForOrderThrows() { String uuid =
     * "35467585763t4sfgchjfuyetf"; User user = new User(); user.setId(13L);
     * List<Address> addresses = getTestAddresses(user);
     *
     * OrderAddressDtoRequest dtoRequest = new OrderAddressDtoRequest();
     * dtoRequest.setId(42L);
     *
     * when(userRepository.findByUuid(uuid)).thenReturn(user);
     * when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
     * when(modelMapper.map(any(),
     * eq(OrderAddressDtoRequest.class))).thenReturn(dtoRequest);
     *
     *
     *
     * @Test void saveCurrentAddressForOrderWithDeletedAddress() { User user =
     * getTestUser(); List<Address> addresses = getTestAddresses(user);
     * OrderAddressDtoRequest dtoRequest = OrderAddressDtoRequest.builder() .id(1L)
     * .addressComment("comment") .build(); Address deletedAddress =
     * Address.builder() .id(1L) .user(user) .addressStatus(AddressStatus.DELETED)
     * .build();
     *
     * when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
     * when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
     * when(addressRepository.findById(1L)).thenReturn(Optional.of(deletedAddress));
     * when(modelMapper.map(any(), eq(OrderAddressDtoRequest.class))).thenReturn(new
     * OrderAddressDtoRequest()); when(modelMapper.map(dtoRequest,
     * Address.class)).thenReturn(new Address());
     *
     * ubsService.saveCurrentAddressForOrder(dtoRequest, user.getUuid());
     *
     * verify(addressRepository, times(addresses.size() + 1)).save(any()); }
     *
     * @Test void saveCurrentAddressForOrderWithAnotherUser() { User user =
     * getTestUser(); User anotherUser = getUser().setId(2L); List<Address>
     * addresses = getTestAddresses(anotherUser); OrderAddressDtoRequest dtoRequest
     * = OrderAddressDtoRequest.builder() .id(13L) .addressComment("comment")
     * .build();
     *
     * when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
     * when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
     * when(addressRepository.findById(13L)).thenReturn(Optional.of(addresses.get(0)
     * )); when(modelMapper.map(any(),
     * eq(OrderAddressDtoRequest.class))).thenReturn(new OrderAddressDtoRequest());
     * when(modelMapper.map(dtoRequest, Address.class)).thenReturn(new Address());
     *
     * ubsService.saveCurrentAddressForOrder(dtoRequest, user.getUuid());
     *
     * verify(addressRepository, times(addresses.size() + 1)).save(any()); }
     */

    @Test
    void testDeleteCurrentAddressForOrder() {
        String uuid = "35467585763t4sfgchjfuyetf";
        User user = new User();
        user.setId(13L);
        List<Address> addresses = getTestAddresses(user);
        Address address = addresses.get(0);
        List<AddressDto> addressDtos = getTestAddressesDto();

        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(addressRepository.save(address)).thenReturn(address);

        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
        when(modelMapper.map(addresses.get(1), AddressDto.class)).thenReturn(addressDtos.get(1));

        ubsService.deleteCurrentAddressForOrder(address.getId(), uuid);
        verify(addressRepository, times(1)).save(address);
    }

    @Test
    void testDeleteUnexistingAddress() {
        when(addressRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(NotFoundOrderAddressException.class,
            () -> ubsService.deleteCurrentAddressForOrder(42L, "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void testDeleteDeletedAddress() {
        Address address = getTestAddresses(getTestUser()).get(0);
        address.setAddressStatus(AddressStatus.DELETED);
        when(addressRepository.findById(42L)).thenReturn(Optional.of(address));
        assertThrows(NotFoundOrderAddressException.class,
            () -> ubsService.deleteCurrentAddressForOrder(42L, "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void testDeleteAddressForWrongUser() {
        Address address = getTestAddresses(new User()).get(0);
        when(addressRepository.findById(42L)).thenReturn(Optional.of(address));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(null);
        assertThrows(AccessDeniedException.class,
            () -> ubsService.deleteCurrentAddressForOrder(42L, "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void getOrderPaymentDetail() {
        Order order = getOrder();
        Certificate certificate = getCertificate();
        certificate.setOrder(order);
        order.setCertificates(Set.of(certificate));
        order.setPayment(List.of(getPayment()));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderPaymentDetailDto actual = ubsService.getOrderPaymentDetail(1L);

        assertEquals(getOrderPaymentDetailDto(), actual);
    }

    @Test
    void getOrderPaymentDetailShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        Exception thrown = assertThrows(OrderNotFoundException.class,
            () -> ubsService.getOrderPaymentDetail(null));
        assertEquals(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    void testGetOrderCancellationReason() {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        Order orderDto = ModelUtils.getOrderTest();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(orderDto));
        assert orderDto != null;
        when(userRepository.findByUuid(anyString())).thenReturn(orderDto.getUser());
        OrderCancellationReasonDto result = ubsService.getOrderCancellationReason(1L, anyString());

        assertEquals(dto.getCancellationReason(), result.getCancellationReason());
        assertEquals(dto.getCancellationComment(), result.getCancellationComment());
    }

    @Test
    void getOrderCancellationReasonOrderNotFoundException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class,
            () -> ubsService.getOrderCancellationReason(1L, "abc"));
    }

    @Test
    void getOrderCancellationReasonAccessDeniedException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getOrderTest()));
        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());
        assertThrows(AccessDeniedException.class,
            () -> ubsService.getOrderCancellationReason(1L, "abc"));
    }

    @Test
    void testUpdateOrderCancellationReason() {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        Order orderDto = ModelUtils.getOrderTest();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(orderDto));
        assert orderDto != null;
        when(userRepository.findByUuid(anyString())).thenReturn(orderDto.getUser());
        when(orderRepository.save(any())).thenReturn(orderDto);
        OrderCancellationReasonDto result = ubsService.updateOrderCancellationReason(1L, dto, anyString());

        assertEquals(dto.getCancellationReason(), result.getCancellationReason());
        assertEquals(dto.getCancellationComment(), result.getCancellationComment());
        verify(orderRepository).save(orderDto);
        verify(orderRepository).findById(1L);
    }

    @Test
    void updateOrderCancellationReasonOrderNotFoundException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class,
            () -> ubsService.updateOrderCancellationReason(1L, null, "abc"));
    }

    @Test
    void updateOrderCancellationReasonAccessDeniedException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getOrderTest()));
        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());
        assertThrows(AccessDeniedException.class,
            () -> ubsService.updateOrderCancellationReason(1L, null, "abc"));
    }

    @Test
    void testGelAllEventsFromOrderByOrderId() {
        List<Event> orderEvents = ModelUtils.getListOfEvents();
        when(orderRepository.findById(1L)).thenReturn(ModelUtils.getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(orderEvents);
        List<EventDto> eventDTOS = orderEvents.stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(Collectors.toList());
        assertEquals(eventDTOS, ubsService.getAllEventsForOrder(1L, anyString()));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithThrowingOrderNotFindException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class,
            () -> ubsService.getAllEventsForOrder(1L, "abc"));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithThrowingEventsNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(ModelUtils.getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(List.of());
        assertThrows(EventsNotFoundException.class,
            () -> ubsService.getAllEventsForOrder(1L, "abc"));
    }

    @Test
    void saveFullOrderFromLiqPay() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        Address address = ubSuser.getAddress();
        address.setUser(user);
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<Payment>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        HashMap<String, String> value = new HashMap<>();
        value.put("action", "pay");
        value.put("amount", "12000");
        value.put("currency", "UAH");
        value.put("description", "ubs user");
        value.put("order_id", "1_1");
        value.put("version", "3");
        value.put("public_key", null);
        value.put("language", "en");
        value.put("result_url", "rer.com");
        value.put("paytypes", "card");

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, null))
            .thenReturn(ModelUtils.getCourierLocations());
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(liqPayService.getCheckoutResponse(any())).thenReturn("Test");

        assertNotNull(ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf"));

        verify(bagRepository, times(2)).findById(3);
        verify(ubsUserRepository).findById(1L);
        verify(modelMapper).map(dto, Order.class);
        verify(modelMapper).map(dto.getPersonalData(), UBSuser.class);
        verify(addressRepository).findById(any());
        verify(orderRepository).findById(any());
        verify(liqPayService).getCheckoutResponse(any());
    }

    @Test
    void testSaveFullOrderFromLiqPayThrowsException() throws InvocationTargetException, IllegalAccessException {
        Service service = new Service();
        Courier courier = new Courier();
        Bag bags = new Bag();
        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(900);
        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(35);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setFullPrice(1);

        UBSuser ubSuser = getUBSuser();

        Address address = ubSuser.getAddress();
        address.setUser(user);
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<Payment>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, null))
            .thenReturn(ModelUtils.getCourierLocations());
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        assertThrows(NotEnoughBagsException.class, () -> {
            ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf");
        });
    }

    @Test
    void saveFullOrderFromLiqPayThrowNotFoundOrderAddressException() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Optional<Bag> bag = ModelUtils.getBag();

        UBSuser ubSuser = getUBSuser();

        Address address = ubSuser.getAddress();
        address.setUser(user);
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<Payment>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, null))
            .thenReturn(ModelUtils.getCourierLocations());
        when(bagRepository.findById(3)).thenReturn(bag);
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundOrderAddressException.class, () -> {
            ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf");
        });
    }

    @Test
    void validateLiqPayPayment() {
        PaymentResponseDtoLiqPay dto = ModelUtils.getPaymentResponceDto();

        when(encryptionUtil.formingResponseSignatureLiqPay(dto.getData(), null)).thenReturn("Test Signature");

        ubsService.validateLiqPayPayment(dto);

    }

    @Test
    void validateNotValidLiqPayPayment() {
        PaymentResponseDtoLiqPay dto = ModelUtils.getPaymentResponceDto();

        when(encryptionUtil.formingResponseSignatureLiqPay(dto.getData(), null)).thenReturn("fdf");

        assertThrows(PaymentValidationException.class, () -> ubsService.validateLiqPayPayment(dto));
    }

    @Test
    void deleteOrder() {
        Order order = ModelUtils.getOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
        ubsService.deleteOrder(1L);
        verify(orderRepository).delete(order);
    }

    @Test
    void processOrderFondyClient() throws Exception {
        Order order = ModelUtils.getOrderCount();
        HashMap<Integer, Integer> value = new HashMap<>();
        value.put(1, 22);
        order.setAmountOfBagsOrdered(value);
        order.setPointsToUse(100);
        User user = ModelUtils.getUser();
        user.setCurrentPoints(100);

        Bag bag = ModelUtils.bagDtoClient();
        OrderFondyClientDto dto = ModelUtils.getOrderFondyClientDto();
        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.ofNullable(user));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));

        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());

        ubsService.processOrderFondyClient(dto, "uuid");

        verify(encryptionUtil).formRequestSignature(any(), eq(null), eq("1"));
        verify(fondyClient).getCheckoutResponse(any());

    }

    @Test
    void proccessOrderLiqpayClient() {
        Order order = ModelUtils.getOrderCount();
        HashMap<Integer, Integer> value = new HashMap<>();
        value.put(1, 22);
        order.setAmountOfBagsOrdered(value);
        OrderFondyClientDto dto = getOrderFondyClientDto();
        Bag bag = ModelUtils.bagDtoClient();
        User user = ModelUtils.getUser();
        user.setCurrentPoints(100);

        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
        when(liqPayService.getCheckoutResponse(any())).thenReturn("TestValue");
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.ofNullable(user));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        ubsService.proccessOrderLiqpayClient(dto, "uuid");

        verify(orderRepository, times(2)).findById(1L);
        verify(liqPayService).getCheckoutResponse(any());
    }

    @Test
    void saveFullOrderToDBForIF() throws IllegalAccessException {
        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        Address address = ubSuser.getAddress();
        address.setUser(user);
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<Payment>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        Field merchantId = null;
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, null))
            .thenReturn(ModelUtils.getCourierLocations());
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());

        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
        assertNotNull(result);
    }

    @Test
    void testSaveToDBfromIForIFThrowsException() throws InvocationTargetException, IllegalAccessException {
        Service service = new Service();
        Courier courier = new Courier();
        LocationStatus locationStatus = LocationStatus.ACTIVE;
        Bag bags = new Bag();
        LocationTranslation locationTranslation = new LocationTranslation();
        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(35);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        Address address = ubSuser.getAddress();
        address.setUser(user);
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<Payment>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        Field merchantId = null;
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, null))
            .thenReturn(ModelUtils.getCourierLocations());
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        Assertions.assertThrows(NotEnoughBagsException.class, () -> {
            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
        });
    }

    @Test
    void saveFullOrderToDBFromLiqPayForIF() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        Address address = ubSuser.getAddress();
        address.setUser(user);
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<Payment>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        HashMap<String, String> value = new HashMap<>();
        value.put("action", "pay");
        value.put("amount", "12000");
        value.put("currency", "UAH");
        value.put("description", "ubs user");
        value.put("order_id", "1_1");
        value.put("version", "3");
        value.put("public_key", null);
        value.put("language", "en");
        value.put("result_url", "rer.com");
        value.put("paytypes", "card");

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, null))
            .thenReturn(ModelUtils.getCourierLocations());
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(liqPayService.getCheckoutResponse(any())).thenReturn("Test");

        assertNotNull(ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf"));

        verify(bagRepository, times(2)).findById(3);
        verify(ubsUserRepository).findById(1L);
        verify(modelMapper).map(dto, Order.class);
        verify(modelMapper).map(dto.getPersonalData(), UBSuser.class);
        verify(addressRepository).findById(any());
        verify(orderRepository).findById(any());
        verify(liqPayService).getCheckoutResponse(any());
    }

    @Test
    void getCourierLocationByCourierIdAndLanguageCodetest() {
        CourierLocation courierLocation = ModelUtils.getCourierLocations();
        GetCourierLocationDto getCourierLocationDto = ModelUtils.getCourierLocationsDto();

        when(courierLocationRepository.findCourierLocationsByCourierIdAndLanguageCode(1L, "ua"))
            .thenReturn(List.of(courierLocation));
        when(modelMapper.map(courierLocation, GetCourierLocationDto.class)).thenReturn(getCourierLocationDto);

        assertEquals(List.of(getCourierLocationDto), ubsService.getCourierLocationByCourierIdAndLanguageCode(1L));

        verify(courierLocationRepository).findCourierLocationsByCourierIdAndLanguageCode(1L, "ua");
        verify(modelMapper).map(courierLocation, GetCourierLocationDto.class);
    }

    @Test
    void getCourierLocationByCourierIdAndLanguageCodeThrowsCourierNotFoundException() {
        when(courierLocationRepository.findCourierLocationsByCourierIdAndLanguageCode(1L, "ua"))
            .thenReturn(Collections.emptyList());
        assertThrows(CourierNotFoundException.class, () -> ubsService.getCourierLocationByCourierIdAndLanguageCode(1L));
    }

    @Test
    void validatePaymentClientTest() {

        PaymentResponseDto dto = ModelUtils.getPaymentResponseDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersDto()));
        when(encryptionUtil.checkIfResponseSignatureIsValid(dto, null)).thenReturn(true);

        ubsService.validatePaymentClient(dto);

        verify(orderRepository, times(2)).findById(1L);
        verify(encryptionUtil).checkIfResponseSignatureIsValid(dto, null);

    }

    @Test
    void validatePaymentClientExceptionTest() {
        PaymentResponseDto dto = ModelUtils.getPaymentResponseDto();

        when(orderRepository.findById(1L))
            .thenReturn(Optional.ofNullable(ModelUtils.getOrdersDto()));

        assertThrows(PaymentValidationException.class, () -> ubsService.validatePaymentClient(dto));
    }

    @Test
    void getUserPointTest() {
        when(userRepository.findByUuid("uuid")).thenReturn(User.builder().id(1L).currentPoints(100).build());

        ubsService.getUserPoint("uuid");

        verify(userRepository).findByUuid("uuid");
    }

    @Test
    void findAllCurrentPointsForUser() {
        User user = ModelUtils.getTestUser();
        user.setCurrentPoints(100);
        user.getChangeOfPointsList().get(0).setAmount(100);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));

        AllPointsUserDto pointsDTO = ubsService.findAllCurrentPointsForUser(user.getUuid());

        assertEquals(user.getCurrentPoints(), pointsDTO.getUserBonuses());

        user.setCurrentPoints(null);
        user.setChangeOfPointsList(null);

        pointsDTO = ubsService.findAllCurrentPointsForUser(user.getUuid());

        assertEquals(0, pointsDTO.getUserBonuses());
    }

    @Test
    void getPaymentResponseFromFondy() {
        Order order = ModelUtils.getOrder();
        FondyPaymentResponse expected = FondyPaymentResponse.builder()
            .paymentStatus(order.getPayment().get(0).getResponseStatus())
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUuid(order.getUser().getUuid())).thenReturn(order.getUser());

        assertEquals(expected, ubsService.getPaymentResponseFromFondy(1L, order.getUser().getUuid()));
    }

    @Test
    void getPaymentResponseFromFondyOrderNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> {
            ubsService.getPaymentResponseFromFondy(1L, "abc");
        });
    }

    @Test
    void getPaymentResponseFromFondyPaymentNotFoundException() {
        Order order = getOrder().setPayment(Collections.emptyList());
        String uuid = order.getUser().getUuid();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUuid(uuid)).thenReturn(order.getUser());

        assertThrows(PaymentNotFoundException.class, () -> {
            ubsService.getPaymentResponseFromFondy(1L, uuid);
        });
    }

    @Test
    void getPaymentResponseFromFondyAccessDeniedException() {
        Order order = ModelUtils.getOrder();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUuid(anyString())).thenReturn(ModelUtils.getTestUser());

        assertThrows(AccessDeniedException.class, () -> {
            ubsService.getPaymentResponseFromFondy(1L, "abc");
        });
    }

    @Test
    void getLiqPayStatusAccessDeniedException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(getOrder()));
        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());
        assertThrows(AccessDeniedException.class, () -> {
            ubsService.getLiqPayStatus(1L, "abc");
        });
    }

    @Test
    void getOrderForUserTest() {
        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = ModelUtils.getOrderPaymentStatusTranslation();
        OrdersDataForUserDto ordersDataForUserDto = ModelUtils.getOrderStatusDto();
        BagTranslation translation = ModelUtils.getBagTranslation();
        Order order = ModelUtils.getOrderTest();
        User user = ModelUtils.getTestUser();
        Bag bag = ModelUtils.bagDto();

        List<Bag> bags = new ArrayList<>();
        List<Order> orderList = new ArrayList<>();

        BagForUserDto bagForUserDto = ordersDataForUserDto.getBags().get(0);
        bag.setCapacity(120);
        bag.setFullPrice(1200);
        order.setAmountOfBagsOrdered(Map.of(1, 10));
        bags.add(bag);
        order.setUser(user);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        when(ordersForUserRepository.findAllOrdersByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(bagRepository.findBagByOrderId(order.getId())).thenReturn(bags);
        when(modelMapper.map(bag, BagForUserDto.class)).thenReturn(bagForUserDto);
        when(bagTranslationRepository.findBagTranslationByBag(bag)).thenReturn(translation);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById(order.getOrderStatus().getNumValue()))
                .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndTranslationValue(
            (long) order.getOrderPaymentStatus().getStatusValue()))
                .thenReturn(orderPaymentStatusTranslation);

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());

        verify(bagTranslationRepository, times(bags.size())).findBagTranslationByBag(bag);
        verify(modelMapper, times(bags.size())).map(bag, BagForUserDto.class);
        verify(bagRepository).findBagByOrderId(order.getId());
        verify(orderStatusTranslationRepository, times(orderList.size()))
            .getOrderStatusTranslationById(order.getOrderStatus().getNumValue());
        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
            .findByOrderPaymentStatusIdAndTranslationValue(
                (long) order.getOrderPaymentStatus().getStatusValue());
        verify(ordersForUserRepository).findAllOrdersByUserUuid(pageable, user.getUuid());

    }

}
