package greencity.service.ubs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;

import greencity.dto.bag.BagOrderDto;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.viber.ViberBot;
import greencity.enums.AddressStatus;
import greencity.enums.CertificateStatus;
import greencity.enums.CourierLimit;
import greencity.enums.LocationStatus;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.TariffStatus;
import greencity.repository.AddressRepository;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViberBotRepository;
import greencity.service.google.GoogleApiService;
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
import greencity.dto.certificate.CertificateDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.EventDto;
import greencity.dto.order.FondyOrderResponse;
import greencity.dto.order.MakeOrderAgainDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderClientDto;
import greencity.dto.order.OrderFondyClientDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;

import static greencity.ModelUtils.*;
import static greencity.ModelUtils.TEST_BAG_LIST;
import static greencity.ModelUtils.TEST_ORDER_ADDRESS_DTO_REQUEST;
import static greencity.ModelUtils.TEST_PAYMENT_LIST;
import static greencity.ModelUtils.getAddress;
import static greencity.ModelUtils.getCertificate;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getMaximumAmountOfAddresses;
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
import static greencity.ModelUtils.getTestOrderAddressLocationDto;
import static greencity.ModelUtils.getTestUser;
import static greencity.ModelUtils.getUBSuser;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserWithLastLocation;
import static greencity.constant.ErrorMessage.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class UBSClientServiceImplTest {
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
    private UserRemoteClient userRemoteClient;
    @Mock
    private FondyClient fondyClient;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private OrderAddressRepository orderAddressRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private UBSClientServiceImpl ubsService;

    @Mock
    private EncryptionUtil encryptionUtil;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventService eventService;
    @Mock
    private OrdersForUserRepository ordersForUserRepository;
    @Mock
    private OrderStatusTranslationRepository orderStatusTranslationRepository;
    @Mock
    private OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private TariffLocationRepository tariffLocationRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private GoogleApiService googleApiService;
    @Mock
    private TelegramBotRepository telegramBotRepository;
    @Mock
    private ViberBotRepository viberBotRepository;

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
    void getFirstPageDataByTariffAndLocationIdShouldReturnExpectedData() {
        var user = getUser();
        var uuid = user.getUuid();

        var tariffLocation = getTariffLocation();

        var tariffsInfo = tariffLocation.getTariffsInfo();
        var tariffsInfoId = tariffsInfo.getId();
        tariffsInfo.setTariffStatus(TariffStatus.ACTIVE);

        var location = tariffLocation.getLocation();
        var locationId = location.getId();

        var bags = getBag1list();
        var bagTranslationDto = getBagTranslationDto();
        var userPointsAndAllBagsDtoExpected = getUserPointsAndAllBagsDto();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));
        when(bagRepository.findBagsByTariffsInfoId(tariffsInfoId)).thenReturn(bags);
        when(modelMapper.map(bags.get(0), BagTranslationDto.class)).thenReturn(bagTranslationDto);

        var userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageDataByTariffAndLocationId(uuid, tariffsInfoId, locationId);

        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags(),
            userPointsAndAllBagsDtoActual.getBags());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().get(0).getId(),
            userPointsAndAllBagsDtoActual.getBags().get(0).getId());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getPoints(),
            userPointsAndAllBagsDtoActual.getPoints());

        verify(userRepository).findUserByUuid(uuid);
        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);
        verify(bagRepository).findBagsByTariffsInfoId(tariffsInfoId);
        verify(modelMapper).map(bags.get(0), BagTranslationDto.class);
    }

    @Test
    void getFirstPageDataByTariffAndLocationIdShouldThrowExceptionWhenTariffLocationDoesNotExist() {
        var user = getUser();
        var uuid = user.getUuid();

        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();

        var expectedErrorMessage = TARIFF_FOR_LOCATION_NOT_EXIST + locationId;

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            uuid, tariffsInfoId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(userRepository).findUserByUuid(uuid);
        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);

        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFirstPageDataByTariffAndLocationIdShouldThrowExceptionWhenLocationDoesNotExist() {
        var user = getUser();
        var uuid = user.getUuid();

        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();

        var expectedErrorMessage = LOCATION_DOESNT_FOUND_BY_ID + locationId;

        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            uuid, tariffsInfoId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(userRepository).findUserByUuid(anyString());
        verify(tariffsInfoRepository).findById(anyLong());
        verify(locationRepository).findById(locationId);

        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFirstPageDataByTariffAndLocationIdShouldThrowExceptionWhenTariffDoesNotExist() {
        var user = getUser();
        var uuid = user.getUuid();

        var tariffsInfo = getTariffInfo();
        var tariffId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();

        var expectedErrorMessage = TARIFF_NOT_FOUND + tariffId;

        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(tariffsInfoRepository.findById(tariffId)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            uuid, tariffId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(userRepository).findUserByUuid(anyString());
        verify(tariffsInfoRepository).findById(tariffId);

        verify(locationRepository, never()).findById(anyLong());
        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), anyLong());
    }

    @Test
    void getFirstPageDataByTariffAndLocationIdShouldThrowExceptionWhenUserDoesNotExist() {
        var user = getUser();
        var uuid = user.getUuid();

        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            uuid, tariffsInfoId, locationId));

        assertEquals(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST, exception.getMessage());

        verify(userRepository).findUserByUuid(uuid);

        verify(tariffsInfoRepository, never()).findById(anyLong());
        verify(locationRepository, never()).findById(anyLong());
        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void checkIfTariffIsAvailableForCurrentLocationThrowExceptionWhenTariffIsDeactivated() {
        var user = getUser();
        var uuid = user.getUuid();

        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();
        tariffsInfo.setTariffStatus(TariffStatus.DEACTIVATED);

        var location = getLocation();
        var locationId = location.getId();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        var exception = assertThrows(BadRequestException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            uuid, tariffsInfoId, locationId));

        assertEquals(TARIFF_OR_LOCATION_IS_DEACTIVATED, exception.getMessage());

        verify(userRepository).findUserByUuid(uuid);
        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);

        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void checkIfTariffIsAvailableForCurrentLocationThrowExceptionWhenLocationIsDeactivated() {
        var user = getUser();
        var uuid = user.getUuid();

        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();
        location.setLocationStatus(LocationStatus.DEACTIVATED);

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        var exception = assertThrows(BadRequestException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            uuid, tariffsInfoId, locationId));

        assertEquals(TARIFF_OR_LOCATION_IS_DEACTIVATED, exception.getMessage());

        verify(userRepository).findUserByUuid(uuid);
        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);

        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void checkIfTariffIsAvailableForCurrentLocationWhenLocationForTariffIsDeactivated() {
        var user = getUser();
        var uuid = user.getUuid();

        var tariffLocation = getTariffLocation();
        tariffLocation.setLocationStatus(LocationStatus.DEACTIVATED);

        var tariffsInfo = tariffLocation.getTariffsInfo();
        var tariffsInfoId = tariffsInfo.getId();
        tariffsInfo.setTariffStatus(TariffStatus.ACTIVE);

        var location = tariffLocation.getLocation();
        var locationId = location.getId();

        var expectedErrorMessage = LOCATION_IS_DEACTIVATED_FOR_TARIFF + tariffsInfoId;

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));

        var exception = assertThrows(BadRequestException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            uuid, tariffsInfoId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(userRepository).findUserByUuid(uuid);
        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);

        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFirstPageDataByOrderIdShouldReturnExpectedData() {
        var user = getUser();
        var uuid = user.getUuid();

        var order = getOrderWithTariffAndLocation();
        var orderId = order.getId();

        var tariffsInfo = order.getTariffsInfo();
        var tariffsInfoId = tariffsInfo.getId();
        var location = order
            .getUbsUser()
            .getOrderAddress()
            .getLocation();
        var tariffLocation = getTariffLocation();

        var bags = getBag1list();
        var bagTranslationDto = getBagTranslationDto();
        var userPointsAndAllBagsDtoExpected = getUserPointsAndAllBagsDto();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));
        when(bagRepository.findBagsByTariffsInfoId(tariffsInfoId)).thenReturn(bags);
        when(modelMapper.map(bags.get(0), BagTranslationDto.class)).thenReturn(bagTranslationDto);

        var userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageDataByOrderId(uuid, orderId);

        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags(),
            userPointsAndAllBagsDtoActual.getBags());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().get(0).getId(),
            userPointsAndAllBagsDtoActual.getBags().get(0).getId());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getPoints(),
            userPointsAndAllBagsDtoActual.getPoints());

        verify(userRepository).findUserByUuid(uuid);
        verify(orderRepository).findById(orderId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);
        verify(bagRepository).findBagsByTariffsInfoId(tariffsInfoId);
        verify(modelMapper).map(bags.get(0), BagTranslationDto.class);
    }

    @Test
    void getFirstPageDataByOrderIdShouldThrowExceptionWhenUserDoesNotExist() {
        var user = getUser();
        var uuid = user.getUuid();

        var order = getOrderWithoutPayment();
        var orderId = order.getId();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByOrderId(
            uuid, orderId));

        assertEquals(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST, exception.getMessage());

        verify(userRepository).findUserByUuid(uuid);

        verify(orderRepository, never()).findById(anyLong());
        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFirstPageDataByOrderIdShouldThrowExceptionWhenOrderDoesNotExist() {
        var user = getUser();
        var uuid = user.getUuid();

        var order = getOrderWithoutPayment();
        var orderId = order.getId();

        var expectedErrorMessage = ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId;

        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByOrderId(
            uuid, orderId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(userRepository).findUserByUuid(anyString());
        verify(orderRepository).findById(orderId);

        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testSaveToDB() throws IllegalAccessException {
        User user = getUserWithLastLocation();
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

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());

        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        Assertions.assertNotNull(result);

    }

    @Test
    void testSaveToDBWShouldThrowBadRequestException() throws IllegalAccessException {
        User user = getUserWithLastLocation();
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

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfoWithLimitOfBagsAndMaxLessThanCountOfBigBag()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(1)).findById(any());
    }

    @Test
    void testSaveToDBWShouldThrowTariffNotFoundExceptionException() {
        User user = getUserWithLastLocation();
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

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
    }

    @Test
    void testSaveToDBWShouldThrowBagNotFoundExceptionException() throws IllegalAccessException {
        User user = getUserWithLastLocation();
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

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository).findById(3);

    }

    @Test
    void testSaveToDBWithoutOrderUnpaid() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto(false);
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));

        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", 1L);
        Assertions.assertNotNull(result);

        verify(userRepository, times(1)).findByUuid("35467585763t4sfgchjfuyetf");
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(2)).findById(any());
        verify(ubsUserRepository, times(1)).findById(anyLong());
        verify(modelMapper, times(1)).map(dto.getPersonalData(), UBSuser.class);
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    void saveToDBFailPaidOrder() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(1000);
        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(5);
        Order order = getOrder();
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        Assertions.assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", 1L));
    }

    @Test
    void testSaveToDBThrowsException() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(3);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfoWithLimitOfBags()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        Assertions.assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));
    }

    @Test
    void getSecondPageData() {
        String uuid = "35467585763t4sfgchjfuyetf";
        PersonalDataDto expected = getOrderResponseDto().getPersonalData();

        User user = getTestUser()
            .setUuid(uuid)
            .setRecipientEmail("mail@mail.ua")
            .setRecipientPhone("067894522")
            .setAlternateEmail("my@email.com");
        List<UBSuser> ubsUser = Arrays.asList(getUBSuser());
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(ubsUser);
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(expected);
        PersonalDataDto actual = ubsService.getSecondPageData("35467585763t4sfgchjfuyetf");

        assertEquals(expected, actual);
    }

    @Test
    void getSecondPageDataWithUserFounded() {

        String uuid = "35467585763t4sfgchjfuyetf";
        PersonalDataDto expected = getOrderResponseDto().getPersonalData();

        User user = getTestUser()
            .setUuid(uuid)
            .setRecipientEmail("mail@mail.ua")
            .setRecipientPhone("067894522")
            .setAlternateEmail("my@email.com");
        List<UBSuser> ubsUser = Collections.singletonList(getUBSuser());
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(ubsUser);
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(expected);
        PersonalDataDto actual = ubsService.getSecondPageData("35467585763t4sfgchjfuyetf");

        assertEquals(expected, actual);

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(ubsUserRepository, times(1)).findUBSuserByUser(any());
        verify(modelMapper, times(1)).map(user, PersonalDataDto.class);
    }

    @Test
    void checkCertificate() {
        Certificate certificate = getCertificate();
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(certificate));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(CertificateDto.builder()
            .code("1111-1234")
            .certificateStatus("ACTIVE")
            .creationDate(LocalDate.now())
            .dateOfUse(LocalDate.now().plusMonths(1))
            .points(10)
            .build());

        assertEquals("ACTIVE", ubsService.checkCertificate("1111-1234").getCertificateStatus());
    }

    @Test
    void checkCertificateUSED() {
        Certificate certificate = getCertificate();
        certificate.setCertificateStatus(CertificateStatus.USED);
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(certificate));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(CertificateDto.builder()
            .code("1111-1234")
            .certificateStatus("USED")
            .creationDate(LocalDate.now())
            .dateOfUse(LocalDate.now().plusMonths(1))
            .points(10)
            .build());

        assertEquals("USED", ubsService.checkCertificate("1111-1234").getCertificateStatus());
    }

    @Test
    void checkCertificateWithNoAvailable() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsService.checkCertificate("randomstring"));
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
            .bagOrderDtoList(
                Arrays.asList(
                    BagOrderDto.builder()
                        .bagId(1)
                        .capacity(10)
                        .price(100)
                        .bagAmount(1)
                        .name("name")
                        .nameEng("nameEng")
                        .build(),
                    BagOrderDto.builder()
                        .bagId(2)
                        .capacity(10)
                        .price(100)
                        .name("name")
                        .nameEng("nameEng")
                        .build()))
            .build();
        Order order = getOrderDoneByUser();
        order.setAmountOfBagsOrdered(Collections.singletonMap(1, 1));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findAllByOrder(dto.getOrderId())).thenReturn(getBag4list());
        MakeOrderAgainDto result = ubsService.makeOrderAgain(new Locale("en"), 1L);

        assertEquals(dto, result);
        verify(orderRepository, times(1)).findById(1L);
        verify(bagRepository, times(1)).findAllByOrder(any());
    }

    @Test
    void makeOrderAgainShouldThrowOrderNotFoundException() {
        Locale locale = new Locale("en");
        Exception thrown = assertThrows(NotFoundException.class,
            () -> ubsService.makeOrderAgain(locale, 1L));
        assertEquals(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    void makeOrderAgainShouldThrowBadOrderStatusException() {
        Order order = getOrderDoneByUser();
        order.setOrderStatus(OrderStatus.CANCELED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Locale locale = new Locale("en");
        Exception thrown = assertThrows(BadRequestException.class,
            () -> ubsService.makeOrderAgain(locale, 1L));
        assertEquals(thrown.getMessage(), ErrorMessage.BAD_ORDER_STATUS_REQUEST
            + order.getOrderStatus());
    }

    @Test
    void findUserByUuid() {
        String uuid = "87df9ad5-6393-441f-8423-8b2e770b01a8";
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(getUser()));
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
        User user = getUser();
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        ubsService.markUserAsDeactivated(1L);
        verify(userRepository).findById(1L);
        verify(userRemoteClient).markUserDeactivated(user.getUuid());
    }

    @Test
    void getsUserAndUserUbsAndViolationsInfoByOrderId() {
        UserInfoDto expectedResult = getUserInfoDto();
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
    void getsUserAndUserUbsAndViolationsInfoByOrderIdWithoutSender() {
        UserInfoDto expectedResult = getUserInfoDto();
        expectedResult.setRecipientId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrderDetailsWithoutSender()));
        when(userRepository.findByUuid(anyString())).thenReturn(getOrderDetailsWithoutSender().getUser());
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
        assertThrows(NotFoundException.class,
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
    void updatesUbsUserInfoInOrder2() {
        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
            .recipientId(1L)
            .build();

        Optional<UBSuser> user = Optional.of(getUBSuser());
        when(ubsUserRepository.findById(1L)).thenReturn(user);
        when(ubsUserRepository.save(user.get())).thenReturn(user.get());

        UbsCustomersDto expected = UbsCustomersDto.builder()
            .name("oleh ivanov")
            .email("mail@mail.ua")
            .phoneNumber("067894522")
            .build();

        UbsCustomersDto actual = ubsService.updateUbsUserInfoInOrder(request, "abc");
        assertEquals(expected, actual);
    }

    @Test
    void updatesUbsUserInfoInOrderShouldThrowUBSuserNotFoundException() {
        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
            .recipientId(1L)
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
        UbsCustomersDtoUpdate request = UbsCustomersDtoUpdate.builder()
            .recipientId(1L)
            .recipientName("Anatolii")
            .recipientSurName("Anatolii")
            .recipientEmail("anatolii.andr@gmail.com")
            .recipientPhoneNumber("095123456").build();

        Optional<UBSuser> user = Optional.of(getUBSuser());
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
    void updateProfileData() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        User user = getUserWithBotNotifyTrue();
        TelegramBot telegramBot = getTelegramBotNotifyTrue();
        ViberBot viberBot = getViberBotNotifyTrue();
        List<AddressDto> addressDto = addressDtoList();
        List<Bot> botList = botList();
        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDto();
        String uuid = UUID.randomUUID().toString();
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(telegramBotRepository.findByUser(user)).thenReturn(Optional.of(telegramBot));
        when(viberBotRepository.findByUser(user)).thenReturn(Optional.of(viberBot));
        when(modelMapper.map(addressDto.get(0), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        when(modelMapper.map(addressDto.get(1), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        doReturn(new OrderWithAddressesResponseDto())
            .when(ubsClientService).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        ubsClientService.updateProfileData(uuid, userProfileUpdateDto);

        for (Bot bot : botList) {
            Assertions.assertNotNull(bot);
        }
        Assertions.assertNotNull(userProfileUpdateDto.getAddressDto());
        Assertions.assertNotNull(userProfileUpdateDto);
        Assertions.assertNotNull(addressDto);
        Assertions.assertTrue(userProfileUpdateDto.getTelegramIsNotify());
        Assertions.assertTrue(userProfileUpdateDto.getViberIsNotify());

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramBotRepository).findByUser(user);
        verify(viberBotRepository).findByUser(user);
        verify(modelMapper).map(addressDto.get(0), OrderAddressDtoRequest.class);
        verify(modelMapper).map(addressDto.get(1), OrderAddressDtoRequest.class);
        verify(ubsClientService, times(2)).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        verify(userRepository).save(user);
        verify(modelMapper).map(user, UserProfileUpdateDto.class);
    }

    @Test
    void updateProfileDataThrowNotFoundException() {
        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDto();
        String uuid = UUID.randomUUID().toString();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsService.updateProfileData(uuid, userProfileUpdateDto));
        verify(userRepository).findUserByUuid(uuid);

    }

    @Test
    void updateProfileDataIfTelegramBotNotExists() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        User user = getUserWithBotNotifyTrue();
        List<AddressDto> addressDto = addressDtoList();
        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDtoWithBotsIsNotifyFalse();
        String uuid = UUID.randomUUID().toString();
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(telegramBotRepository.findByUser(user)).thenReturn(Optional.empty());
        when(viberBotRepository.findByUser(user)).thenReturn(Optional.empty());
        when(modelMapper.map(addressDto.get(0), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        when(modelMapper.map(addressDto.get(1), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        doReturn(new OrderWithAddressesResponseDto())
            .when(ubsClientService).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        ubsClientService.updateProfileData(uuid, userProfileUpdateDto);

        assertFalse(userProfileUpdateDto.getTelegramIsNotify());

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramBotRepository).findByUser(user);
        verify(viberBotRepository).findByUser(user);
        verify(modelMapper).map(addressDto.get(0), OrderAddressDtoRequest.class);
        verify(modelMapper).map(addressDto.get(1), OrderAddressDtoRequest.class);
        verify(ubsClientService, times(2)).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
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
        assertEquals(userProfileDto, ubsService.getProfileData(uuid));
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
            () -> ubsService.getProfileData(uuid));
        verify(userRepository).findUserByUuid(uuid);
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

        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(modelMapper.map(addresses.get(0), AddressDto.class)).thenReturn(testAddressesDto.get(0));
        when(modelMapper.map(addresses.get(1), AddressDto.class)).thenReturn(testAddressesDto.get(1));

        OrderWithAddressesResponseDto actual = ubsService.findAllAddressesForCurrentOrder(uuid);

        assertEquals(actual, expected);
        verify(userRepository, times(1)).findByUuid(uuid);
        verify(addressRepository, times(1)).findAllNonDeletedAddressesByUserId(user.getId());
    }

    private List<Address> getTestAddresses(User user) {
        Address address1 = Address.builder()
            .addressStatus(AddressStatus.NEW).id(13L).city("Kyiv").district("Svyatoshyn")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Peremohy av.")
            .user(user).actual(true).coordinates(new Coordinates(12.5, 34.5))
            .build();

        Address address2 = Address.builder()
            .addressStatus(AddressStatus.NEW).id(42L).city("Lviv").district("Syhiv")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Lvivska st.")
            .user(user).actual(true).coordinates(new Coordinates(13.5, 36.5))
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
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        CreateAddressRequestDto createAddressRequestDto = getAddressRequestDto();
        Address addressToSave = new Address();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResult().get(0));
        when(modelMapper.map(any(),
            eq(OrderAddressDtoRequest.class)))
                .thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
        when(modelMapper.map(any(),
            eq(Address.class))).thenReturn(addressToSave);
        when(modelMapper.map(addresses.get(0),
            AddressDto.class))
                .thenReturn(addressDto());

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.saveCurrentAddressForOrder(createAddressRequestDto, uuid);

        Assertions.assertEquals(getAddressDtoResponse(), actualWithSearchAddress);
        verify(addressRepository).save(addressToSave);
    }

    @Test
    void testSaveCurrentAddressForOrderAlreadyExistException() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();

        CreateAddressRequestDto createAddressRequestDto = getAddressRequestDto();

        addresses.get(0).setAddressStatus(AddressStatus.NEW);
        addresses.get(0).setActual(false);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResult().get(0));
        dtoRequest.setPlaceId(null);
        when(modelMapper.map(any(),
            eq(OrderAddressDtoRequest.class)))
                .thenReturn(dtoRequest);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));

        assertEquals(ADDRESS_ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    void testSaveCurrentAddressForMaximumNumbersOfOrdersAddressesException() {
        User user = getUserForCreate();
        List<Address> addresses = getMaximumAmountOfAddresses();
        String uuid = user.getUuid();
        CreateAddressRequestDto createAddressRequestDto = getAddressRequestDto();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));

        assertEquals(ErrorMessage.NUMBER_OF_ADDRESSES_EXCEEDED, exception.getMessage());
    }

    @Test
    void testUpdateCurrentAddressForOrder() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.IN_ORDER);
        addresses.get(0).setUser(user);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode(eq(updateAddressRequestDto.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResult().get(0));
        when(modelMapper.map(any(),
            eq(OrderAddressDtoRequest.class)))
                .thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
        when(addressRepository.findById(updateAddressRequestDto.getId()))
            .thenReturn(Optional.ofNullable(addresses.get(0)));
        when(modelMapper.map(any(),
            eq(Address.class))).thenReturn(addresses.get(0));

        when(addressRepository.save(addresses.get(0))).thenReturn(addresses.get(0));
        when(modelMapper.map(addresses.get(0),
            AddressDto.class))
                .thenReturn(addressDto());

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);

        Assertions.assertNotNull(updateAddressRequestDto.getSearchAddress());
        Assertions.assertNull(dtoRequest.getSearchAddress());
        Assertions.assertEquals(getAddressDtoResponse(), actualWithSearchAddress);

        verify(googleApiService, times(2)).getResultFromGeoCode(eq(updateAddressRequestDto.getPlaceId()),
            anyInt());

        updateAddressRequestDto.setSearchAddress(null);
        OrderWithAddressesResponseDto actualWithoutSearchAddress =
            ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        Assertions.assertEquals(getAddressDtoResponse(), actualWithoutSearchAddress);
        verify(addressRepository, times(2)).save(addresses.get(0));
    }

    @Test
    void testUpdateCurrentAddressForOrderWithNoAddress() {
        User user = getUserForCreate(AddressStatus.DELETED);
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.get(0).setActual(false);
        addresses.get(0).setUser(user);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findById(updateAddressRequestDto.getId()))
            .thenReturn(Optional.ofNullable(addresses.get(0)));
        when(googleApiService.getResultFromGeoCode(eq(updateAddressRequestDto.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResult().get(0));

        when(addressRepository.findById(updateAddressRequestDto.getId()))
            .thenReturn(Optional.ofNullable(addresses.get(0)));
        when(modelMapper.map(any(),
            eq(Address.class))).thenReturn(addresses.get(0));

        when(addressRepository.save(addresses.get(0))).thenReturn(addresses.get(0));

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);

        Assertions.assertNotNull(updateAddressRequestDto.getSearchAddress());
        Assertions.assertNull(dtoRequest.getSearchAddress());
        Assertions.assertEquals(OrderWithAddressesResponseDto.builder().addressList(Collections.emptyList()).build(),
            actualWithSearchAddress);

        verify(googleApiService, times(2)).getResultFromGeoCode(eq(updateAddressRequestDto.getPlaceId()),
            anyInt());
        verify(addressRepository, times(1)).save(addresses.get(0));
        verify(addressRepository, times(1)).findById(anyLong());
        verify(modelMapper, times(1)).map(any(), eq(Address.class));
    }

    @Test
    void testUpdateCurrentAddressForOrderAlreadyExistException() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        dtoRequest.setPlaceId(null);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.IN_ORDER);
        addresses.get(0).setUser(user);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findById(updateAddressRequestDto.getId()))
            .thenReturn(Optional.ofNullable(addresses.get(0)));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode(eq(updateAddressRequestDto.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResult().get(0));
        when(modelMapper.map(any(),
            eq(OrderAddressDtoRequest.class)))
                .thenReturn(dtoRequest);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid));

        assertEquals(ADDRESS_ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    void testUpdateCurrentAddressForOrderNotFoundOrderAddressException() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.IN_ORDER);
        addresses.get(0).setUser(user);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findById(updateAddressRequestDto.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid));

        assertEquals(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + updateAddressRequestDto.getId(), exception.getMessage());
    }

    @Test
    void testUpdateCurrentAddressForOrderThrowsAccessDeniedException() {
        long addressId = 1L;
        long userId = 2L;

        User user = getUserForCreate();
        user.setId(userId);

        Address address = new Address();
        address.setId(addressId);
        address.setUser(new User());
        address.getUser().setId(userId + 1);

        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(addressId);

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> ubsService.updateCurrentAddressForOrder(dtoRequest, uuid));

        assertEquals(CANNOT_ACCESS_PERSONAL_INFO, exception.getMessage());
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

        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(List.of(addresses.get(1)));
        when(modelMapper.map(addresses.get(1), AddressDto.class)).thenReturn(addressDtos.get(1));

        ubsService.deleteCurrentAddressForOrder(address.getId(), uuid);
        verify(addressRepository).save(address);
    }

    @Test
    void testDeleteUnexistingAddress() {
        when(addressRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsService.deleteCurrentAddressForOrder(42L, "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void testDeleteDeletedAddress() {
        String uuid = "35467585763t4sfgchjfuyetf";
        Address address = getTestAddresses(getTestUser()).get(0);
        address.setAddressStatus(AddressStatus.DELETED);

        when(userRepository.findByUuid(uuid)).thenReturn(getTestUser());
        when(addressRepository.findById(42L)).thenReturn(Optional.of(address));

        assertThrows(BadRequestException.class,
            () -> ubsService.deleteCurrentAddressForOrder(42L, uuid));
    }

    @Test
    void testDeleteAddressForWrongUser() {
        long addressId = 42L;
        long userId = 2L;
        User user = new User();
        user.setId(userId);
        User addressOwnerUser = new User();
        addressOwnerUser.setId(userId + 1);
        String uuid = "35467585763t4sfgchjfuyetf";
        Address address = getTestAddresses(addressOwnerUser).get(0);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        assertThrows(AccessDeniedException.class,
            () -> ubsService.deleteCurrentAddressForOrder(addressId, uuid));
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
        Exception thrown = assertThrows(NotFoundException.class,
            () -> ubsService.getOrderPaymentDetail(null));
        assertEquals(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    void testGetOrderCancellationReason() {
        OrderCancellationReasonDto dto = getCancellationDto();
        Order orderDto = getOrderTest();
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
        assertThrows(NotFoundException.class,
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
        OrderCancellationReasonDto dto = getCancellationDto();
        Order orderDto = getOrderTest();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(orderDto));
        assert orderDto != null;
        when(userRepository.findByUuid(anyString())).thenReturn(orderDto.getUser());
        when(orderRepository.save(any())).thenReturn(orderDto);
        OrderCancellationReasonDto result = ubsService.updateOrderCancellationReason(1L, dto, anyString());

        verify(eventService, times(1))
            .saveEvent("Статус Замовлення - Скасовано", "", orderDto);
        assertEquals(dto.getCancellationReason(), result.getCancellationReason());
        assertEquals(dto.getCancellationComment(), result.getCancellationComment());
        verify(orderRepository).save(orderDto);
        verify(orderRepository).findById(1L);
    }

    @Test
    void updateOrderCancellationReasonOrderNotFoundException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
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
        List<Event> orderEvents = getListOfEvents();
        when(orderRepository.findById(1L)).thenReturn(getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(orderEvents);
        List<EventDto> eventDTOS = orderEvents.stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(Collectors.toList());
        assertEquals(eventDTOS, ubsService.getAllEventsForOrder(1L, anyString()));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithThrowingOrderNotFindException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsService.getAllEventsForOrder(1L, "abc"));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithThrowingEventsNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(Collections.emptyList());
        assertThrows(NotFoundException.class,
            () -> ubsService.getAllEventsForOrder(1L, "abc"));
    }

    @Test
    void deleteOrder() {
        Order order = getOrder();
        when(ordersForUserRepository.getAllByUserUuidAndId(order.getUser().getUuid(), order.getId()))
            .thenReturn(order);

        ubsService.deleteOrder(order.getUser().getUuid(), 1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrderFail() {
        Order order = getOrder();
        when(ordersForUserRepository.getAllByUserUuidAndId(order.getUser().getUuid(), order.getId()))
            .thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            ubsService.deleteOrder("UUID", 1L);
        });
    }

    @Test
    void processOrderFondyClient() throws Exception {
        Order order = getOrderCount();
        HashMap<Integer, Integer> value = new HashMap<>();
        value.put(1, 22);
        order.setAmountOfBagsOrdered(value);
        order.setPointsToUse(100);
        order.setSumTotalAmountWithoutDiscounts(1000L);
        order.setCertificates(Set.of(getCertificate()));
        order.setPayment(TEST_PAYMENT_LIST);
        User user = getUser();
        user.setCurrentPoints(100);
        user.setChangeOfPointsList(new ArrayList<>());
        order.setUser(user);

        OrderFondyClientDto dto = getOrderFondyClientDto();
        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        Certificate certificate = getCertificate();
        CertificateDto certificateDto = createCertificateDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(user));

        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());
        when(bagRepository.findBagsByOrderId(order.getId())).thenReturn(TEST_BAG_LIST);
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(certificateDto);

        ubsService.processOrderFondyClient(dto, "uuid");

        verify(encryptionUtil).formRequestSignature(any(), eq(null), eq("1"));
        verify(fondyClient).getCheckoutResponse(any());

    }

    @Test
    void processOrderFondyClientFailPaidOrder() {
        Order order = getOrderCountWithPaymentStatusPaid();
        OrderFondyClientDto dto = getOrderFondyClientDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
        Assertions.assertThrows(BadRequestException.class, () -> ubsService.processOrderFondyClient(dto, "uuid"));
    }

    @Test
    void saveFullOrderToDBForIF() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(15);
        dto.setCertificates(Set.of(getCertificate().getCode()));
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(getCertificate()));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(encryptionUtil.formRequestSignature(any(), eq(null), eq("1"))).thenReturn("TestValue");
        when(fondyClient.getCheckoutResponse(any())).thenReturn(getSuccessfulFondyResponse());

        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        Assertions.assertNotNull(result);
    }

    @Test
    void saveFullOrderToDBWhenSumToPayeqNull() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(9000);
        user.setUbsUsers(getUbsUsers());

        OrderResponseDto dto = getOrderResponseDto();
        dto.setAddressId(1L);
        dto.setPointsToUse(6000);
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        order.setPayment(null);
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser().setId(null);

        Address address = getAddress();
        address.setUser(user);

        var location = getLocation();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setLocation(location);

        orderAddress.setAddressStatus(AddressStatus.NEW);
        address.setAddressStatus(AddressStatus.NEW);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(addressRepository.findById(any())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);

        FondyOrderResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        Assertions.assertNotNull(result);
    }

    @Test
    void testSaveToDBfromIForIFThrowsException() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(3);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(100);
        bag.setFullPrice(400);

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfoWithLimitOfBags()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        Assertions.assertThrows(BadRequestException.class, () -> {
            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        });
    }

    @Test
    void testCheckSumIfCourierLimitBySumOfOrderForIF1() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(9000);

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

        OrderAddress address = ubSuser.getOrderAddress();
        address.setAddressStatus(AddressStatus.NEW);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(
                getTariffInfoWithLimitOfBags()
                    .setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
                    .setMin(50000L)));

        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));

        Assertions.assertThrows(BadRequestException.class, () -> {
            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        });

    }

    @Test
    void testCheckSumIfCourierLimitBySumOfOrderForIF2() throws InvocationTargetException, IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(9000);

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

        OrderAddress address = ubSuser.getOrderAddress();
        address.setAddressStatus(AddressStatus.NEW);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(
                getTariffInfoWithLimitOfBags()
                    .setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
                    .setMax(500L)));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));

        Assertions.assertThrows(BadRequestException.class, () -> {
            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        });
    }

    @Test
    void validatePaymentClientTest() {

        PaymentResponseDto dto = getPaymentResponseDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrdersDto()));
        when(encryptionUtil.checkIfResponseSignatureIsValid(dto, null)).thenReturn(true);

        ubsService.validatePaymentClient(dto);

        verify(orderRepository, times(2)).findById(1L);
        verify(encryptionUtil).checkIfResponseSignatureIsValid(dto, null);

    }

    @Test
    void validatePaymentClientExceptionTest() {
        PaymentResponseDto dto = getPaymentResponseDto();

        when(orderRepository.findById(1L))
            .thenReturn(Optional.ofNullable(getOrdersDto()));

        assertThrows(BadRequestException.class, () -> ubsService.validatePaymentClient(dto));
    }

    @Test
    void getUserPointTest() {
        when(userRepository.findByUuid("uuid")).thenReturn(User.builder().id(1L).currentPoints(100).build());

        ubsService.getUserPoint("uuid");

        verify(userRepository).findByUuid("uuid");
    }

    @Test
    void findAllCurrentPointsForUser() {
        User user = getTestUser();
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
        Order order = getOrder();
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
        assertThrows(NotFoundException.class, () -> {
            ubsService.getPaymentResponseFromFondy(1L, "abc");
        });
    }

    @Test
    void getPaymentResponseFromFondyPaymentNotFoundException() {
        Order order = getOrder().setPayment(Collections.emptyList());
        String uuid = order.getUser().getUuid();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUuid(uuid)).thenReturn(order.getUser());

        assertThrows(NotFoundException.class, () -> {
            ubsService.getPaymentResponseFromFondy(1L, uuid);
        });
    }

    @Test
    void getPaymentResponseFromFondyAccessDeniedException() {
        Order order = getOrder();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUuid(anyString())).thenReturn(getTestUser());

        assertThrows(AccessDeniedException.class, () -> {
            ubsService.getPaymentResponseFromFondy(1L, "abc");
        });
    }

    @Test
    void getOrderForUserTest() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        OrdersDataForUserDto ordersDataForUserDto = getOrderStatusDto();
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();

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

        when(ordersForUserRepository.getAllByUserUuidAndId(user.getUuid(), order.getId()))
            .thenReturn(order);
        when(bagRepository.findBagsByOrderId(order.getId())).thenReturn(bags);
        when(modelMapper.map(bag, BagForUserDto.class)).thenReturn(bagForUserDto);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
                .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
                .thenReturn(orderPaymentStatusTranslation);

        ubsService.getOrderForUser(user.getUuid(), 1L);

        verify(modelMapper, times(bags.size())).map(bag, BagForUserDto.class);
        verify(bagRepository).findBagsByOrderId(order.getId());
        verify(orderStatusTranslationRepository, times(orderList.size()))
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue());
        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
            .getById(
                (long) order.getOrderPaymentStatus().getStatusValue());
    }

    @Test
    void getOrderForUserFail() {
        Order order = getOrderTest();
        User user = getTestUser();

        when(ordersForUserRepository.getAllByUserUuidAndId("UUID", order.getId()))
            .thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            ubsService.getOrderForUser("UUID", 1L);
        });
    }

    @Test
    void getOrdersForUserTest() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        OrdersDataForUserDto ordersDataForUserDto = getOrderStatusDto();
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();

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
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(bagRepository.findBagsByOrderId(order.getId())).thenReturn(bags);
        when(modelMapper.map(bag, BagForUserDto.class)).thenReturn(bagForUserDto);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
                .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
                .thenReturn(orderPaymentStatusTranslation);

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());

        verify(modelMapper, times(bags.size())).map(bag, BagForUserDto.class);
        verify(bagRepository).findBagsByOrderId(order.getId());
        verify(orderStatusTranslationRepository, times(orderList.size()))
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue());
        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
            .getById(
                (long) order.getOrderPaymentStatus().getStatusValue());
        verify(ordersForUserRepository).getAllByUserUuid(pageable, user.getUuid());
    }

    @Test
    void testOrdersForUserWithExportedQuantity() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        OrdersDataForUserDto ordersDataForUserDto = getOrderStatusDto();
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();

        List<Bag> bags = new ArrayList<>();
        List<Order> orderList = new ArrayList<>();

        BagForUserDto bagForUserDto = ordersDataForUserDto.getBags().get(0);
        bag.setCapacity(120);
        bag.setFullPrice(1200);
        order.setExportedQuantity(Map.of(1, 10));
        bags.add(bag);
        order.setUser(user);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(bagRepository.findBagsByOrderId(order.getId())).thenReturn(bags);
        when(modelMapper.map(bag, BagForUserDto.class)).thenReturn(bagForUserDto);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
                .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
                .thenReturn(orderPaymentStatusTranslation);

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());

        verify(modelMapper, times(bags.size())).map(bag, BagForUserDto.class);
        verify(bagRepository).findBagsByOrderId(order.getId());
        verify(orderStatusTranslationRepository, times(orderList.size()))
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue());
        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
            .getById(
                (long) order.getOrderPaymentStatus().getStatusValue());
        verify(ordersForUserRepository).getAllByUserUuid(pageable, user.getUuid());
    }

    @Test
    void testOrdersForUserWithConfirmedQuantity() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        OrdersDataForUserDto ordersDataForUserDto = getOrderStatusDto();
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();

        List<Bag> bags = new ArrayList<>();
        List<Order> orderList = new ArrayList<>();

        BagForUserDto bagForUserDto = ordersDataForUserDto.getBags().get(0);
        bag.setCapacity(120);
        bag.setFullPrice(1200);
        order.setConfirmedQuantity(Map.of(1, 10));
        bags.add(bag);
        order.setUser(user);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(bagRepository.findBagsByOrderId(order.getId())).thenReturn(bags);
        when(modelMapper.map(bag, BagForUserDto.class)).thenReturn(bagForUserDto);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
                .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
                .thenReturn(orderPaymentStatusTranslation);

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());

        verify(modelMapper, times(bags.size())).map(bag, BagForUserDto.class);
        verify(bagRepository).findBagsByOrderId(order.getId());
        verify(orderStatusTranslationRepository, times(orderList.size()))
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue());
        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
            .getById(
                (long) order.getOrderPaymentStatus().getStatusValue());
        verify(ordersForUserRepository).getAllByUserUuid(pageable, user.getUuid());
    }

    @Test
    void senderInfoDtoBuilderTest() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        UBSuser ubsuser = getUBSuserWithoutSender();
        Order order = getOrderTest().setUbsUser(ubsuser);
        User user = getTestUser();
        List<Order> orderList = new ArrayList<>();
        order.setAmountOfBagsOrdered(Map.of(1, 10));
        order.setUser(user);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
                .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
                .thenReturn(orderPaymentStatusTranslation);

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);
        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());
    }

    @Test
    void getTariffInfoForLocationTest() {
        var tariff = getTariffInfo();
        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariff));
        OrderCourierPopUpDto dto = ubsService.getTariffInfoForLocation(1L);
        assertTrue(dto.getOrderIsPresent());
        verify(modelMapper).map(tariff, TariffsForLocationDto.class);
    }

    @Test
    void getInfoForCourierOrderingTest() {
        var tariff = getTariffInfo();
        when(orderRepository.getLastOrderOfUserByUUIDIfExists(anyString()))
            .thenReturn(Optional.of(getOrder()));
        when(tariffsInfoRepository.findTariffsInfoByOrdersId(anyLong())).thenReturn(tariff);
        OrderCourierPopUpDto dto = ubsService.getInfoForCourierOrdering("35467585763t4sfgchjfuyetf", Optional.empty());
        verify(modelMapper).map(tariff, TariffsForLocationDto.class);
        assertTrue(dto.getOrderIsPresent());
    }

    @Test
    void getInfoForCourierOrderingTest2() {
        List<Location> list = getLocationList();
        when(locationRepository.findAllActive()).thenReturn(list);
        when(modelMapper.map(list.get(0), RegionDto.class))
            .thenReturn(RegionDto.builder().regionId(1L).nameUk("Київська область ").nameEn("Kyiv region").build());
        when(modelMapper.map(list.get(0), LocationsDtos.class))
            .thenReturn(LocationsDtos.builder().locationId(1L).nameEn("Kyiv").nameUk("Київ").build());
        OrderCourierPopUpDto dto = ubsService.getInfoForCourierOrdering("35467585763t4sfgchjfuyetf", Optional.of("w"));
        assertEquals(1, dto.getAllActiveLocationsDtos().size());
        assertFalse(dto.getOrderIsPresent());

    }

    @Test
    void checkIfAddressHasBeenDeletedTest() throws IllegalAccessException {

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

        OrderAddress address = ubSuser.getOrderAddress();
        address.setAddressStatus(AddressStatus.DELETED);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);

        Assertions.assertThrows(NotFoundException.class, () -> {
            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        });

    }

    @Test
    void checkAddressUserTest() throws IllegalAccessException {

        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        User user1 = getUser();

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

        OrderAddress address = ubSuser.getOrderAddress();
        address.setAddressStatus(AddressStatus.NEW);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);

        Assertions.assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

    }

    @Test
    void checkIfUserHaveEnoughPointsTest() throws IllegalAccessException {

        User user = getUserWithLastLocation();
        user.setCurrentPoints(100);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = new Bag();
        bag.setCapacity(120);
        bag.setFullPrice(400);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findById(3)).thenReturn(Optional.of(bag));

        Assertions.assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));
    }

    @Test
    void getTariffForOrderTest() {
        TariffsInfo tariffsInfo = getTariffInfo();
        when(tariffsInfoRepository.findByOrdersId(anyLong())).thenReturn(Optional.of(tariffsInfo));
        when(modelMapper.map(tariffsInfo, TariffsForLocationDto.class))
            .thenReturn(getTariffsForLocationDto());
        var dto = ubsService.getTariffForOrder(1L);
        verify(tariffsInfoRepository, times(1)).findByOrdersId(anyLong());
        verify(modelMapper).map(tariffsInfo, TariffsForLocationDto.class);
    }

    @Test
    void getTariffForOrderFailTest() {
        when(tariffsInfoRepository.findByOrdersId(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> ubsService.getTariffForOrder(1L));
    }

    @Test
    void getAllAuthorities() {
        Set<String> authorities = new HashSet<>();
        when(userRemoteClient.getAllAuthorities(anyString())).thenReturn(authorities);
        userRemoteClient.getAllAuthorities("test@mail.com");
        verify(userRemoteClient, times(1)).getAllAuthorities("test@mail.com");
    }

    @Test
    void updateEmployeesAuthorities() {
        UserEmployeeAuthorityDto dto = ModelUtils.getUserEmployeeAuthorityDto();
        userRemoteClient.updateEmployeesAuthorities(dto);
        verify(userRemoteClient, times(1)).updateEmployeesAuthorities(dto);
    }

    @Test
    void getAllAuthoritiesService() {
        Optional<Employee> employeeOptional = Optional.ofNullable(getEmployee());
        when(employeeRepository.findByEmail(anyString())).thenReturn(employeeOptional);
        when(userRemoteClient.getAllAuthorities(employeeOptional.get().getEmail()))
            .thenReturn(Set.copyOf(ModelUtils.getAllAuthorities()));
        Set<String> authoritiesResult = ubsService.getAllAuthorities(employeeOptional.get().getEmail());
        Set<String> authExpected = Set.of("SEE_CLIENTS_PAGE");
        assertEquals(authExpected, authoritiesResult);

        verify(employeeRepository, times(1)).findByEmail(anyString());
        verify(userRemoteClient, times(1)).getAllAuthorities(any());
    }

    @Test
    void testOrdersForUserWithQuantity() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();

        List<Bag> bags = new ArrayList<>();
        List<Order> orderList = new ArrayList<>();

        bag.setCapacity(120);
        bag.setFullPrice(1200);
        bags.add(bag);
        order.setUser(user);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
                .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
                .thenReturn(orderPaymentStatusTranslation);

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());
        verify(orderStatusTranslationRepository, times(orderList.size()))
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue());
        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
            .getById(
                (long) order.getOrderPaymentStatus().getStatusValue());
        verify(ordersForUserRepository).getAllByUserUuid(pageable, user.getUuid());
    }

    @Test
    void testCreateUserProfileIfProfileDoesNotExist() {
        UserProfileCreateDto userProfileCreateDto = getUserProfileCreateDto();
        User userForSave = User.builder()
            .uuid(userProfileCreateDto.getUuid())
            .recipientEmail(userProfileCreateDto.getEmail())
            .recipientName(userProfileCreateDto.getName())
            .currentPoints(0)
            .violations(0)
            .dateOfRegistration(LocalDate.now()).build();
        User user = getUser();
        when(userRemoteClient.checkIfUserExistsByUuid(userProfileCreateDto.getUuid())).thenReturn(true);
        when(userRepository.findByUuid(userProfileCreateDto.getUuid())).thenReturn(null);
        when(userRepository.save(userForSave)).thenReturn(user);
        Long actualId = ubsService.createUserProfile(userProfileCreateDto);
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
        Long actualId = ubsService.createUserProfile(userProfileCreateDto);
        verify(userRepository, times(1)).findByUuid(userProfileCreateDto.getUuid());
        verify(userRepository, times(0)).save(any(User.class));
        assertEquals(user.getId(), actualId);
    }

    @Test
    void testCreateUserProfileIfUserByUuidDoesNotExist() {
        UserProfileCreateDto userProfileCreateDto = getUserProfileCreateDto();
        when(userRemoteClient.checkIfUserExistsByUuid(userProfileCreateDto.getUuid())).thenReturn(false);
        assertThrows(NotFoundException.class, () -> ubsService.createUserProfile(userProfileCreateDto));
    }
}
