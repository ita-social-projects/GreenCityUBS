package greencity.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import greencity.dto.order.*;
import greencity.dto.payment.StatusRequestDtoLiqPay;
import greencity.entity.enums.*;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.liqpay.LiqPay;

import greencity.ModelUtils;
import greencity.client.FondyClient;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.LocationsDtos;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.RegionDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.address.AddressDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseDtoLiqPay;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.order.Certificate;
import greencity.entity.order.Courier;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.Service;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.AddressRepository;
import greencity.repository.BagRepository;
import greencity.repository.BagTranslationRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.CourierRepository;
import greencity.repository.EventRepository;
import greencity.repository.LanguageRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.EventService;
import greencity.service.ubs.LiqPayService;
import greencity.service.ubs.UBSClientServiceImpl;
import greencity.service.ubs.UBSManagementService;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static greencity.ModelUtils.TEST_ORDER_ADDRESS_DTO_REQUEST;
import static greencity.ModelUtils.getBagOrderDto;
import static greencity.ModelUtils.getBagTranslation;
import static greencity.ModelUtils.getCertificate;
import static greencity.ModelUtils.getOrder;
import static greencity.ModelUtils.getOrderClientDto;
import static greencity.ModelUtils.getOrderDetails;
import static greencity.ModelUtils.getOrderDoneByUser;
import static greencity.ModelUtils.getOrderFondyClientDto;
import static greencity.ModelUtils.getOrderPaymentDetailDto;
import static greencity.ModelUtils.getOrderResponseDto;
import static greencity.ModelUtils.getOrderTest;
import static greencity.ModelUtils.getPayment;
import static greencity.ModelUtils.getSuccessfulFondyResponse;
import static greencity.ModelUtils.getTestUser;
import static greencity.ModelUtils.getUBSuser;
import static greencity.ModelUtils.getUserWithLastLocation;

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
    private CourierRepository courierRepository;
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private UBSManagementService ubsManagementService;
    @Mock
    private OrderStatusTranslationRepository orderStatusTranslationRepository;
    @Mock
    private OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private LocationRepository locationRepository;
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
        dto.setFee(null);
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
        assertThrows(BadRequestException.class, () -> ubsService.validatePayment(dto));
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
        user.setAlternateEmail("test@mail.com");
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
        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());

        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        assertNotNull(result);

    }

    @Test
    void testSaveToDBExistOrder() throws InvocationTargetException, IllegalAccessException {

        User user = ModelUtils.getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
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
        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
                .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());

        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", 1l);
        assertNotNull(result);
    }


