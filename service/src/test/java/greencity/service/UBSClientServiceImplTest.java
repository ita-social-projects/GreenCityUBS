package greencity.service;

import com.liqpay.LiqPay;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.AddressStatus;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.service.ubs.UBSClientServiceImpl;
import greencity.util.EncryptionUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
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
    private LocationRepository locationRepository;
    @Mock
    EntityManager entityManager;
    @Mock
    private RestClient restClient;
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

    @Test
    @Transactional
    void testValidatePayment() {
        PaymentResponseDto dto = new PaymentResponseDto();
        Order order = getOrder();
        dto.setOrderId(order.getId().toString());
        dto.setResponseStatus("approved");
        dto.setOrderStatus("approved");
        Payment payment = getPayment();
        when(encryptionUtil.checkIfResponseSignatureIsValid(dto, null)).thenReturn(true);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(modelMapper.map(dto, Payment.class)).thenReturn(payment);
        ubsService.validatePayment(dto);
        verify(paymentRepository, times(1)).save(payment);

    }

    @Test
    void unvalidValidatePayment() {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setResponseStatus("approved");
        when(encryptionUtil.checkIfResponseSignatureIsValid(dto, null)).thenReturn(false);
        assertThrows(PaymentValidationException.class, () -> ubsService.validatePayment(dto));
    }

    @Test
    void testValidatePaymentFailureResponse() {
        PaymentResponseDto paymentResponseDto = new PaymentResponseDto();
        paymentResponseDto.setResponseStatus("failure");
        assertThrows(PaymentValidationException.class, () -> ubsService.validatePayment(paymentResponseDto));
    }

    @Test
    void getFirstPageData() {
        UserPointsAndAllBagsDtoTest userPointsAndAllBagsDtoExpected =
            new UserPointsAndAllBagsDtoTest(new ArrayList<BagTranslationDto>(), 2l, 600);

        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(600);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);

        UserPointsAndAllBagsDto userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageData("35467585763t4sfgchjfuyetf");

        assertEquals(userPointsAndAllBagsDtoExpected.getBags(), userPointsAndAllBagsDtoActual.getBags());
        assertEquals(userPointsAndAllBagsDtoExpected.getPoints(), userPointsAndAllBagsDtoActual.getPoints());
    }

    @Test
    void getFirstPageDataTest() throws InterruptedException {
        UserPointsAndAllBagsDtoTest userPointsAndAllBagsDtoExpected =
            new UserPointsAndAllBagsDtoTest(new ArrayList<BagTranslationDto>(), 2l, 600);

        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(600);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);

        UserPointsAndAllBagsDtoTest userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageDataTest("35467585763t4sfgchjfuyetf");

        assertEquals(userPointsAndAllBagsDtoExpected.getBags(), userPointsAndAllBagsDtoActual.getBags());
        assertEquals(userPointsAndAllBagsDtoExpected.getPoints(), userPointsAndAllBagsDtoActual.getPoints());
    }

    @Test
    void testSaveToDB() throws InvocationTargetException, IllegalAccessException {

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
        bag.setPrice(400);

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
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(13L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));

        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(restClient.getDataFromFondy(any())).thenReturn("TestValue");
        String result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
        assertNotNull(result);

    }

    @Test
    void testSaveToDBThrowsException() throws InvocationTargetException, IllegalAccessException {

        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(900);
        Location location = new Location(1l, "Name", 100l, List.of(user));
        user.setLastLocation(location);

        OrderResponseDto dto = getOrderResponseDto();
//        dto.setMinAmountOfBigBags(10000l);
        dto.getBags().get(0).setAmount(35);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setPrice(400);

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
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        Assertions.assertThrows(IncorrectValueException.class, () -> {
            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
        });
    }

    @Test
    void getSecondPageData() {
        PersonalDataDto expected = ModelUtils.getOrderResponseDto().getPersonalData();
        User user = new User();
        user.setId(13L);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(ubsUserRepository.getAllByUserId(anyLong()))
            .thenReturn(Collections.singletonList(ModelUtils.getUBSuser()));
        when(modelMapper.map(ModelUtils.getUBSuser(), PersonalDataDto.class)).thenReturn(expected);

        assertEquals(expected, ubsService.getSecondPageData("35467585763t4sfgchjfuyetf").get(0));
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
            ubsService.checkCertificate("randomstring").getCertificateStatus();
        });
    }

    @Test
    void getAllOrdersDoneByUser() {
        Order order = getOrderDoneByUser();
        OrderClientDto dto = getOrderClientDto();
        List<Order> orderList = Collections.singletonList(order);
        List<OrderClientDto> expected = Collections.singletonList(dto);

        when(orderRepository.getAllOrdersOfUser(anyString()))
            .thenReturn(orderList);
        when(modelMapper.map(order, OrderClientDto.class)).thenReturn(dto);

        List<OrderClientDto> result = ubsService.getAllOrdersDoneByUser(anyString());

        assertEquals(expected, result);
    }

    @Test
    void cancelFormedOrder() {
        Order order = getFormedOrder();
        OrderClientDto expected = getOrderClientDto();
        expected.setOrderStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderClientDto.class)).thenReturn(expected);

        OrderClientDto result = ubsService.cancelFormedOrder(1L);

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(order);

        assertEquals(expected, result);
    }

    @Test
    void cancelFormedOrderShouldThrowOrderNotFoundException() {
        Exception thrown = assertThrows(OrderNotFoundException.class,
            () -> ubsService.cancelFormedOrder(1L));
        assertEquals(thrown.getMessage(), ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
    }

    @Test
    void cancelFormedOrderShouldThrowBadOrderStatusException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrderDoneByUser()));
        Exception thrown = assertThrows(BadOrderStatusRequestException.class,
            () -> ubsService.cancelFormedOrder(1L));
        assertEquals(thrown.getMessage(), ErrorMessage.BAD_ORDER_STATUS_REQUEST
            + getOrderDoneByUser().getOrderStatus());
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
        when(bagTranslationRepository.findAllByLanguageOrder("en", 1L))
            .thenReturn(List.of(getBagTranslation()));

        MakeOrderAgainDto result = ubsService.makeOrderAgain(new Locale("en"), 1L);

        assertEquals(dto, result);
        verify(orderRepository, times(1)).findById(1L);
        verify(bagTranslationRepository, times(1)).findAllByLanguageOrder("en", 1L);
    }

    @Test
    void makeOrderAgainShouldThrowOrderNotFoundException() {
        Exception thrown = assertThrows(OrderNotFoundException.class,
            () -> ubsService.makeOrderAgain(new Locale("en"), 1L));
        assertEquals(thrown.getMessage(), ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
    }

    @Test
    void makeOrderAgainShouldThrowBadOrderStatusException() {
        Order order = getOrderDoneByUser();
        order.setOrderStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Exception thrown = assertThrows(BadOrderStatusRequestException.class,
            () -> ubsService.makeOrderAgain(new Locale("en"), 1L));
        assertEquals(thrown.getMessage(), ErrorMessage.BAD_ORDER_STATUS_REQUEST
            + order.getOrderStatus());
    }

    @Test
    void findAllOrdersByUuid() {
        when(orderRepository.findAllOrdersByUserUuid("87df9ad5-6393-441f-8423-8b2e770b01a8"))
            .thenReturn(Arrays.asList(ModelUtils.getOrder()));
        assertEquals(ModelUtils.getOrder().getPointsToUse(),
            ubsService.findAllCurrentPointsForUser("87df9ad5-6393-441f-8423-8b2e770b01a8").getUserBonuses());
    }

    @Test
    void findAllOrderNotFoundException() {
        Exception thrown = assertThrows(OrderNotFoundException.class,
            () -> ubsService.findAllCurrentPointsForUser("87df9ad5-6393-441f-8423-8b2e770b01a8"));
        assertEquals(thrown.getMessage(), ErrorMessage.ORDERS_FOR_UUID_NOT_EXIST);
    }

    void getsUserAndUserUbsAndViolationsInfoByOrderIdThrowOrderNotFoundException() {
        when(orderRepository.findById(1L))
            .thenThrow(OrderNotFoundException.class);
        assertThrows(OrderNotFoundException.class,
            () -> ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L));
    }

    @Test
    void getsUserAndUserUbsAndViolationsInfoByOrderId() {
        UserInfoDto expectedResult = ModelUtils.getUserInfoDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrderDetails()));
        when(userRepository.countTotalUsersViolations(1L)).thenReturn(expectedResult.getTotalUserViolations());
        when(userRepository.checkIfUserHasViolationForCurrentOrder(1L, 1L))
            .thenReturn(expectedResult.getUserViolationForCurrentOrder());
        UserInfoDto actual = ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L);

        verify(orderRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).countTotalUsersViolations(1L);
        verify(userRepository, times(1)).checkIfUserHasViolationForCurrentOrder(1L, 1L);

        assertEquals(expectedResult, actual);
    }

    @Test
    void updatesUbsUserInfoInOrderShouldThrowUBSuserNotFoundException() {
        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
            .id(1l)
            .recipientName("Anatolii Petyrov")
            .recipientEmail("anatolii.andr@gmail.com")
            .recipientPhoneNumber("095123456").build();

        when(ubsUserRepository.findById(1L))
            .thenThrow(UBSuserNotFoundException.class);
        assertThrows(UBSuserNotFoundException.class,
            () -> ubsService.updateUbsUserInfoInOrder(request));
    }

    @Test
    void updatesUbsUserInfoInOrder() {
        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
            .id(1l)
            .recipientName("Anatolii Petyrov")
            .recipientEmail("anatolii.andr@gmail.com")
            .recipientPhoneNumber("095123456").build();

        Optional<UBSuser> user = Optional.of(ModelUtils.getUBSuser());
        when(ubsUserRepository.findById(1L)).thenReturn(user);
        when(ubsUserRepository.save(user.get())).thenReturn(user.get());

        UbsCustomersDto expected = UbsCustomersDto.builder()
            .name("Anatolii Petyrov")
            .email("anatolii.andr@gmail.com")
            .phoneNumber("095123456")
            .build();

        UbsCustomersDto actual = ubsService.updateUbsUserInfoInOrder(request);
        assertEquals(expected, actual);
    }

    @Test
    void saveProfileData() {
        User user = ModelUtils.getUser();

        when(userRepository.findByUuid("87df9ad5-6393-441f-8423-8b2e770b01a8")).thenReturn(user);

        AddressDto addressDto = ModelUtils.addressDto();
        Address address = ModelUtils.address();

        UserProfileDto userProfileDto =
            UserProfileDto.builder().addressDto(addressDto).recipientEmail(user.getRecipientEmail())
                .recipientName(user.getRecipientName()).recipientSurname(user.getRecipientSurname())
                .recipientPhone(user.getRecipientPhone())
                .build();

        UBSuser ubSuser = new UBSuser(address, user, new ArrayList<>(), 1L, user.getRecipientName(),
            user.getRecipientSurname(), user.getRecipientPhone(), user.getRecipientEmail());

        Optional<UBSuser> optionalUBSuser = Optional.of(ubSuser);

        PersonalDataDto dto = PersonalDataDto.builder().email(ubSuser.getEmail()).firstName(ubSuser.getFirstName())
            .lastName(ubSuser.getLastName()).phoneNumber(ubSuser.getPhoneNumber()).id(1L).build();

        lenient().when(modelMapper.map(addressDto, Address.class)).thenReturn(address);
        when(userRepository.save(user)).thenReturn(user);
        when(addressRepository.save(address)).thenReturn(address);
        lenient().when(modelMapper.map(address, AddressDto.class)).thenReturn(addressDto);
        lenient().when(modelMapper.map(user, UserProfileDto.class)).thenReturn(userProfileDto);
        when(ubsUserRepository.findByEmail("someUser@gmail.com")).thenReturn(optionalUBSuser);
        when(ubsUserRepository.findById(1L)).thenReturn(optionalUBSuser);
        lenient().when(modelMapper.map(dto, UBSuser.class)).thenReturn(ubSuser);
        lenient().when(modelMapper.map(address, AddressDto.class)).thenReturn(addressDto);
        ubsService.saveProfileData("87df9ad5-6393-441f-8423-8b2e770b01a8", userProfileDto);
        assertNotNull(userProfileDto.getAddressDto());
        assertNotNull(userProfileDto);
        assertNotNull(address);
    }

    @Test
    void getProfileData() {
        User user = ModelUtils.getUser();
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        UserProfileDto userProfileDto = new UserProfileDto();
        AddressDto addressDto = ModelUtils.addressDto();
        userProfileDto.setAddressDto(addressDto);
        Address address = ModelUtils.address();
        when(modelMapper.map(user, UserProfileDto.class)).thenReturn(userProfileDto);
        assertEquals(userProfileDto, ubsService.getProfileData(user.getUuid()));
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
        String uuid = "35467585763t4sfgchjfuyetf";
        User user = new User();
        user.setId(13L);
        List<Address> addresses = getTestAddresses(user);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
        when(modelMapper.map(any(), eq(OrderAddressDtoRequest.class))).thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);

        addresses.get(0).setActual(false);
        when(addressRepository.save(addresses.get(0))).thenReturn(addresses.get(0));

        OrderAddressDtoRequest dtoRequest = new OrderAddressDtoRequest();
        dtoRequest.setId(42L);
        when(addressRepository.findById(dtoRequest.getId())).thenReturn(Optional.of(addresses.get(0)));
        when(modelMapper.map(dtoRequest, Address.class)).thenReturn(new Address());

        addresses.get(0).setAddressStatus(AddressStatus.IN_ORDER); // AddressStatus.DELETED

        ubsService.saveCurrentAddressForOrder(dtoRequest, uuid);

        verify(addressRepository, times(1)).save(addresses.get(0));
    }

    @Test
    void testSaveCurrentAddressForOrderThrows() {
        String uuid = "35467585763t4sfgchjfuyetf";
        User user = new User();
        user.setId(13L);
        List<Address> addresses = getTestAddresses(user);

        OrderAddressDtoRequest dtoRequest = new OrderAddressDtoRequest();
        dtoRequest.setId(42L);

        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
        when(modelMapper.map(any(), eq(OrderAddressDtoRequest.class))).thenReturn(dtoRequest);

        assertThrows(AddressAlreadyExistException.class,
            () -> ubsService.saveCurrentAddressForOrder(dtoRequest, uuid));
    }

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

        address.setAddressStatus(AddressStatus.DELETED);
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
    void testDeleteUnexistingAddressForCurrentUser() {
        Address address = getTestAddresses(new User()).get(0);
        when(addressRepository.findById(42L)).thenReturn(Optional.of(address));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(null);
        assertThrows(NotFoundOrderAddressException.class,
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
            () -> ubsService.getOrderPaymentDetail(any()));
        assertEquals(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    void testGetOrderCancellationReason() {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        Order orderDto = ModelUtils.getOrderTest();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(orderDto));
        OrderCancellationReasonDto result = ubsService.getOrderCancellationReason(1L);

        assertEquals(dto.getCancellationReason(), result.getCancellationReason());
        assertEquals(dto.getCancellationComment(), result.getCancellationComment());

    }

    @Test
    void testUpdateOrderCancellationReason() {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        Order orderDto = ModelUtils.getOrderTest();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(orderDto));
        when(orderRepository.save(any())).thenReturn(orderDto);
        OrderCancellationReasonDto result = ubsService.updateOrderCancellationReason(1L, dto);

        assertEquals(dto.getCancellationReason(), result.getCancellationReason());
        assertEquals(dto.getCancellationComment(), result.getCancellationComment());
        assert orderDto != null;
        verify(orderRepository).save(orderDto);
        verify(orderRepository).findById(1L);
    }

    @Test
    void testGetAllLocationsForNewUser() {
        User user = getUser();

        when(userRepository.findByUuid("uuid")).thenReturn(user);
        when(locationRepository.findAll()).thenReturn(getLocationList());
        assertEquals(getLocationResponseDtoList(), ubsService.getAllLocations("uuid"));
    }

    @Test
    void testGetAllLocationsForUserWithLocation() {
        User user = getUser();
        user.setLastLocation(getLastLocation());

        when(userRepository.findByUuid("uuid")).thenReturn(user);
        when(locationRepository.findAll()).thenReturn(getLocationList());
        assertEquals(getLocationResponseDtoList(), ubsService.getAllLocations("uuid"));
    }

    @Test
    void testSetNewLastOrderLocation() {
        LocationIdDto locationIdDto = LocationIdDto.builder().locationId(1l).build();
        User user = getUser();
        Location lastLocation = getLastLocation();
        when(userRepository.findByUuid("uuid")).thenReturn(user);
        when(locationRepository.findById(1l)).thenReturn(Optional.of(lastLocation));
        ubsService.setNewLastOrderLocation("uuid", locationIdDto);

        assertEquals(user.getLastLocation(), lastLocation);
    }

    @Test
    void testGelAllEventsFromOrderByOrderId() {
        List<Event> orderEvents = ModelUtils.getListOfEvents();
        when(orderRepository.findById(1L)).thenReturn(ModelUtils.getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(orderEvents);
        List<EventDto> eventDTOS = orderEvents.stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(Collectors.toList());
        assertEquals(eventDTOS, ubsService.getAllEventsForOrderById(1L));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithThrowingOrderNotFindException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class,
            () -> ubsService.getAllEventsForOrderById(1L));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithThrowingEventsNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(ModelUtils.getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(List.of());
        assertThrows(EventsNotFoundException.class,
            () -> ubsService.getAllEventsForOrderById(1L));
    }

    @Test
    void saveFullOrderFromLiqPay() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(35);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setPrice(400);

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
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(13L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(restClient.getDataFromLiqPay(any())).thenReturn(value);
        when(liqPay.cnb_form(any())).thenReturn("Test Values");

        assertNotNull(ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf"));

        verify(bagRepository).findById(3);
        verify(ubsUserRepository).findById(13L);
        verify(modelMapper).map(dto, Order.class);
        verify(modelMapper).map(dto.getPersonalData(), UBSuser.class);
        verify(addressRepository).findById(any());
        verify(orderRepository).findById(any());
        verify(restClient).getDataFromLiqPay(any());
        verify(liqPay).cnb_form(any());
    }

    @Test
    void testSaveFullOrderFromLiqPayThrowsException() throws InvocationTargetException, IllegalAccessException {

        User user = ModelUtils.getUserWithLastLocation();
        user.setCurrentPoints(900);
        Location location = new Location(1l, "Name", 100l, List.of(user));
        user.setLastLocation(location);

        OrderResponseDto dto = getOrderResponseDto();
        ;
        dto.getBags().get(0).setAmount(35);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setPrice(1);

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
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        assertThrows(IncorrectValueException.class, () -> {
            ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf");
        });
    }

    @Test
    void saveFullOrderFromLiqPayThrowNotFoundOrderAddressException() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(35);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setPrice(400);

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
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(13L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundOrderAddressException.class, () -> {
            ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf");
        });
    }

    @Test
    void validateLiqPayPayment() {
        String signature = "TestSignature";
        PaymentResponseDtoLiqPay dto = new PaymentResponseDtoLiqPay();
        Order order = ModelUtils.getOrder();
        dto.setOrderId(order.getId().toString());
        dto.setStatus("success");
        Payment payment = getPayment();

        when(encryptionUtil.formingResponseSignatureLiqPay(dto, null)).thenReturn("TestSignature");
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(modelMapper.map(dto, Payment.class)).thenReturn(payment);

        ubsService.validateLiqPayPayment(dto, signature);

        verify(paymentRepository).save(payment);
        ;
    }

    @Test
    void validateNotValidLiqPayPayment() {
        String signature = "signature";
        PaymentResponseDtoLiqPay dto = new PaymentResponseDtoLiqPay();
        dto.setStatus("success");

        when(encryptionUtil.formingResponseSignatureLiqPay(dto, null)).thenReturn("fdf");

        assertThrows(PaymentValidationException.class, () -> ubsService.validateLiqPayPayment(dto, signature));
    }

    @Test
    void vadlidateLiqPayPaymentWithStatusFailure() {
        String signature = "signature";
        PaymentResponseDtoLiqPay dto = new PaymentResponseDtoLiqPay();
        dto.setStatus("failure");

        assertThrows(PaymentValidationException.class, () -> ubsService.validateLiqPayPayment(dto, signature));
    }

    @Test
    void vadlidateLiqPayPaymentWithStatusError() {
        String signature = "signature";
        PaymentResponseDtoLiqPay dto = new PaymentResponseDtoLiqPay();
        dto.setStatus("error");

        when(encryptionUtil.formingResponseSignatureLiqPay(dto, null)).thenReturn("signature");

        assertThrows(PaymentValidationException.class, () -> ubsService.validateLiqPayPayment(dto, signature));
    }

}