//
//    @Test
//    void testSaveToDBThrowsException() throws InvocationTargetException, IllegalAccessException {
//        Service service = new Service();
//        Courier courier = new Courier();
//        LocationStatus locationStatus = LocationStatus.ACTIVE;
//        Bag bags = new Bag();
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(900);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(3);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(100);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Order order1 = getOrder();
//        order1.setPayment(new ArrayList<Payment>());
//        Payment payment1 = getPayment();
//        payment1.setId(1L);
//        order1.getPayment().add(payment1);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfoWithLimitOfBags()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        Assertions.assertThrows(BadRequestException.class, () -> {
//            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        });
//    }
//
//    @Test
//    void getSecondPageData() {
//        String uuid = "35467585763t4sfgchjfuyetf";
//        PersonalDataDto expected = ModelUtils.getOrderResponseDto().getPersonalData();
//
//        User user = ModelUtils.getTestUser()
//            .setUuid(uuid)
//            .setRecipientEmail("mail@mail.ua")
//            .setRecipientPhone("067894522")
//            .setAlternateEmail("my@email.com");
//        List<UBSuser> ubsUser = new ArrayList<>();
//        when(userRepository.findByUuid(uuid)).thenReturn(user);
//        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(ubsUser);
//        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(expected);
//        PersonalDataDto actual = ubsService.getSecondPageData("35467585763t4sfgchjfuyetf");
//
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    void checkCertificate() {
//        when(certificateRepository.findById("certificate")).thenReturn(Optional.of(Certificate.builder()
//            .code("certificate")
//            .certificateStatus(CertificateStatus.ACTIVE)
//            .build()));
//
//        assertEquals("ACTIVE", ubsService.checkCertificate("certificate").getCertificateStatus());
//    }
//
//    @Test
//    void checkCertificateUSED() {
//        when(certificateRepository.findById("certificate")).thenReturn(Optional.of(Certificate.builder()
//            .code("certificate")
//            .certificateStatus(CertificateStatus.USED)
//            .build()));
//
//        assertEquals("USED", ubsService.checkCertificate("certificate").getCertificateStatus());
//    }
//
//    @Test
//    void checkCertificateWithNoAvailable() {
//        Assertions.assertThrows(NotFoundException.class, () -> {
//            ubsService.checkCertificate("randomstring");
//        });
//    }
//
//    @Test
//    void getAllOrdersDoneByUser() {
//        Order order = getOrderDoneByUser();
//        OrderClientDto dto = getOrderClientDto();
//        List<Order> orderList = Collections.singletonList(order);
//        List<OrderClientDto> expected = Collections.singletonList(dto);
//
//        when(orderRepository.getAllOrdersOfUser(any())).thenReturn(orderList);
//        when(modelMapper.map(order, OrderClientDto.class)).thenReturn(dto);
//
//        List<OrderClientDto> result = ubsService.getAllOrdersDoneByUser(anyString());
//
//        assertEquals(expected, result);
//    }
//
//    @Test
//    void makeOrderAgain() {
//        MakeOrderAgainDto dto = MakeOrderAgainDto.builder()
//            .orderId(1L)
//            .orderAmount(350L)
//            .bagOrderDtoList(List.of(getBagOrderDto()))
//            .build();
//        Order order = getOrderDoneByUser();
//        order.setAmountOfBagsOrdered(Collections.singletonMap(1, 1));
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
//        when(bagTranslationRepository.findAllByOrder(1L))
//            .thenReturn(List.of(getBagTranslation()));
//
//        MakeOrderAgainDto result = ubsService.makeOrderAgain(new Locale("en"), 1L);
//
//        assertEquals(dto, result);
//        verify(orderRepository, times(1)).findById(1L);
//        verify(bagTranslationRepository, times(1)).findAllByOrder(1L);
//    }
//
//    @Test
//    void makeOrderAgainShouldThrowOrderNotFoundException() {
//        Locale locale = new Locale("en");
//        Exception thrown = assertThrows(NotFoundException.class,
//            () -> ubsService.makeOrderAgain(locale, 1L));
//        assertEquals(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
//    }
//
//    @Test
//    void makeOrderAgainShouldThrowBadOrderStatusException() {
//        Order order = getOrderDoneByUser();
//        order.setOrderStatus(OrderStatus.CANCELED);
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
//        Locale locale = new Locale("en");
//        Exception thrown = assertThrows(BadRequestException.class,
//            () -> ubsService.makeOrderAgain(locale, 1L));
//        assertEquals(thrown.getMessage(), ErrorMessage.BAD_ORDER_STATUS_REQUEST
//            + order.getOrderStatus());
//    }
//
//    @Test
//    void findUserByUuid() {
//        String uuid = "87df9ad5-6393-441f-8423-8b2e770b01a8";
//        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(ModelUtils.getUser()));
//        ubsService.findAllCurrentPointsForUser(uuid);
//        verify(userRepository).findUserByUuid(uuid);
//    }
//
//    @Test
//    void findUserNotFoundException() {
//        Exception thrown = assertThrows(UserNotFoundException.class,
//            () -> ubsService.findAllCurrentPointsForUser("87df9ad5-6393-441f-8423-8b2e770b01a8"));
//        assertEquals(ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
//    }
//
//    @Test
//    void markUserAsDeactivatedByIdThrowsNotFoundException() {
//        Exception thrown = assertThrows(NotFoundException.class,
//            () -> ubsService.markUserAsDeactivated(1L));
//        assertEquals(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST, thrown.getMessage());
//    }
//
//    @Test
//    void markUserAsDeactivatedById() {
//        User user = ModelUtils.getUser();
//        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
//        ubsService.markUserAsDeactivated(1L);
//        verify(userRepository).findById(1L);
//        verify(userRemoteClient).markUserDeactivated(user.getUuid());
//    }
//
//    @Test
//    void getsUserAndUserUbsAndViolationsInfoByOrderId() {
//        UserInfoDto expectedResult = ModelUtils.getUserInfoDto();
//        expectedResult.setRecipientId(1L);
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrderDetails()));
//        when(userRepository.findByUuid(anyString())).thenReturn(getOrderDetails().getUser());
//        when(userRepository.countTotalUsersViolations(1L)).thenReturn(expectedResult.getTotalUserViolations());
//        when(userRepository.checkIfUserHasViolationForCurrentOrder(1L, 1L))
//            .thenReturn(expectedResult.getUserViolationForCurrentOrder());
//        UserInfoDto actual = ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, anyString());
//
//        verify(orderRepository, times(1)).findById(1L);
//        verify(userRepository, times(1)).countTotalUsersViolations(1L);
//        verify(userRepository, times(1)).checkIfUserHasViolationForCurrentOrder(1L, 1L);
//
//        assertEquals(expectedResult, actual);
//    }
//
//    @Test
//    void getsUserAndUserUbsAndViolationsInfoByOrderIdWithoutSender() {
//        UserInfoDto expectedResult = ModelUtils.getUserInfoDto();
//        expectedResult.setRecipientId(1L);
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getOrderDetailsWithoutSender()));
//        when(userRepository.findByUuid(anyString())).thenReturn(ModelUtils.getOrderDetailsWithoutSender().getUser());
//        when(userRepository.countTotalUsersViolations(1L)).thenReturn(expectedResult.getTotalUserViolations());
//        when(userRepository.checkIfUserHasViolationForCurrentOrder(1L, 1L))
//            .thenReturn(expectedResult.getUserViolationForCurrentOrder());
//        UserInfoDto actual = ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, anyString());
//
//        verify(orderRepository, times(1)).findById(1L);
//        verify(userRepository, times(1)).countTotalUsersViolations(1L);
//        verify(userRepository, times(1)).checkIfUserHasViolationForCurrentOrder(1L, 1L);
//
//        assertEquals(expectedResult, actual);
//    }
//
//    @Test
//    void getUserAndUserUbsAndViolationsInfoByOrderIdOrderNotFoundException() {
//        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(NotFoundException.class,
//            () -> ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, "abc"));
//    }
//
//    @Test
//    void getUserAndUserUbsAndViolationsInfoByOrderIdAccessDeniedException() {
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrder()));
//        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());
//        assertThrows(AccessDeniedException.class,
//            () -> ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, "abc"));
//    }
//
//    @Test
//    void updatesUbsUserInfoInOrderShouldThrowUBSuserNotFoundException() {
//        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
//        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
//            .recipientId(1l)
//            .recipientName("Anatolii Petyrov")
//            .recipientEmail("anatolii.andr@gmail.com")
//            .recipientPhoneNumber("095123456").build();
//
//        when(ubsUserRepository.findById(1L))
//            .thenThrow(UBSuserNotFoundException.class);
//        assertThrows(UBSuserNotFoundException.class,
//            () -> ubsService.updateUbsUserInfoInOrder(request, "abc"));
//    }
//
//    @Test
//    void updatesUbsUserInfoInOrder() {
//        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(ModelUtils.getUser()));
//        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
//            .recipientId(1l)
//            .recipientName("Anatolii")
//            .recipientSurName("Anatolii")
//            .recipientEmail("anatolii.andr@gmail.com")
//            .recipientPhoneNumber("095123456").build();
//
//        Optional<UBSuser> user = Optional.of(ModelUtils.getUBSuser());
//        when(ubsUserRepository.findById(1L)).thenReturn(user);
//        when(ubsUserRepository.save(user.get())).thenReturn(user.get());
//
//        UbsCustomersDto expected = UbsCustomersDto.builder()
//            .name("Anatolii Anatolii")
//            .email("anatolii.andr@gmail.com")
//            .phoneNumber("095123456")
//            .build();
//
//        UbsCustomersDto actual = ubsService.updateUbsUserInfoInOrder(request, "abc");
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    void saveProfileData() {
//        User user = ModelUtils.getUser();
//
//        when(userRepository.findByUuid("87df9ad5-6393-441f-8423-8b2e770b01a8")).thenReturn(user);
//
//        List<AddressDto> addressDto = ModelUtils.addressDtoList();
//        List<Address> address = ModelUtils.addressList();
//        List<Bot> botList = ModelUtils.botList();
//
//        UserProfileUpdateDto userProfileUpdateDto =
//            UserProfileUpdateDto.builder().addressDto(addressDto)
//                .recipientName(user.getRecipientName()).recipientSurname(user.getRecipientSurname())
//                .recipientPhone(user.getRecipientPhone())
//                .alternateEmail("test@email.com")
//                .build();
//
//        when(modelMapper.map(addressDto.get(0), Address.class)).thenReturn(address.get(0));
//        when(modelMapper.map(addressDto.get(1), Address.class)).thenReturn(address.get(1));
//        when(userRepository.save(user)).thenReturn(user);
//        for (Address address1 : address) {
//            when(addressRepository.save(address1)).thenReturn(address1);
//        }
//        when(modelMapper.map(address.get(0), AddressDto.class)).thenReturn(addressDto.get(0));
//        when(modelMapper.map(address.get(1), AddressDto.class)).thenReturn(addressDto.get(1));
//        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);
//        ubsService.updateProfileData("87df9ad5-6393-441f-8423-8b2e770b01a8", userProfileUpdateDto);
//        for (Bot bot : botList) {
//            assertNotNull(bot);
//        }
//        assertNotNull(userProfileUpdateDto.getAddressDto());
//        assertNotNull(userProfileUpdateDto);
//        assertNotNull(address);
//    }
//
//    @Test
//    void getProfileData() {
//        User user = ModelUtils.getUser();
//        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
//        UserProfileDto userProfileDto = new UserProfileDto();
//        List<AddressDto> addressDto = ModelUtils.addressDtoList();
//        userProfileDto.setAddressDto(addressDto);
//        List<Address> address = ModelUtils.addressList();
//        List<Bot> botList = ModelUtils.botList();
//        userProfileDto.setBotList(botList);
//        when(modelMapper.map(user, UserProfileDto.class)).thenReturn(userProfileDto);
//        when(userRemoteClient.getPasswordStatus()).thenReturn(new PasswordStatusDto(true));
//        assertEquals(userProfileDto, ubsService.getProfileData(user.getUuid()));
//        for (Bot bot : botList) {
//            assertNotNull(bot);
//        }
//        assertNotNull(addressDto);
//        assertNotNull(userProfileDto);
//        assertNotNull(address);
//    }
//
//    @Test
//    void testFindAllAddressesForCurrentOrder() {
//        String uuid = "35467585763t4sfgchjfuyetf";
//        User user = new User();
//        user.setId(13L);
//        when(userRepository.findByUuid(uuid)).thenReturn(user);
//
//        List<AddressDto> testAddressesDto = getTestAddressesDto();
//
//        OrderWithAddressesResponseDto expected = new OrderWithAddressesResponseDto(testAddressesDto);
//
//        List<Address> addresses = getTestAddresses(user);
//
//        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
//        when(modelMapper.map(addresses.get(0), AddressDto.class)).thenReturn(testAddressesDto.get(0));
//        when(modelMapper.map(addresses.get(1), AddressDto.class)).thenReturn(testAddressesDto.get(1));
//
//        OrderWithAddressesResponseDto actual = ubsService.findAllAddressesForCurrentOrder(uuid);
//
//        assertEquals(actual, expected);
//        verify(userRepository, times(2)).findByUuid(uuid);
//        verify(addressRepository, times(1)).findAllByUserId(user.getId());
//    }
//
//    private List<Address> getTestAddresses(User user) {
//        Address address1 = Address.builder()
//            .addressStatus(AddressStatus.NEW).id(13L).city("Kyiv").district("Svyatoshyn")
//            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Peremohy av.")
//            .user(user).actual(true).coordinates(new Coordinates(12.5, 34.5)).ubsUsers(new LinkedList<>())
//            .build();
//
//        Address address2 = Address.builder()
//            .addressStatus(AddressStatus.NEW).id(42L).city("Lviv").district("Syhiv")
//            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Lvivska st.")
//            .user(user).actual(true).coordinates(new Coordinates(13.5, 36.5)).ubsUsers(new LinkedList<>())
//            .build();
//
//        return Arrays.asList(address1, address2);
//    }
//
//    private List<AddressDto> getTestAddressesDto() {
//        AddressDto addressDto1 = AddressDto.builder().actual(true).id(13L).city("Kyiv").district("Svyatoshyn")
//            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Peremohy av.")
//            .coordinates(new Coordinates(12.5, 34.5)).build();
//
//        AddressDto addressDto2 = AddressDto.builder().actual(true).id(42L).city("Lviv").district("Syhiv")
//            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Lvivska st.")
//            .coordinates(new Coordinates(13.5, 36.5)).build();
//        return Arrays.asList(addressDto1, addressDto2);
//    }
//
//    @Test
//    void testSaveCurrentAddressForOrder() {
//        User user = ModelUtils.getUserForCreate();
//        List<Address> addresses = user.getAddresses();
//        String uuid = user.getUuid();
//        OrderAddressDtoRequest dtoRequest = ModelUtils.getTestOrderAddressLocationDto();
//
//        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
//        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
//        when(googleApiService.getResultFromGeoCode("fake address")).thenReturn(ModelUtils.getGeocodingResult());
//        when(modelMapper.map(any(),
//            eq(OrderAddressDtoRequest.class)))
//                .thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
//
//        addresses.get(0).setActual(false);
//
//        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
//
//        when(addressRepository.save(addresses.get(0))).thenReturn(addresses.get(0));
//        when(modelMapper.map(dtoRequest,
//            Address.class)).thenReturn(new Address());
//        when(modelMapper.map(addresses.get(0),
//            AddressDto.class))
//                .thenReturn(ModelUtils.addressDto());
//
//        addresses.get(0).setAddressStatus(AddressStatus.NEW);
//
//        OrderWithAddressesResponseDto actualWithSearchAddress =
//            ubsService.saveCurrentAddressForOrder(createAddressRequestDto, uuid);
//
//        Assertions.assertEquals(ModelUtils.getAddressDtoResponse(), actualWithSearchAddress);
//        verify(addressRepository, times(1)).save(addresses.get(0));
//    }
//
//    @Test
//    void testSaveCurrentAddressForOrderAlreadyExistException() {
//        User user = ModelUtils.getUserForCreate();
//        List<Address> addresses = user.getAddresses();
//        String uuid = user.getUuid();
//        OrderAddressDtoRequest dtoRequest = ModelUtils.getTestOrderAddressLocationDto();
//
//        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
//        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
//        when(googleApiService.getResultFromGeoCode("fake address"))
//            .thenReturn(ModelUtils.getGeocodingResult());
//        when(modelMapper.map(any(),
//            eq(OrderAddressDtoRequest.class)))
//                .thenReturn(dtoRequest);
//
//        addresses.get(0).setActual(false);
//
//        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
//
//        addresses.get(0).setAddressStatus(AddressStatus.NEW);
//
//        assertThrows(NotFoundException.class,
//            () -> ubsService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));
//    }
//
//    @Test
//    void testUpdateCurrentAddressForOrder() {
//        User user = ModelUtils.getUserForCreate();
//        List<Address> addresses = user.getAddresses();
//        String uuid = user.getUuid();
//        OrderAddressDtoRequest dtoRequest = ModelUtils.getTestOrderAddressLocationDto();
//        dtoRequest.setId(1L);
//        OrderAddressDtoRequest updateAddressRequestDto = ModelUtils.getTestOrderAddressDtoRequest();
//        updateAddressRequestDto.setId(1L);
//        addresses.get(0).setActual(false);
//        addresses.get(0).setAddressStatus(AddressStatus.IN_ORDER);
//        addresses.get(0).setUser(user);
//
//        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
//        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
//        when(googleApiService.getResultFromGeoCode("fake address")).thenReturn(ModelUtils.getGeocodingResult());
//        when(modelMapper.map(any(),
//            eq(OrderAddressDtoRequest.class)))
//                .thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
//        when(addressRepository.findById(user.getId())).thenReturn(Optional.ofNullable(addresses.get(0)));
//        when(modelMapper.map(dtoRequest,
//            Address.class)).thenReturn(addresses.get(0));
//
//        when(addressRepository.save(addresses.get(0))).thenReturn(addresses.get(0));
//        when(modelMapper.map(addresses.get(0),
//            AddressDto.class))
//                .thenReturn(ModelUtils.addressDto());
//
//        OrderWithAddressesResponseDto actualWithSearchAddress =
//            ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
//
//        Assertions.assertNotNull(updateAddressRequestDto.getSearchAddress());
//        Assertions.assertNull(dtoRequest.getSearchAddress());
//        Assertions.assertEquals(ModelUtils.getAddressDtoResponse(), actualWithSearchAddress);
//        verify(googleApiService).getResultFromGeoCode("fake address");
//
//        updateAddressRequestDto.setSearchAddress(null);
//        OrderWithAddressesResponseDto actualWithoutSearchAddress =
//            ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
//        Assertions.assertEquals(ModelUtils.getAddressDtoResponse(), actualWithoutSearchAddress);
//        verify(addressRepository, times(2)).save(addresses.get(0));
//    }
//
//    @Test
//    void testUpdateCurrentAddressForOrderAlreadyExistException() {
//        User user = ModelUtils.getUserForCreate();
//        List<Address> addresses = user.getAddresses();
//        String uuid = user.getUuid();
//        OrderAddressDtoRequest dtoRequest = ModelUtils.getTestOrderAddressLocationDto();
//        dtoRequest.setId(1L);
//        OrderAddressDtoRequest updateAddressRequestDto = ModelUtils.getTestOrderAddressDtoRequest();
//        updateAddressRequestDto.setId(1L);
//        addresses.get(0).setActual(false);
//        addresses.get(0).setAddressStatus(AddressStatus.IN_ORDER);
//        addresses.get(0).setUser(user);
//
//        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
//        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
//        when(googleApiService.getResultFromGeoCode("fake address")).thenReturn(ModelUtils.getGeocodingResult());
//        when(modelMapper.map(any(),
//            eq(OrderAddressDtoRequest.class)))
//                .thenReturn(dtoRequest);
//
//        assertThrows(NotFoundException.class,
//            () -> ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid));
//    }
//
//    @Test
//    void testUpdateCurrentAddressForOrderNotFoundOrderAddressException() {
//        User user = ModelUtils.getUserForCreate();
//        List<Address> addresses = user.getAddresses();
//        String uuid = user.getUuid();
//        OrderAddressDtoRequest dtoRequest = ModelUtils.getTestOrderAddressLocationDto();
//        dtoRequest.setId(1L);
//        OrderAddressDtoRequest updateAddressRequestDto = ModelUtils.getTestOrderAddressDtoRequest();
//        updateAddressRequestDto.setId(1L);
//        addresses.get(0).setActual(false);
//        addresses.get(0).setAddressStatus(AddressStatus.IN_ORDER);
//        addresses.get(0).setUser(user);
//
//        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
//        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
//        when(googleApiService.getResultFromGeoCode("fake address")).thenReturn(ModelUtils.getGeocodingResult());
//        when(modelMapper.map(any(),
//            eq(OrderAddressDtoRequest.class)))
//                .thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
//        when(addressRepository.findById(user.getId())).thenReturn(Optional.empty());
//
//        assertThrows(NotFoundException.class,
//            () -> ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid));
//    }
//
//    @Test
//    void testDeleteCurrentAddressForOrder() {
//        String uuid = "35467585763t4sfgchjfuyetf";
//        User user = new User();
//        user.setId(13L);
//        List<Address> addresses = getTestAddresses(user);
//        Address address = addresses.get(0);
//        List<AddressDto> addressDtos = getTestAddressesDto();
//
//        when(userRepository.findByUuid(uuid)).thenReturn(user);
//        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
//        when(userRepository.findByUuid(uuid)).thenReturn(user);
//        when(addressRepository.save(address)).thenReturn(address);
//
//        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
//        when(modelMapper.map(addresses.get(1), AddressDto.class)).thenReturn(addressDtos.get(1));
//
//        ubsService.deleteCurrentAddressForOrder(address.getId(), uuid);
//        verify(addressRepository, times(1)).save(address);
//    }
//
//    @Test
//    void testDeleteUnexistingAddress() {
//        when(addressRepository.findById(42L)).thenReturn(Optional.empty());
//        assertThrows(NotFoundException.class,
//            () -> ubsService.deleteCurrentAddressForOrder(42L, "35467585763t4sfgchjfuyetf"));
//    }
//
//    @Test
//    void testDeleteDeletedAddress() {
//        Address address = getTestAddresses(getTestUser()).get(0);
//        address.setAddressStatus(AddressStatus.DELETED);
//        when(addressRepository.findById(42L)).thenReturn(Optional.of(address));
//        assertThrows(NotFoundException.class,
//            () -> ubsService.deleteCurrentAddressForOrder(42L, "35467585763t4sfgchjfuyetf"));
//    }
//
//    @Test
//    void testDeleteAddressForWrongUser() {
//        Address address = getTestAddresses(new User()).get(0);
//        when(addressRepository.findById(42L)).thenReturn(Optional.of(address));
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(null);
//        assertThrows(AccessDeniedException.class,
//            () -> ubsService.deleteCurrentAddressForOrder(42L, "35467585763t4sfgchjfuyetf"));
//    }
//
//    @Test
//    void getOrderPaymentDetail() {
//        Order order = getOrder();
//        Certificate certificate = getCertificate();
//        certificate.setOrder(order);
//        order.setCertificates(Set.of(certificate));
//        order.setPayment(List.of(getPayment()));
//
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
//
//        OrderPaymentDetailDto actual = ubsService.getOrderPaymentDetail(1L);
//
//        assertEquals(getOrderPaymentDetailDto(), actual);
//    }
//
//    @Test
//    void getOrderPaymentDetailShouldThrowOrderNotFoundException() {
//        when(orderRepository.findById(any())).thenReturn(Optional.empty());
//        Exception thrown = assertThrows(NotFoundException.class,
//            () -> ubsService.getOrderPaymentDetail(null));
//        assertEquals(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
//    }
//
//    @Test
//    void testGetOrderCancellationReason() {
//        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
//        Order orderDto = ModelUtils.getOrderTest();
//        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(orderDto));
//        assert orderDto != null;
//        when(userRepository.findByUuid(anyString())).thenReturn(orderDto.getUser());
//        OrderCancellationReasonDto result = ubsService.getOrderCancellationReason(1L, anyString());
//
//        assertEquals(dto.getCancellationReason(), result.getCancellationReason());
//        assertEquals(dto.getCancellationComment(), result.getCancellationComment());
//    }
//
//    @Test
//    void getOrderCancellationReasonOrderNotFoundException() {
//        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
//        assertThrows(NotFoundException.class,
//            () -> ubsService.getOrderCancellationReason(1L, "abc"));
//    }
//
//    @Test
//    void getOrderCancellationReasonAccessDeniedException() {
//        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getOrderTest()));
//        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());
//        assertThrows(AccessDeniedException.class,
//            () -> ubsService.getOrderCancellationReason(1L, "abc"));
//    }
//
//    @Test
//    void testUpdateOrderCancellationReason() {
//        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
//        Order orderDto = ModelUtils.getOrderTest();
//        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(orderDto));
//        assert orderDto != null;
//        when(userRepository.findByUuid(anyString())).thenReturn(orderDto.getUser());
//        when(orderRepository.save(any())).thenReturn(orderDto);
//        OrderCancellationReasonDto result = ubsService.updateOrderCancellationReason(1L, dto, anyString());
//
//        assertEquals(dto.getCancellationReason(), result.getCancellationReason());
//        assertEquals(dto.getCancellationComment(), result.getCancellationComment());
//        verify(orderRepository).save(orderDto);
//        verify(orderRepository).findById(1L);
//    }
//
//    @Test
//    void updateOrderCancellationReasonOrderNotFoundException() {
//        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
//        assertThrows(NotFoundException.class,
//            () -> ubsService.updateOrderCancellationReason(1L, null, "abc"));
//    }
//
//    @Test
//    void updateOrderCancellationReasonAccessDeniedException() {
//        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getOrderTest()));
//        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());
//        assertThrows(AccessDeniedException.class,
//            () -> ubsService.updateOrderCancellationReason(1L, null, "abc"));
//    }
//
//    @Test
//    void testGelAllEventsFromOrderByOrderId() {
//        List<Event> orderEvents = ModelUtils.getListOfEvents();
//        when(orderRepository.findById(1L)).thenReturn(ModelUtils.getOrderWithEvents());
//        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(orderEvents);
//        List<EventDto> eventDTOS = orderEvents.stream()
//            .map(event -> modelMapper.map(event, EventDto.class))
//            .collect(Collectors.toList());
//        assertEquals(eventDTOS, ubsService.getAllEventsForOrder(1L, anyString()));
//    }
//
//    @Test
//    void testGelAllEventsFromOrderByOrderIdWithThrowingOrderNotFindException() {
//        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(NotFoundException.class,
//            () -> ubsService.getAllEventsForOrder(1L, "abc"));
//    }
//
//    @Test
//    void testGelAllEventsFromOrderByOrderIdWithThrowingEventsNotFoundException() {
//        when(orderRepository.findById(1L)).thenReturn(ModelUtils.getOrderWithEvents());
//        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(List.of());
//        assertThrows(NotFoundException.class,
//            () -> ubsService.getAllEventsForOrder(1L, "abc"));
//    }
//
//    @Test
//    void saveFullOrderFromLiqPay() {
//        User user = getUserWithLastLocation();
//        user.setCurrentPoints(900);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Order order1 = getOrder();
//        order1.setPayment(new ArrayList<Payment>());
//        Payment payment1 = getPayment();
//        payment1.setId(1L);
//        order1.getPayment().add(payment1);
//
//        HashMap<String, String> value = new HashMap<>();
//        value.put("action", "pay");
//        value.put("amount", "12000");
//        value.put("currency", "UAH");
//        value.put("description", "ubs user");
//        value.put("order_id", "1_1");
//        value.put("version", "3");
//        value.put("public_key", null);
//        value.put("language", "en");
//        value.put("result_url", "rer.com");
//        value.put("paytypes", "card");
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
//        when(modelMapper.map(dto, Order.class)).thenReturn(order);
//        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
//        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
//        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
//        when(liqPayService.getCheckoutResponse(any())).thenReturn("Test");
//
//        assertNotNull(ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf"));
//
//        verify(bagRepository, times(2)).findById(3);
//        verify(ubsUserRepository).findById(1L);
//        verify(modelMapper).map(dto, Order.class);
//        verify(modelMapper).map(dto.getPersonalData(), UBSuser.class);
//        verify(addressRepository).findById(any());
//        verify(orderRepository).findById(any());
//        verify(liqPayService).getCheckoutResponse(any());
//    }
//
//    @Test
//    void testSaveFullOrderFromLiqPayThrowsException() throws InvocationTargetException, IllegalAccessException {
//        Service service = new Service();
//        Courier courier = new Courier();
//        Bag bags = new Bag();
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(900);
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(2);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(100);
//        bag.setFullPrice(1);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Order order1 = getOrder();
//        order1.setPayment(new ArrayList<Payment>());
//        Payment payment1 = getPayment();
//        payment1.setId(1L);
//        order1.getPayment().add(payment1);
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfoWithLimitOfBags()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        assertThrows(BadRequestException.class, () -> {
//            ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf");
//        });
//    }
//
//    @Test
//    void saveFullOrderFromLiqPayThrowNotFoundOrderAddressException() {
//        User user = getUserWithLastLocation();
//        user.setCurrentPoints(900);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Optional<Bag> bag = ModelUtils.getBag();
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Order order1 = getOrder();
//        order1.setPayment(new ArrayList<Payment>());
//        Payment payment1 = getPayment();
//        payment1.setId(1L);
//        order1.getPayment().add(payment1);
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
//        when(bagRepository.findById(3)).thenReturn(bag);
//        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
//        when(modelMapper.map(dto, Order.class)).thenReturn(order);
//        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
//        when(addressRepository.findById(any())).thenReturn(Optional.empty());
//
//        assertThrows(NotFoundException.class, () -> {
//            ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf");
//        });
//    }
//
//    @Test
//    void validateLiqPayPayment() {
//        PaymentResponseDtoLiqPay dto = ModelUtils.getPaymentResponceDto();
//
//        when(encryptionUtil.formingResponseSignatureLiqPay(dto.getData(), null)).thenReturn("Test Signature");
//
//        ubsService.validateLiqPayPayment(dto);
//
//    }
//
//    @Test
//    void validateNotValidLiqPayPayment() {
//        PaymentResponseDtoLiqPay dto = ModelUtils.getPaymentResponceDto();
//
//        when(encryptionUtil.formingResponseSignatureLiqPay(dto.getData(), null)).thenReturn("fdf");
//
//        assertThrows(BadRequestException.class, () -> ubsService.validateLiqPayPayment(dto));
//    }
//
//    @Test
//    void deleteOrder() {
//        Order order = ModelUtils.getOrder();
//        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
//        ubsService.deleteOrder(1L);
//        verify(orderRepository).delete(order);
//    }
//
//    @Test
//    void processOrderFondyClient() throws Exception {
//        Order order = ModelUtils.getOrderCount();
//        HashMap<Integer, Integer> value = new HashMap<>();
//        value.put(1, 22);
//        order.setAmountOfBagsOrdered(value);
//        order.setPointsToUse(100);
//        order.setSumTotalAmountWithoutDiscounts(1000L);
//        order.setCertificates(Set.of(getCertificate()));
//        User user = ModelUtils.getUser();
//        user.setCurrentPoints(100);
//        user.setChangeOfPointsList(new ArrayList<>());
//        order.setUser(user);
//
//        Bag bag = ModelUtils.bagDtoClient();
//        OrderFondyClientDto dto = ModelUtils.getOrderFondyClientDto();
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
//        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.ofNullable(user));
//        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
//
//        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
//        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());
//
//        ubsService.processOrderFondyClient(dto, "uuid");
//
//        verify(encryptionUtil).formRequestSignature(any(), eq(null), eq("1"));
//        verify(fondyClient).getCheckoutResponse(any());
//
//    }
//
//    @Test
//    void proccessOrderLiqpayClient() {
//        Order order = ModelUtils.getOrderCount();
//        HashMap<Integer, Integer> value = new HashMap<>();
//        value.put(1, 22);
//        order.setAmountOfBagsOrdered(value);
//        OrderFondyClientDto dto = getOrderFondyClientDto();
//        Bag bag = ModelUtils.bagDtoClient();
//        User user = ModelUtils.getUser();
//        user.setCurrentPoints(100);
//
//        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
//        when(liqPayService.getCheckoutResponse(any())).thenReturn("TestValue");
//        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.ofNullable(user));
//        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
//        ubsService.proccessOrderLiqpayClient(dto, "uuid");
//
//        verify(orderRepository, times(2)).findById(1L);
//        verify(liqPayService).getCheckoutResponse(any());
//    }
//
//    @Test
//    void saveFullOrderToDBForIF() throws IllegalAccessException {
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(900);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//        dto.setCertificates(Set.of(getCertificate().getCode()));
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Order order1 = getOrder();
//        order1.setPayment(new ArrayList<Payment>());
//        Payment payment1 = getPayment();
//        payment1.setId(1L);
//        order1.getPayment().add(payment1);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
//        when(modelMapper.map(dto, Order.class)).thenReturn(order);
//        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(ModelUtils.getCertificate()));
//        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
//        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
//        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
//        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
//        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());
//
//        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        assertNotNull(result);
//    }
//
//    @Test
//    void saveFullOrderToDBWhenSumToPayeqNull() throws IllegalAccessException {
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(9000);
//        user.setUbsUsers(ModelUtils.getUbsUsers());
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.setPointsToUse(6000);
//        dto.getBags().get(0).setAmount(15);
//        Order order = getOrder();
//        order.setPayment(null);
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser().setId(null);
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
//        when(modelMapper.map(dto, Order.class)).thenReturn(order);
//        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
//        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
//
//        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        assertNotNull(result);
//    }
//
//    @Test
//    void testSaveToDBfromIForIFThrowsException() throws InvocationTargetException, IllegalAccessException {
//        Service service = new Service();
//        Courier courier = new Courier();
//        LocationStatus locationStatus = LocationStatus.ACTIVE;
//        Bag bags = new Bag();
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(900);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(3);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(100);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Order order1 = getOrder();
//        order1.setPayment(new ArrayList<Payment>());
//        Payment payment1 = getPayment();
//        payment1.setId(1L);
//        order1.getPayment().add(payment1);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfoWithLimitOfBags()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        Assertions.assertThrows(BadRequestException.class, () -> {
//            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        });
//    }
//
//    @Test
//    void testCheckSumIfCourierLimitBySumOfOrderForIF1() throws InvocationTargetException, IllegalAccessException {
//        Service service = new Service();
//        Courier courier = new Courier();
//        LocationStatus locationStatus = LocationStatus.ACTIVE;
//        Bag bags = new Bag();
//
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(9000);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(
//                ModelUtils.getTariffInfoWithLimitOfBags()
//                    .setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
//                    .setMinPriceOfOrder(50000L)));
//
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//
//        Assertions.assertThrows(BadRequestException.class, () -> {
//            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        });
//
//    }
//
//    @Test
//    void testCheckSumIfCourierLimitBySumOfOrderForIF2() throws InvocationTargetException, IllegalAccessException {
//        Service service = new Service();
//        Courier courier = new Courier();
//        LocationStatus locationStatus = LocationStatus.ACTIVE;
//        Bag bags = new Bag();
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(9000);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(
//                ModelUtils.getTariffInfoWithLimitOfBags()
//                    .setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
//                    .setMaxPriceOfOrder(500L)));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//
//        Assertions.assertThrows(BadRequestException.class, () -> {
//            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        });
//    }
//
//    @Test
//    void saveFullOrderToDBFromLiqPayForIF() {
//        User user = getUserWithLastLocation();
//        user.setCurrentPoints(900);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Order order1 = getOrder();
//        order1.setPayment(new ArrayList<Payment>());
//        Payment payment1 = getPayment();
//        payment1.setId(1L);
//        order1.getPayment().add(payment1);
//
//        HashMap<String, String> value = new HashMap<>();
//        value.put("action", "pay");
//        value.put("amount", "12000");
//        value.put("currency", "UAH");
//        value.put("description", "ubs user");
//        value.put("order_id", "1_1");
//        value.put("version", "3");
//        value.put("public_key", null);
//        value.put("language", "en");
//        value.put("result_url", "rer.com");
//        value.put("paytypes", "card");
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
//        when(modelMapper.map(dto, Order.class)).thenReturn(order);
//        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
//        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
//        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
//        when(liqPayService.getCheckoutResponse(any())).thenReturn("Test");
//
//        assertNotNull(ubsService.saveFullOrderToDBFromLiqPay(dto, "35467585763t4sfgchjfuyetf"));
//
//        verify(bagRepository, times(2)).findById(3);
//        verify(ubsUserRepository).findById(1L);
//        verify(modelMapper).map(dto, Order.class);
//        verify(modelMapper).map(dto.getPersonalData(), UBSuser.class);
//        verify(addressRepository).findById(any());
//        verify(orderRepository).findById(any());
//        verify(liqPayService).getCheckoutResponse(any());
//    }
//
//    @Test
//    void validatePaymentClientTest() {
//
//        PaymentResponseDto dto = ModelUtils.getPaymentResponseDto();
//
//        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersDto()));
//        when(encryptionUtil.checkIfResponseSignatureIsValid(dto, null)).thenReturn(true);
//
//        ubsService.validatePaymentClient(dto);
//
//        verify(orderRepository, times(2)).findById(1L);
//        verify(encryptionUtil).checkIfResponseSignatureIsValid(dto, null);
//
//    }
//
//    @Test
//    void validatePaymentClientExceptionTest() {
//        PaymentResponseDto dto = ModelUtils.getPaymentResponseDto();
//
//        when(orderRepository.findById(1L))
//            .thenReturn(Optional.ofNullable(ModelUtils.getOrdersDto()));
//
//        assertThrows(BadRequestException.class, () -> ubsService.validatePaymentClient(dto));
//    }
//
//    @Test
//    void getUserPointTest() {
//        when(userRepository.findByUuid("uuid")).thenReturn(User.builder().id(1L).currentPoints(100).build());
//
//        ubsService.getUserPoint("uuid");
//
//        verify(userRepository).findByUuid("uuid");
//    }
//
//    @Test
//    void findAllCurrentPointsForUser() {
//        User user = ModelUtils.getTestUser();
//        user.setCurrentPoints(100);
//        user.getChangeOfPointsList().get(0).setAmount(100);
//
//        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
//
//        AllPointsUserDto pointsDTO = ubsService.findAllCurrentPointsForUser(user.getUuid());
//
//        assertEquals(user.getCurrentPoints(), pointsDTO.getUserBonuses());
//
//        user.setCurrentPoints(null);
//        user.setChangeOfPointsList(null);
//
//        pointsDTO = ubsService.findAllCurrentPointsForUser(user.getUuid());
//
//        assertEquals(0, pointsDTO.getUserBonuses());
//    }
//
//    @Test
//    void getPaymentResponseFromFondy() {
//        Order order = ModelUtils.getOrder();
//        FondyPaymentResponse expected = FondyPaymentResponse.builder()
//            .paymentStatus(order.getPayment().get(0).getResponseStatus())
//            .build();
//
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
//        when(userRepository.findByUuid(order.getUser().getUuid())).thenReturn(order.getUser());
//
//        assertEquals(expected, ubsService.getPaymentResponseFromFondy(1L, order.getUser().getUuid()));
//    }
//
//    @Test
//    void getPaymentResponseFromFondyOrderNotFoundException() {
//        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(NotFoundException.class, () -> {
//            ubsService.getPaymentResponseFromFondy(1L, "abc");
//        });
//    }
//
//    @Test
//    void getPaymentResponseFromFondyPaymentNotFoundException() {
//        Order order = getOrder().setPayment(Collections.emptyList());
//        String uuid = order.getUser().getUuid();
//
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
//        when(userRepository.findByUuid(uuid)).thenReturn(order.getUser());
//
//        assertThrows(NotFoundException.class, () -> {
//            ubsService.getPaymentResponseFromFondy(1L, uuid);
//        });
//    }
//
//    @Test
//    void getPaymentResponseFromFondyAccessDeniedException() {
//        Order order = ModelUtils.getOrder();
//
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
//        when(userRepository.findByUuid(anyString())).thenReturn(ModelUtils.getTestUser());
//
//        assertThrows(AccessDeniedException.class, () -> {
//            ubsService.getPaymentResponseFromFondy(1L, "abc");
//        });
//    }
//
//    @Test
//    void getLiqPayStatusAccessDeniedException() {
//        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(getOrder()));
//        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());
//        assertThrows(AccessDeniedException.class, () -> {
//            ubsService.getLiqPayStatus(1L, "abc");
//        });
//    }
//
//    @Test
//    void getOrderForUserTest() {
//        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
//        OrderPaymentStatusTranslation orderPaymentStatusTranslation = ModelUtils.getOrderPaymentStatusTranslation();
//        OrdersDataForUserDto ordersDataForUserDto = ModelUtils.getOrderStatusDto();
//        BagTranslation translation = ModelUtils.getBagTranslation();
//        Order order = ModelUtils.getOrderTest();
//        User user = ModelUtils.getTestUser();
//        Bag bag = ModelUtils.bagDto();
//
//        List<Bag> bags = new ArrayList<>();
//        List<Order> orderList = new ArrayList<>();
//
//        BagForUserDto bagForUserDto = ordersDataForUserDto.getBags().get(0);
//        bag.setCapacity(120);
//        bag.setFullPrice(1200);
//        order.setAmountOfBagsOrdered(Map.of(1, 10));
//        bags.add(bag);
//        order.setUser(user);
//        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
//        orderList.add(order);
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
//        Page<Order> page = new PageImpl<>(orderList, pageable, 1);
//
//        when(ordersForUserRepository.findAllOrdersByUserUuid(pageable, user.getUuid()))
//            .thenReturn(page);
//        when(bagRepository.findBagByOrderId(order.getId())).thenReturn(bags);
//        when(modelMapper.map(bag, BagForUserDto.class)).thenReturn(bagForUserDto);
//        when(bagTranslationRepository.findBagTranslationByBag(bag)).thenReturn(translation);
//        when(orderStatusTranslationRepository
//            .getOrderStatusTranslationById(order.getOrderStatus().getNumValue()))
//                .thenReturn(Optional.of(orderStatusTranslation));
//        when(orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndTranslationValue(
//            (long) order.getOrderPaymentStatus().getStatusValue()))
//                .thenReturn(orderPaymentStatusTranslation);
//
//        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);
//
//        assertEquals(dto.getTotalElements(), orderList.size());
//        assertEquals(dto.getPage().get(0).getId(), order.getId());
//
//        verify(bagTranslationRepository, times(bags.size())).findBagTranslationByBag(bag);
//        verify(modelMapper, times(bags.size())).map(bag, BagForUserDto.class);
//        verify(bagRepository).findBagByOrderId(order.getId());
//        verify(orderStatusTranslationRepository, times(orderList.size()))
//            .getOrderStatusTranslationById(order.getOrderStatus().getNumValue());
//        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
//            .findByOrderPaymentStatusIdAndTranslationValue(
//                (long) order.getOrderPaymentStatus().getStatusValue());
//        verify(ordersForUserRepository).findAllOrdersByUserUuid(pageable, user.getUuid());
//
//    }
//
//    @Test
//    void senderInfoDtoBuilderTest() {
//        OrderStatusTranslation orderStatusTranslation = ModelUtils.getOrderStatusTranslation();
//        OrderPaymentStatusTranslation orderPaymentStatusTranslation = ModelUtils.getOrderPaymentStatusTranslation();
//        UBSuser ubsuser = ModelUtils.getUBSuserWithoutSender();
//        Order order = ModelUtils.getOrderTest().setUbsUser(ubsuser);
//        User user = ModelUtils.getTestUser();
//        List<Order> orderList = new ArrayList<>();
//        order.setAmountOfBagsOrdered(Map.of(1, 10));
//        order.setUser(user);
//        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
//        orderList.add(order);
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
//        Page<Order> page = new PageImpl<>(orderList, pageable, 1);
//
//        when(ordersForUserRepository.findAllOrdersByUserUuid(pageable, user.getUuid()))
//            .thenReturn(page);
//        when(orderStatusTranslationRepository
//            .getOrderStatusTranslationById(order.getOrderStatus().getNumValue()))
//                .thenReturn(Optional.of(orderStatusTranslation));
//        when(orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndTranslationValue(
//            (long) order.getOrderPaymentStatus().getStatusValue()))
//                .thenReturn(orderPaymentStatusTranslation);
//
//        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);
//        assertEquals(dto.getTotalElements(), orderList.size());
//        assertEquals(dto.getPage().get(0).getId(), order.getId());
//    }
//
//    @Test
//    void getTariffInfoForLocationTest() {
//        var tariff = ModelUtils.getTariffInfo();
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(tariff));
//        OrderCourierPopUpDto dto = ubsService.getTariffInfoForLocation(1L);
//        assertTrue(dto.getOrderIsPresent());
//        verify(modelMapper).map(tariff, TariffsForLocationDto.class);
//    }
//
//    @Test
//    void getInfoForCourierOrderingTest() {
//        var tariff = ModelUtils.getTariffInfo();
//        when(orderRepository.getLastOrderOfUserByUUIDIfExists(anyString()))
//            .thenReturn(Optional.of(ModelUtils.getOrder()));
//        when(tariffsInfoRepository.findTariffsInfoByOrder(anyLong())).thenReturn(tariff);
//        OrderCourierPopUpDto dto = ubsService.getInfoForCourierOrdering("35467585763t4sfgchjfuyetf", Optional.empty());
//        verify(modelMapper).map(tariff, TariffsForLocationDto.class);
//        assertTrue(dto.getOrderIsPresent());
//    }
//
//    @Test
//    void getInfoForCourierOrderingTest2() {
//        var tariff = ModelUtils.getTariffInfo();
//        List<Location> list = ModelUtils.getLocationList();
//        when(locationRepository.findAllActive()).thenReturn(list);
//        when(modelMapper.map(list.get(0), RegionDto.class))
//            .thenReturn(RegionDto.builder().regionId(1L).nameUk("Київська область ").nameEn("Kyiv region").build());
//        when(modelMapper.map(list.get(0), LocationsDtos.class))
//            .thenReturn(LocationsDtos.builder().locationId(1L).nameEn("Kyiv").nameUk("Київ").build());
//        OrderCourierPopUpDto dto = ubsService.getInfoForCourierOrdering("35467585763t4sfgchjfuyetf", Optional.of("w"));
//        assertEquals(1, dto.getAllActiveLocationsDtos().size());
//        assertFalse(dto.getOrderIsPresent());
//
//    }
//
//    @Test
//    void checkIfAddressHasBeenDeletedTest() throws InvocationTargetException, IllegalAccessException {
//
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(900);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user);
//        address.setAddressStatus(AddressStatus.DELETED);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
//        when(modelMapper.map(dto, Order.class)).thenReturn(order);
//        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
//        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
//
//        Assertions.assertThrows(NotFoundException.class, () -> {
//            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        });
//
//    }
//
//    @Test
//    void checkAddressUserTest() throws InvocationTargetException, IllegalAccessException {
//
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(900);
//
//        User user1 = ModelUtils.getUser();
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        UBSuser ubSuser = getUBSuser();
//
//        Address address = ubSuser.getAddress();
//        address.setUser(user1);
//        address.setAddressStatus(AddressStatus.NEW);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
//        when(modelMapper.map(dto, Order.class)).thenReturn(order);
//        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
//        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(address));
//
//        Assertions.assertThrows(NotFoundException.class, () -> {
//            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        });
//
//    }
//
//    @Test
//    void checkIfUserHaveEnoughPointsTest() throws InvocationTargetException, IllegalAccessException {
//
//        User user = ModelUtils.getUserWithLastLocation();
//        user.setCurrentPoints(100);
//
//        OrderResponseDto dto = getOrderResponseDto();
//        dto.getBags().get(0).setAmount(15);
//        Order order = getOrder();
//        user.setOrders(new ArrayList<>());
//        user.getOrders().add(order);
//        user.setChangeOfPointsList(new ArrayList<>());
//
//        Bag bag = new Bag();
//        bag.setCapacity(120);
//        bag.setFullPrice(400);
//
//        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
//        Field merchantId = null;
//        for (Field f : fields) {
//            if (f.getName().equals("merchantId")) {
//                f.setAccessible(true);
//                f.set(ubsService, "1");
//            }
//        }
//
//        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
//        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
//            .thenReturn(Optional.of(ModelUtils.getTariffInfo()));
//        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
//
//        Assertions.assertThrows(BadRequestException.class, () -> {
//            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf");
//        });
//
//    }
//
//    @Test
//    void testCreateUbsUserBasedUserProfileData() {
//        UserProfileDto userProfileDto = ModelUtils.userProfileDto().setRecipientEmail("mail@mail.ua");
//        PersonalDataDto dto = ModelUtils.getPersonalDataDto2();
//        UBSuser ubsUser = getUBSuser().setId(1l);
//
//        when(ubsUserRepository.findByEmail("mail@mail.ua")).thenReturn(Optional.of(ubsUser));
//        when(modelMapper.map(dto, UBSuser.class)).thenReturn(ubsUser);
//
//        ubsService.createUbsUserBasedUserProfileData(userProfileDto, ModelUtils.getUser(), ubsUser.getAddress());
//        verify(ubsUserRepository, times(2)).save(ubsUser);
//    }
//
//    @Test
//    void getLiqPayStatusTest() {
//        Order order = getOrder().setUser(getTestUser());
//        order.getPayment().add(getPayment());
//
//        StatusRequestDtoLiqPay dto = StatusRequestDtoLiqPay.builder()
//            .orderId("1_1")
//            .action("status")
//            .version(3)
//            .build();
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("version", dto.getVersion().toString());
//        response.put("order_id", dto.getOrderId());
//        response.put("create_date", 100l);
//        response.put("end_date", 200l);
//        response.put("sender_commission", 20d);
//        response.put("amount", 20d);
//
//        when(orderRepository.findById(1l)).thenReturn(Optional.of(order));
//        when(userRepository.findByUuid("abc")).thenReturn(getTestUser());
//        when(liqPayService.getPaymentStatus(dto)).thenReturn(response);
//        when(paymentRepository.findAllByOrder(order)).thenReturn(order.getPayment());
//
//        ubsService.getLiqPayStatus(1L, "abc");
//
//        Map<String, Object> res = new HashMap<>();
//        res.put("status", "failure");
//        res.put("version", dto.getVersion().toString());
//        res.put("order_id", dto.getOrderId());
//        res.put("create_date", 100l);
//        res.put("end_date", 200l);
//        res.put("sender_commission", 20d);
//        res.put("amount", 20d);
//
//        when(orderRepository.findById(1l)).thenReturn(Optional.of(order));
//        when(userRepository.findByUuid("abc")).thenReturn(getTestUser());
//        when(liqPayService.getPaymentStatus(dto)).thenReturn(res);
//        when(paymentRepository.findAllByOrder(order)).thenReturn(order.getPayment());
//
//        ubsService.getLiqPayStatus(1L, "abc");
//
//    }
}
