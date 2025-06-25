package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.client.MonoBankClient;
import greencity.client.UserRemoteClient;
import greencity.client.WayForPayClient;
import greencity.constant.ErrorMessage;
import greencity.dto.LocationsDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.RegionDto;
import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffInfoDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.address.AddressDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.order.EventDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.dto.payment.monobank.MonoBankPaymentRequestDto;
import greencity.dto.payment.monobank.MonoBankPaymentResponseDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.entity.viber.ViberBot;
import greencity.enums.AddressStatus;
import greencity.enums.CertificateStatus;
import greencity.enums.CourierLimit;
import greencity.enums.LocationStatus;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.PaymentSystem;
import greencity.enums.TariffStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.mapping.location.LocationToLocationsDtoMapper;
import greencity.repository.AddressRepository;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.CourierRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import greencity.repository.LocationRepository;
import greencity.repository.NotificationParameterRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.AuthorizedUserRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserNotificationRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViberBotRepository;
import greencity.service.google.GoogleApiService;
import greencity.service.notification.NotificationServiceImpl;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;
import greencity.util.OrderUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static greencity.ModelUtils.TEST_BAG_FOR_USER_DTO;
import static greencity.ModelUtils.TEST_EMAIL;
import static greencity.ModelUtils.TEST_PAYMENT_LIST;
import static greencity.ModelUtils.TEST_UUID;
import static greencity.ModelUtils.addressDtoList;
import static greencity.ModelUtils.addressDtoListWithNullPlaceId;
import static greencity.ModelUtils.addressList;
import static greencity.ModelUtils.bagDto;
import static greencity.ModelUtils.botList;
import static greencity.ModelUtils.createCertificateDto;
import static greencity.ModelUtils.getActiveCertificateWith10Points;
import static greencity.ModelUtils.getAddress;
import static greencity.ModelUtils.getBag;
import static greencity.ModelUtils.getBag1list;
import static greencity.ModelUtils.getBagForOrder;
import static greencity.ModelUtils.getBagTranslationDto;
import static greencity.ModelUtils.getCancellationDto;
import static greencity.ModelUtils.getCheckoutResponseFromMonoBank;
import static greencity.ModelUtils.getCourier;
import static greencity.ModelUtils.getCourierDto;
import static greencity.ModelUtils.getCourierDtoList;
import static greencity.ModelUtils.getDtoWithLanguage;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getEvent1;
import static greencity.ModelUtils.getEvent2;
import static greencity.ModelUtils.getGeocodingResultWithKyivRegion;
import static greencity.ModelUtils.getLocation;
import static greencity.ModelUtils.getMonoBankPaymentResponseDto;
import static greencity.ModelUtils.getNotificationPaymentLink;
import static greencity.ModelUtils.getOrder;
import static greencity.ModelUtils.getOrder2;
import static greencity.ModelUtils.getOrderCount;
import static greencity.ModelUtils.getOrderCountWithPaymentStatusPaid;
import static greencity.ModelUtils.getOrderPaymentDetailDto;
import static greencity.ModelUtils.getOrderPaymentStatusTranslation;
import static greencity.ModelUtils.getOrderResponseDto;
import static greencity.ModelUtils.getOrderStatusTranslation;
import static greencity.ModelUtils.getOrderTest;
import static greencity.ModelUtils.getOrderWayForPayClientDto;
import static greencity.ModelUtils.getOrderWithEvents;
import static greencity.ModelUtils.getOrderWithTariffAndLocation;
import static greencity.ModelUtils.getOrderWithoutPayment;
import static greencity.ModelUtils.getPayment;
import static greencity.ModelUtils.getPaymentResponseDto;
import static greencity.ModelUtils.getRegionDto;
import static greencity.ModelUtils.getTariffInfo;
import static greencity.ModelUtils.getTariffInfoDto;
import static greencity.ModelUtils.getTariffInfoWithLimitOfBags;
import static greencity.ModelUtils.getTariffInfoWithLimitOfBagsAndMaxLessThanCountOfBigBag;
import static greencity.ModelUtils.getTariffLocation;
import static greencity.ModelUtils.getTariffsForLocationDto;
import static greencity.ModelUtils.getTariffsInfo;
import static greencity.ModelUtils.getTelegramBotNotifyTrue;
import static greencity.ModelUtils.getTestOrderAddressDtoRequest;
import static greencity.ModelUtils.getTestOrderAddressDtoRequestWithNullPlaceId;
import static greencity.ModelUtils.getTestUser;
import static greencity.ModelUtils.getUBSuser;
import static greencity.ModelUtils.getUBSuserWithoutSender;
import static greencity.ModelUtils.getUbsCustomersDtoUpdate;
import static greencity.ModelUtils.getUbsUsers;
import static greencity.ModelUtils.getUsedCertificateWith600Points;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserNotificationForUnpaidOrder;
import static greencity.ModelUtils.getUserPointsAndAllBagsDto;
import static greencity.ModelUtils.getUserProfileCreateDto;
import static greencity.ModelUtils.getUserProfileUpdateDto;
import static greencity.ModelUtils.getUserProfileUpdateDtoWithBotsIsNotifyFalse;
import static greencity.ModelUtils.getUserWithBotNotifyTrue;
import static greencity.ModelUtils.getUserWithLastLocation;
import static greencity.ModelUtils.getViberBotNotifyTrue;
import static greencity.constant.AppConstant.USER_WITH_PREFIX;
import static greencity.constant.ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.LOCATION_IS_DEACTIVATED_FOR_TARIFF;
import static greencity.constant.ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.PAYMENT_VALIDATION_ERROR;
import static greencity.constant.ErrorMessage.TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND;
import static greencity.constant.ErrorMessage.TARIFF_OR_LOCATION_IS_DEACTIVATED;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class UBSClientServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BagRepository bagRepository;

    @Mock
    private UBSUserRepository ubsUserRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrderAddressRepository orderAddressRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private UBSClientServiceImpl ubsService;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private PaymentRepository paymentRepository;

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
    private AuthorizedUserRepository telegramBotRepository;

    @Mock
    private ViberBotRepository viberBotRepository;

    @InjectMocks
    private UBSClientServiceImpl ubsClientService;

    @Mock
    private OrderBagService orderBagService;
    @Mock
    private OrderBagRepository orderBagRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private WayForPayClient wayForPayClient;

    @Mock
    private LocationToLocationsDtoMapper locationToLocationsDtoMapper;

    @Value("${greencity.wayforpay.secret}")
    private String wayForPaySecret;

    @Mock
    private MonoBankClient monoBankClient;

    @Mock
    private OrderUtils orderUtils;

    @Mock
    private NotificationServiceImpl notificationServiceImpl;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private NotificationParameterRepository notificationParameterRepository;

    @Mock
    private AddressService addressService;

    @Value("${greencity.monobank.token}")
    private String token;

    @Test
    void getFirstPageDataByTariffAndLocationIdShouldThrowExceptionWhenTariffLocationDoesNotExist() {
        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();

        var expectedErrorMessage = TARIFF_FOR_LOCATION_NOT_EXIST + locationId;

        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            tariffsInfoId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);

        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFirstPageDataByTariffAndLocationIdShouldThrowExceptionWhenLocationDoesNotExist() {
        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();

        var expectedErrorMessage = LOCATION_DOESNT_FOUND_BY_ID + locationId;

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            tariffsInfoId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(tariffsInfoRepository).findById(anyLong());
        verify(locationRepository).findById(locationId);

        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFirstPageDataByTariffAndLocationIdShouldThrowExceptionWhenTariffDoesNotExist() {
        var tariffsInfo = getTariffInfo();
        var tariffId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();

        var expectedErrorMessage = TARIFF_NOT_FOUND + tariffId;

        when(tariffsInfoRepository.findById(tariffId)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            tariffId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(tariffsInfoRepository).findById(tariffId);

        verify(locationRepository, never()).findById(anyLong());
        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), anyLong());
    }

    @Test
    void checkIfTariffIsAvailableForCurrentLocationThrowExceptionWhenTariffIsDeactivated() {
        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();
        tariffsInfo.setTariffStatus(TariffStatus.DEACTIVATED);

        var location = getLocation();
        var locationId = location.getId();

        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        var exception = assertThrows(BadRequestException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            tariffsInfoId, locationId));

        assertEquals(TARIFF_OR_LOCATION_IS_DEACTIVATED, exception.getMessage());

        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);

        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void checkIfTariffIsAvailableForCurrentLocationThrowExceptionWhenLocationIsDeactivated() {
        var tariffsInfo = getTariffInfo();
        var tariffsInfoId = tariffsInfo.getId();

        var location = getLocation();
        var locationId = location.getId();
        location.setLocationStatus(LocationStatus.DEACTIVATED);

        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        var exception = assertThrows(BadRequestException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            tariffsInfoId, locationId));

        assertEquals(TARIFF_OR_LOCATION_IS_DEACTIVATED, exception.getMessage());

        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);

        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void checkIfTariffIsAvailableForCurrentLocationWhenLocationForTariffIsDeactivated() {
        var tariffLocation = getTariffLocation();
        tariffLocation.setLocationStatus(LocationStatus.DEACTIVATED);

        var tariffsInfo = tariffLocation.getTariffsInfo();
        var tariffsInfoId = tariffsInfo.getId();
        tariffsInfo.setTariffStatus(TariffStatus.ACTIVE);

        var location = tariffLocation.getLocation();
        var locationId = location.getId();

        var expectedErrorMessage = LOCATION_IS_DEACTIVATED_FOR_TARIFF + tariffsInfoId;

        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));

        var exception = assertThrows(BadRequestException.class, () -> ubsService.getFirstPageDataByTariffAndLocationId(
            tariffsInfoId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);

        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFirstPageDataByTariffAndLocationIdShouldReturnExpectedData() {
        var tariffLocation = getTariffLocation();

        var tariffsInfo = tariffLocation.getTariffsInfo();
        var tariffsInfoId = tariffsInfo.getId();
        tariffsInfo.setTariffStatus(TariffStatus.ACTIVE);

        var location = tariffLocation.getLocation();
        var locationId = location.getId();

        var bags = getBag1list();
        var bagTranslationDto = getBagTranslationDto();
        var userPointsAndAllBagsDtoExpected = getUserPointsAndAllBagsDto();

        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(tariffsInfoId)).thenReturn(bags);
        when(modelMapper.map(bags.getFirst(), BagTranslationDto.class)).thenReturn(bagTranslationDto);

        var userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageDataByTariffAndLocationId(tariffsInfoId, locationId);

        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags(),
            userPointsAndAllBagsDtoActual.getBags());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().getFirst().getId(),
            userPointsAndAllBagsDtoActual.getBags().getFirst().getId());
        assertEquals(
            0,
            userPointsAndAllBagsDtoActual.getPoints());

        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);
        verify(bagRepository).findAllActiveBagsByTariffsInfoId(tariffsInfoId);
        verify(modelMapper).map(bags.getFirst(), BagTranslationDto.class);
    }

    @Test
    void getFirstPageDataByOrderIdShouldReturnExpectedData() {
        var user = getUser();
        var uuid = user.getUuid();

        var order = getOrderWithTariffAndLocation();
        order.setUser(user);
        var orderId = order.getId();

        var tariffsInfo = order.getTariffsInfo();
        var tariffsInfoId = tariffsInfo.getId();
        var location = order
            .getUbsUser()
            .getOrderAddress()
            .getLocation();
        var tariffLocation = getTariffLocation();

        var bags = getBag1list();
        var userPointsAndAllBagsDtoExpected = ModelUtils.getUserPointsAndAllBagsDtoWithQuantity();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(tariffsInfoId)).thenReturn(bags);
        when(orderBagRepository.getAmountOfOrderBagsByOrderIdAndBagId(anyLong(), anyInt()))
            .thenReturn(Optional.of(2));

        var userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageDataByOrderId(uuid, orderId);

        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().getFirst().toString(),
            userPointsAndAllBagsDtoActual.getBags().getFirst().toString());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().getFirst().getQuantity(),
            userPointsAndAllBagsDtoActual.getBags().getFirst().getQuantity());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().getFirst().getId(),
            userPointsAndAllBagsDtoActual.getBags().getFirst().getId());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getPoints(),
            userPointsAndAllBagsDtoActual.getPoints());

        verify(userRepository).findUserByUuid(uuid);
        verify(orderRepository).findById(orderId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);
        verify(bagRepository).findAllActiveBagsByTariffsInfoId(tariffsInfoId);
        verify(orderBagRepository).getAmountOfOrderBagsByOrderIdAndBagId(anyLong(), anyInt());
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
        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
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
        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getFirstPageDataByOrderIdShouldThrowExceptionWhenOrderIsNotOfCurrentUser() {
        var user = getUser();
        user.setId(2L);
        var uuid = user.getUuid();

        var order = getOrderWithoutPayment();
        order.setUser(User.builder().id(1L).build());
        var orderId = order.getId();

        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var exception = assertThrows(AccessDeniedException.class, () -> ubsService.getFirstPageDataByOrderId(
            uuid, orderId));

        assertEquals(ORDER_DOES_NOT_BELONG_TO_USER, exception.getMessage());

        verify(userRepository).findUserByUuid(anyString());
        verify(orderRepository).findById(orderId);

        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testSaveToDB() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);
        user.setOrders(new ArrayList<>());
        user.setChangeOfPointsList(new ArrayList<>());

        Order order = getOrder();
        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();

        UBSuser ubsUser = getUBSuser();
        OrderAddress orderAddress = ubsUser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setAddressId(1L);
        dto.setLocationId(15L);
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("merchantId")) {
                field.setAccessible(true);
                field.set(ubsService, "1");
            }
        }

        tariffsInfo.setBags(Collections.singletonList(bag));
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

        when(locationRepository.findAddressAndLocationNamesMatch(anyLong(), anyLong()))
            .thenReturn(Optional.of("Bearded Lady"));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubsUser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("http://example.com/invoice", result.link());
    }

    @Test
    void testSaveToDBThrowsAddressNotWithinLocationAreaException() throws AddressNotWithinLocationAreaException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setAddressId(1L);
        dto.setLocationId(2L);

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        assertThrows(AddressNotWithinLocationAreaException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));
        verify(addressRepository).findById(anyLong());
        verify(userRepository).findByUuid(anyString());
    }

    @Test
    void testSaveToDBWithNullCoordinatesThrowsException() throws AddressNotWithinLocationAreaException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setAddressId(1L);
        dto.setLocationId(2L);

        Address addressWithNullCoordinates = ModelUtils.getAddress();
        addressWithNullCoordinates.getBaseAddress().setCityEn("Poltava");
        Coordinates coordinates = Coordinates.builder().latitude(0.0).longitude(0.0).build();
        addressWithNullCoordinates.setCoordinates(coordinates);
        when(googleApiService.getGeocodingResultByCityAndCountryAndLocale(anyString(), anyString(), anyString()))
            .thenReturn(getGeocodingResultWithKyivRegion().getFirst());
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(addressWithNullCoordinates));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        assertThrows(AddressNotWithinLocationAreaException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));
        verify(addressRepository).findById(anyLong());
        verify(userRepository).findByUuid(anyString());
    }

    @Test
    void testSaveToDBThrowsEntityNotFoundException() throws EntityNotFoundException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setAddressId(1L);
        dto.setLocationId(1L);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));
        verify(userRepository).findByUuid(anyString());
    }

    @Test
    void testSaveToDB_AddressNotEqualsUsers() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setAddressId(1L);
        dto.setLocationId(1L);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

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
        tariffsInfo.setBags(Collections.singletonList(bag));
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        when(addressRepository.findById(any())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user.setId(null), user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser.setId(null)));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser.setId(null));
        when(addressRepository.findById(any())).thenReturn(Optional.of(getAddress().setUser(getTestUser())));
        when(locationRepository.findById(any())).thenReturn(Optional.of(getLocation()));

        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

    }

    @Test
    void testSaveToDB_AddressStatusDeleted() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setLocationId(1L);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

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
        tariffsInfo.setBags(Collections.singletonList(bag));
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        when(addressRepository.findById(any())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user.setId(null), user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser.setId(null)));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser.setId(null));
        Address address = getAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.DELETED);
        when(addressRepository.findById(any()))
            .thenReturn(Optional.of(address));
        when(locationRepository.findById(any())).thenReturn(Optional.of(getLocation()));
        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

    }

    @Test
    void testSaveToDBWithTwoBags() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.setLocationId(2L);
        dto.getBags().getFirst().setAmount(15);
        dto.setBags(List.of(BagDto.builder().id(1).amount(1).build(), BagDto.builder().id(3).amount(15).build()));
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);

        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag1 = getBagForOrder();
        bag1.setId(1);
        bag1.setCapacity(100);
        bag1.setLimitIncluded(false);
        Bag bag3 = getBagForOrder();
        bag3.setCapacity(1000);
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Arrays.asList(bag1, bag3));
        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);
        order.updateWithNewOrderBags(
            Arrays.asList(ModelUtils.getOrderBag(), ModelUtils.getOrderBag(), ModelUtils.getOrderBag()));
        order1
            .updateWithNewOrderBags(
                Arrays.asList(ModelUtils.getOrderBag(), ModelUtils.getOrderBag(), ModelUtils.getOrderBag()));
        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }
        when(addressRepository.findById(any())).thenReturn(Optional.of(ModelUtils.getAddressTrue()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(1)).thenReturn(Optional.of(bag1));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag3));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        Assertions.assertNotNull(result);

    }

    @Test
    void testSaveToDBWithCertificates() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setCertificates(Set.of("4444-4444"));
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Collections.singletonList(bag));

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

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
            .thenReturn(Optional.of(tariffsInfo));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(certificateRepository.findById(anyString())).thenReturn(Optional.of(getActiveCertificateWith10Points()));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        Assertions.assertNotNull(result);
    }

    @Test
    void testSaveToDBWithDontSendLinkToFondy() throws NoSuchFieldException, IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);
        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setCertificates(Set.of("4444-4444"));
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());
        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Collections.singletonList(bag));
        Certificate certificate = getActiveCertificateWith10Points();
        certificate.setPoints(1000_00);
        UBSuser ubSuser = getUBSuser();
        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Field entityManagerField = UBSClientServiceImpl.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        EntityManager mockEntityManager = mock(EntityManager.class);
        entityManagerField.set(ubsService, mockEntityManager);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(certificateRepository.findById(anyString())).thenReturn(Optional.of(certificate));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(monoBankClient.getCheckoutResponse(any(MonoBankPaymentRequestDto.class), eq(token)))
            .thenReturn(getCheckoutResponseFromMonoBank());
        doNothing().when(mockEntityManager).clear();

        PaymentSystemResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", order.getId());
        Assertions.assertNotNull(result);

        verify(userRepository, times(2)).findByUuid("35467585763t4sfgchjfuyetf");
        verify(orderRepository, times(2)).findById(anyLong());
        verify(mockEntityManager, times(1)).clear();
    }

    @Test
    void testSaveToDBWhenSumToPayLessThanPoints() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(5_000);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(2);
        dto.setPointsToUse(2_000);
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Collections.singletonList(bag));

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

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
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        Assertions.assertNotNull(result);

    }

    @Test
    void testSaveToDbThrowBadRequestExceptionPriceLowerThanLimit() {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(1);
        Bag bag = getBagForOrder();
        Order order = getOrder();
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Collections.singletonList(bag));
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(1)).findActiveBagById(anyInt());
    }

    @Test
    void testSaveToDbThrowBadRequestExceptionPriceGreaterThanLimit() {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(1)).findActiveBagById(anyInt());
    }

    @Test
    void testSaveToDBWShouldThrowBadRequestException() {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBagsAndMaxLessThanCountOfBigBag();
        bag.setTariffsInfo(tariffsInfo);

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
    }

    @Test
    void testSaveToDBWShouldThrowTariffNotFoundExceptionException() {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
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
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

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
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository).findActiveBagById(3);

    }

    @Test
    void testSaveToDBWithoutOrderUnpaid() throws NoSuchFieldException, IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);
        OrderResponseDto dto = getOrderResponseDto(false);
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());
        order.setUser(user);
        Bag bag = getBagForOrder();
        UBSuser ubSuser = getUBSuser();
        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);

        Field entityManagerField = UBSClientServiceImpl.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        EntityManager mockEntityManager = mock(EntityManager.class);
        entityManagerField.set(ubsClientService, mockEntityManager);

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(monoBankClient.getCheckoutResponse(any(MonoBankPaymentRequestDto.class), eq(token)))
            .thenReturn(getCheckoutResponseFromMonoBank());
        doNothing().when(mockEntityManager).clear();

        PaymentSystemResponse result = ubsClientService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", 1L);
        Assertions.assertNotNull(result);

        verify(userRepository, times(2)).findByUuid("35467585763t4sfgchjfuyetf");
        verify(tariffsInfoRepository, times(1)).findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(ubsUserRepository, times(1)).findById(anyLong());
        verify(modelMapper, times(1)).map(dto.getPersonalData(), UBSuser.class);
        verify(orderRepository, times(2)).findById(anyLong()); // Два виклики через isExistOrder і clear
        verify(mockEntityManager, times(1)).clear();
    }

    @Test
    void saveToDBFailPaidOrder() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(1000);
        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(5);
        Order order = getOrder();
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", 1L));
    }

    @Test
    void testSaveToDBThrowsException() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(3);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBags();
        bag.setTariffsInfo(tariffsInfo);

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);

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
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        assertThrows(BadRequestException.class,
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
        List<UBSuser> ubsUser = Collections.singletonList(getUBSuser());
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(ubsUser);
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(expected);
        PersonalDataDto actual = ubsService.getSecondPageData("35467585763t4sfgchjfuyetf");

        assertEquals(expected, actual);
    }

    @Test
    void getSecondPageData_AlternativeEmailIsNull() {
        String uuid = "35467585763t4sfgchjfuyetf";
        PersonalDataDto expected = getOrderResponseDto().getPersonalData();

        User user = getTestUser()
            .setUuid(uuid)
            .setRecipientEmail("mail@mail.ua")
            .setRecipientPhone("067894522");
        List<UBSuser> ubsUser = Collections.singletonList(getUBSuser());
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(ubsUser);
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(expected);

        PersonalDataDto actual = ubsService.getSecondPageData("35467585763t4sfgchjfuyetf");

        assertEquals(expected, actual);
    }

    @Test
    void getSecondPageData_AlternativeEmailIsEmpty() {
        String uuid = "35467585763t4sfgchjfuyetf";
        PersonalDataDto expected = getOrderResponseDto().getPersonalData();

        User user = getTestUser()
            .setUuid(uuid)
            .setRecipientEmail("mail@mail.ua")
            .setRecipientPhone("067894522")
            .setAlternateEmail("");
        List<UBSuser> ubsUser = Collections.singletonList(getUBSuser());
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(ubsUser);
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(expected);

        PersonalDataDto actual = ubsService.getSecondPageData("35467585763t4sfgchjfuyetf");

        assertEquals(expected, actual);
    }

    @Test
    void getSecondPageData_ubsUser_isEmpty() {
        String uuid = "35467585763t4sfgchjfuyetf";
        PersonalDataDto expected = getOrderResponseDto().getPersonalData();

        User user = getTestUser()
            .setUuid(uuid)
            .setRecipientEmail("mail@mail.ua")
            .setRecipientPhone("067894522")
            .setAlternateEmail("my@email.com");
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(Collections.emptyList());
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
        Certificate certificate = getActiveCertificateWith10Points();
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(certificate));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(CertificateDto.builder()
            .code("1111-1234")
            .certificateStatus("ACTIVE")
            .creationDate(LocalDate.now())
            .dateOfUse(LocalDate.now().plusMonths(1))
            .points(10)
            .build());

        assertEquals("ACTIVE", ubsService.checkCertificate("1111-1234", TEST_UUID).getCertificateStatus());
    }

    @Test
    void checkCertificateUSEDWithForeignUUID() {
        Certificate certificate = getUsedCertificateWith600Points();
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(certificate));
        assertEquals("USED", ubsService.checkCertificate("1111-1234", TEST_UUID).getCertificateStatus());
    }

    @Test
    void checkCertificateUSEDWithCorrectUUID() {
        Certificate certificate = getUsedCertificateWith600Points();
        certificate.getOrder().getUser().setUuid(TEST_UUID);
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(certificate));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(CertificateDto.builder()
            .code("1111-1234")
            .certificateStatus("ACTIVE")
            .creationDate(LocalDate.now())
            .dateOfUse(null)
            .points(10)
            .build());
        assertNull(ubsService.checkCertificate("1111-1234", TEST_UUID).getDateOfUse());
    }

    @Test
    void checkCertificateWithNoAvailable() {
        assertThrows(NotFoundException.class, () -> ubsService.checkCertificate("randomstring", TEST_UUID));
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
        DeactivateUserRequestDto request = DeactivateUserRequestDto.builder()
            .reason("test")
            .build();
        Exception thrown = assertThrows(NotFoundException.class,
            () -> ubsService.markUserAsDeactivated("test", request));
        assertEquals(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST, thrown.getMessage());
    }

    @Test
    void markUserAsDeactivatedById() {
        User user = getUser();
        DeactivateUserRequestDto request = DeactivateUserRequestDto.builder()
            .reason("test")
            .build();
        when(userRepository.findByUuid("test")).thenReturn(user);
        ubsService.markUserAsDeactivated("test", request);
        verify(userRepository).findByUuid("test");
        verify(userRemoteClient).markUserDeactivated(user.getUuid(), request);
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
        when(userRepository.countTotalUsersViolations(1L)).thenReturn(expectedResult.getTotalUserViolations());
        when(userRepository.checkIfUserHasViolationForCurrentOrder(1L, 1L))
            .thenReturn(expectedResult.getUserViolationForCurrentOrder());
        UserInfoDto actual = ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, user.getUuid());

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
        when(userRepository.countTotalUsersViolations(1L)).thenReturn(expectedResult.getTotalUserViolations());
        when(userRepository.checkIfUserHasViolationForCurrentOrder(1L, 1L))
            .thenReturn(expectedResult.getUserViolationForCurrentOrder());
        UserInfoDto actual = ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, user.getUuid());

        verify(ubsUserRepository, times(1)).findUbsUserByOrderId(1L);
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
    void getUserAndUserUbsAndViolationsInfoByOrderIdThrowsAnAccessDeniedExceptionForNonEqualUserUuidTest() {
        UBSuser ubSuser = getUBSuser();
        ubSuser.setUser(getUser());
        when(ubsUserRepository.findUbsUserByOrderId(1L)).thenReturn(Optional.of(ubSuser));

        assertThrows(AccessDeniedException.class,
            () -> ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, "abc"));
        verify(ubsUserRepository, times(1)).findUbsUserByOrderId(1L);
    }

    @Test
    void updateUbsUserInfoInOrderThrowUBSuserNotFoundExceptionTest() {
        UbsCustomersDtoUpdate request = getUbsCustomersDtoUpdate();

        when(ubsUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UBSuserNotFoundException.class,
            () -> ubsService.updateUbsUserInfoInOrder(request, "abc"));
        verify(ubsUserRepository).findById(1L);
    }

    @Test
    void updateUbsUserInfoInOrderTest() {
        UbsCustomersDtoUpdate request = getUbsCustomer();

        Optional<UBSuser> ubsUserOptional = Optional.of(getUBSuser());
        UBSuser ubsUser = ubsUserOptional.get();
        User user = getUser();
        ubsUser.setUser(user);

        MockedStatic<SecurityContextHolder> mockedContextHolder = mockStatic(SecurityContextHolder.class);
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

        UbsCustomersDto actual = ubsService.updateUbsUserInfoInOrder(request, user.getUuid());
        assertEquals(expected, actual);

        verify(ubsUserRepository).findById(1L);
        verify(ubsUserRepository).save(ubsUserOptional.get());
        verify(eventService).save(anyString(), anyString(), any());

        mockedContextHolder.verify(SecurityContextHolder::getContext);
        verify(securityContext).getAuthentication();
        verify(authentication, times(2)).getAuthorities();

        mockedContextHolder.close();
    }

    @Test
    void updateUbsUserInfoInOrderWithWrongAccessThrowsExceptionTest() {
        UbsCustomersDtoUpdate request = getUbsCustomer();

        Optional<UBSuser> ubsUserOptional = Optional.of(getUBSuser());
        UBSuser ubsUser = ubsUserOptional.get();
        User user = getUser();
        ubsUser.setUser(user);

        MockedStatic<SecurityContextHolder> mockedContextHolder = mockStatic(SecurityContextHolder.class);
        mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities())
            .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority(USER_WITH_PREFIX)));

        when(ubsUserRepository.findById(1L)).thenReturn(ubsUserOptional);

        assertThrows(AccessDeniedException.class,
            () -> ubsService.updateUbsUserInfoInOrder(request, user.getUuid() + "test"));

        verify(ubsUserRepository).findById(1L);

        mockedContextHolder.verify(SecurityContextHolder::getContext);
        verify(securityContext).getAuthentication();
        verify(authentication).getAuthorities();

        mockedContextHolder.close();
    }

    private static UbsCustomersDtoUpdate getUbsCustomer() {
        return UbsCustomersDtoUpdate.builder()
            .customerId(1L)
            .customerName("Anatolii")
            .customerSurname("Anatolii")
            .customerEmail("anatolii.andr@gmail.com")
            .customerPhoneNumber("095123456").build();
    }

    @Test
    void updateProfileData() {
        UBSClientServiceImpl ubsClientServiceSpy = spy(ubsService);

        User user = getUserWithBotNotifyTrue();
        AuthorizedUser telegramBot = getTelegramBotNotifyTrue();
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
            .when(addressService).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        ubsClientServiceSpy.updateProfileData(uuid, userProfileUpdateDto);

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
            () -> ubsService.updateProfileData(uuid, userProfileUpdateDto));
        verify(userRepository).findUserByUuid(uuid);

    }

    @Test
    void updateProfileDataWhenAddressPlaceIdIsNull() {
        UBSClientServiceImpl ubsClientServiceSpy = spy(ubsService);

        User user = getUserWithBotNotifyTrue();
        AuthorizedUser telegramBot = getTelegramBotNotifyTrue();
        ViberBot viberBot = getViberBotNotifyTrue();
        List<AddressDto> addressDto = addressDtoListWithNullPlaceId();

        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDto();
        userProfileUpdateDto.getAddressDto().get(0).setPlaceId(null);
        userProfileUpdateDto.getAddressDto().get(1).setPlaceId(null);

        String uuid = UUID.randomUUID().toString();
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequestWithNullPlaceId();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(telegramBotRepository.findByUser(user)).thenReturn(Optional.of(telegramBot));
        when(viberBotRepository.findByUser(user)).thenReturn(Optional.of(viberBot));
        when(modelMapper.map(addressDto.get(0), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        when(modelMapper.map(addressDto.get(1), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        doReturn(new OrderWithAddressesResponseDto()).when(addressService)
            .updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        ubsClientServiceSpy.updateProfileData(uuid, userProfileUpdateDto);

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramBotRepository).findByUser(user);
        verify(viberBotRepository).findByUser(user);
        verify(modelMapper).map(addressDto.get(0), OrderAddressDtoRequest.class);
        verify(modelMapper).map(addressDto.get(1), OrderAddressDtoRequest.class);
        verify(addressService, times(2)).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        verify(userRepository).save(user);
        verify(modelMapper).map(user, UserProfileUpdateDto.class);
    }

    @Test
    void updateProfileDataIfTelegramBotNotExists() {
        UBSClientServiceImpl ubsClientServiceSpy = spy(ubsService);

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
            .when(addressService).updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        ubsClientServiceSpy.updateProfileData(uuid, userProfileUpdateDto);

        assertFalse(userProfileUpdateDto.getTelegramIsNotify());

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramBotRepository).findByUser(user);
        verify(viberBotRepository).findByUser(user);
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
    void getOrderPaymentDetail() {
        Order order = getOrder();
        Certificate certificate = getActiveCertificateWith10Points();
        certificate.setOrder(order);
        order.setCertificates(Set.of(certificate));
        order.setPayment(List.of(getPayment()));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderPaymentDetailDto actual = ubsService.getOrderPaymentDetail(1L);

        assertEquals(getOrderPaymentDetailDto(), actual);
    }

    @Test
    void getOrderPaymentDetailIfCertificateAndPointsAbsent() {
        Order order = getOrder();
        order.setPayment(List.of(getPayment()));
        order.setPointsToUse(0);
        OrderPaymentDetailDto expected = getOrderPaymentDetailDto();
        expected.setPointsToUse(0);
        expected.setCertificates(0);
        expected.setAmountToPay(95000L);
        expected.setAmount(95000L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderPaymentDetailDto actual = ubsService.getOrderPaymentDetail(1L);

        assertEquals(expected, actual);
    }

    @Test
    void getOrderPaymentDetailIfPaymentIsEmpty() {
        Order order = getOrder();
        order.setPayment(List.of());
        Certificate certificate = getActiveCertificateWith10Points();
        certificate.setOrder(order);
        order.setCertificates(Set.of(certificate));
        order.setPayment(List.of(getPayment()));
        OrderPaymentDetailDto expected = getOrderPaymentDetailDto();
        expected.setAmount(0L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderPaymentDetailDto actual = ubsService.getOrderPaymentDetail(1L);

        assertEquals(getOrderPaymentDetailDto(), actual);
    }

    @Test
    void getOrderPaymentDetailShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        Exception thrown = assertThrows(NotFoundException.class,
                () -> ubsService.getOrderPaymentDetail(null));
        assertEquals(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
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
    void testGelAllEventsFromOrderByOrderIdWithUA() {
        Long orderId = 1L;
        String language = "ua";
        Event event1 = getEvent1();
        Event event2 = getEvent2();
        EventDto eventDto1 = getDtoWithLanguage(language, event1);
        EventDto eventDto2 = getDtoWithLanguage(language, event2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(new Order()));
        when(eventRepository.findAllEventsByOrderId(anyLong())).thenReturn(List.of(event1, event2));
        when(modelMapper.map(event1, EventDto.class)).thenReturn(eventDto1);
        when(modelMapper.map(event2, EventDto.class)).thenReturn(eventDto2);

        List<EventDto> result = ubsService.getAllEventsForOrder(orderId, anyString(), "ua");

        assertEquals(2, result.size());
        assertEquals(eventDto2, result.get(0));
        assertEquals(eventDto1, result.get(1));

        verify(orderRepository).findById(orderId);
        verify(eventRepository).findAllEventsByOrderId(orderId);
        verify(modelMapper).map(event1, EventDto.class);
        verify(modelMapper).map(event2, EventDto.class);
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithEng() {
        Long orderId = 1L;
        String language = "en";
        Event event1 = getEvent1();
        Event event2 = getEvent2();
        EventDto eventDto1 = getDtoWithLanguage(language, event1);
        EventDto eventDto2 = getDtoWithLanguage(language, event2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(new Order()));
        when(eventRepository.findAllEventsByOrderId(anyLong())).thenReturn(List.of(event1, event2));
        when(modelMapper.map(event1, EventDto.class)).thenReturn(eventDto1);
        when(modelMapper.map(event2, EventDto.class)).thenReturn(eventDto2);

        List<EventDto> result = ubsService.getAllEventsForOrder(orderId, anyString(), "en");

        assertEquals(2, result.size());
        assertEquals(eventDto2, result.get(0));
        assertEquals(eventDto1, result.get(1));

        verify(orderRepository).findById(orderId);
        verify(eventRepository).findAllEventsByOrderId(orderId);
        verify(modelMapper).map(event1, EventDto.class);
        verify(modelMapper).map(event2, EventDto.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bg", "it", "ru"})
    void testGelAllEventsFromOrderByOrderIdWithOtherLanguage(String language) {
        Long orderId = 1L;
        String email = "test@gmail.com";
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(new Order()));
        when(eventRepository.findAllEventsByOrderId(anyLong())).thenReturn(List.of(new Event()));

        assertThrows(BadRequestException.class,
            () -> ubsService.getAllEventsForOrder(orderId, email, language));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithThrowingOrderNotFindException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> ubsService.getAllEventsForOrder(1L, "abc", "en"));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithThrowingEventsNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(Collections.emptyList());
        assertThrows(NotFoundException.class,
                () -> ubsService.getAllEventsForOrder(1L, "abc", "en"));
    }

    @Test
    void deleteOrder() {
        Order order = getOrder();
        when(ordersForUserRepository.getAllByUserUuidAndId(order.getUser().getUuid(), order.getId()))
            .thenReturn(order);

        ubsService.deleteOrder(order.getUser().getUuid(), 1L);

        verify(orderRepository).save(order);
        verify(ordersForUserRepository).getAllByUserUuidAndId(order.getUser().getUuid(), order.getId());
    }

    @Test
    void deleteOrderFail() {
        Order order = getOrder();
        when(ordersForUserRepository.getAllByUserUuidAndId(order.getUser().getUuid(), order.getId()))
            .thenReturn(null);

        assertThrows(NotFoundException.class, () -> ubsService.deleteOrder("UUID", 1L));
    }

    @Test
    void saveFullOrderToDBForIF() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setCertificates(Set.of(getActiveCertificateWith10Points().getCode()));
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBag();
        bag.setCapacity(100);
        bag.setFullPrice(400_00L);
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        order1.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(getActiveCertificateWith10Points()));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        Assertions.assertNotNull(result);
    }

    @Test
    void saveFullOrderToDBWhenSumToPayeqNull() throws IllegalAccessException, NoSuchFieldException {
        // Ініціалізація об’єктів
        User user = getUserWithLastLocation();
        user.setCurrentPoints(9000);
        user.setUbsUsers(getUbsUsers());

        OrderResponseDto dto = getOrderResponseDto();
        dto.setAddressId(1L);
        dto.setPointsToUse(6000);
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        order.setPayment(null);
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag(), ModelUtils.getOrderBag()));
        Bag bag = getBagForOrder();
        UBSuser ubSuser = getUBSuser().setId(null);
        Address address = getAddress();
        address.setUser(user);
        var location = getLocation();
        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setLocation(location);
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        // Налаштування entityManager через рефлексію
        Field entityManagerField = UBSClientServiceImpl.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        EntityManager mockEntityManager = mock(EntityManager.class);
        entityManagerField.set(ubsClientService, mockEntityManager);

        // Налаштування моків
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffsInfo()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(addressRepository.findById(any())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(monoBankClient.getCheckoutResponse(any(MonoBankPaymentRequestDto.class), eq(token)))
            .thenReturn(getCheckoutResponseFromMonoBank());
        doNothing().when(mockEntityManager).clear();

        // Виклик методу
        PaymentSystemResponse result =
            ubsClientService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", order.getId());
        Assertions.assertNotNull(result);

        // Перевірки
        verify(userRepository, times(2)).findByUuid("35467585763t4sfgchjfuyetf");
        verify(orderRepository, times(2)).findById(anyLong());
        verify(mockEntityManager, times(1)).clear();
    }

    @Test
    void testSaveToDBfromIForIFThrowsException() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(3);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        bag.setTariffsInfo(getTariffInfoWithLimitOfBags());

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);

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
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfoWithLimitOfBags()));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));
    }

    @Test
    void testCheckSumIfCourierLimitBySumOfOrderForIF1() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(9000);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);

        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBags();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMin(50000L);
        bag.setTariffsInfo(tariffsInfo);

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));

        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

    }

    @Test
    void testCheckSumIfCourierLimitBySumOfOrderForIF2() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(9000);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);

        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBags();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMax(500L);
        bag.setTariffsInfo(tariffsInfo);

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));
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
        user.getChangeOfPointsList().getFirst().setAmount(100);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));

        AllPointsUserDto pointsDTO = ubsService.findAllCurrentPointsForUser(user.getUuid());

        assertEquals(user.getCurrentPoints(), pointsDTO.getUserBonuses());

        user.setCurrentPoints(null);
        user.setChangeOfPointsList(null);

        pointsDTO = ubsService.findAllCurrentPointsForUser(user.getUuid());

        assertEquals(0, pointsDTO.getUserBonuses());
    }

    @Test
    void getOrderForUserTest() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();
        List<Order> orderList = new ArrayList<>();

        bag.setCapacity(120);
        bag.setFullPrice(1200_00L);
        order.setAmountOfBagsOrdered(Map.of(1, 10));
        order.setUser(user);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(ordersForUserRepository.getAllByUserUuidAndId(user.getUuid(), order.getId()))
            .thenReturn(order);
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
            .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
            .thenReturn(orderPaymentStatusTranslation);

        ubsService.getOrderForUser(user.getUuid(), 1L);

        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
        verify(orderStatusTranslationRepository, times(orderList.size()))
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue());
        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
            .getById(
                (long) order.getOrderPaymentStatus().getStatusValue());
    }

    @Test
    void getOrderForUserFail() {
        Order order = getOrderTest();

        when(ordersForUserRepository.getAllByUserUuidAndId("UUID", order.getId()))
            .thenReturn(null);

        assertThrows(NotFoundException.class, () -> ubsService.getOrderForUser("UUID", 1L));
    }

    @Test
    void getOrdersForUserTest() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();
        List<Order> orderList = new ArrayList<>();

        bag.setCapacity(120);
        bag.setFullPrice(1200_00L);
        order.setAmountOfBagsOrdered(Map.of(1, 10));
        order.setUser(user);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
            .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
            .thenReturn(orderPaymentStatusTranslation);
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().getFirst().getId(), order.getId());

        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
        verify(orderStatusTranslationRepository, times(orderList.size()))
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue());
        verify(orderPaymentStatusTranslationRepository, times(orderList.size()))
            .getById((long) order.getOrderPaymentStatus().getStatusValue());
        verify(ordersForUserRepository).getAllByUserUuid(pageable, user.getUuid());
    }

    @Test
    void testOrdersForUserWithExportedQuantity() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        Order order = getOrderTest();
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        User user = getTestUser();
        Bag bag = bagDto();
        List<Order> orderList = new ArrayList<>();

        bag.setCapacity(120);
        bag.setFullPrice(1200_00L);
        order.setExportedQuantity(Map.of(1, 10));
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
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().getFirst().getId(), order.getId());

        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
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
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();
        List<Order> orderList = new ArrayList<>();

        bag.setCapacity(120);
        bag.setFullPrice(1200_00L);
        order.setConfirmedQuantity(Map.of(1, 10));
        order.setUser(user);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
            .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
            .thenReturn(orderPaymentStatusTranslation);
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().getFirst().getId(), order.getId());

        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
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
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        TariffsInfo tariffsInfo = getTariffsInfo();
        tariffsInfo.setBags(Collections.singletonList(getBag()));
        order.setTariffsInfo(tariffsInfo);
        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
            .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
            .thenReturn(orderPaymentStatusTranslation);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);
        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().getFirst().getId(), order.getId());
    }

    @Test
    void getTariffInfoForLocationTest() {
        var tariff = getTariffInfo();
        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(locationRepository.existsById(1L)).thenReturn(true);
        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariff));
        TariffInfoByLocationDto dto = ubsService.getTariffInfoForLocation(1L, 1L);
        Assertions.assertTrue(dto.getOrderIsPresent());
        verify(courierRepository).existsCourierById(1L);
        verify(modelMapper).map(tariff, TariffsForLocationDto.class);
        verify(tariffsInfoRepository).findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong());
    }

    @Test
    void getTariffInfoForLocationWhenCourierNotFoundTest() {
        when(courierRepository.existsCourierById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> ubsService
                .getTariffInfoForLocation(1L, 1L));
        verify(courierRepository).existsCourierById(1L);
    }

    @Test
    void getTariffInfoForLocationWhenTariffForCourierAndLocationNotFoundTest() {
        var expectedErrorMessage = String.format(TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST, 1L, 1L);
        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(locationRepository.existsById(1L)).thenReturn(true);
        var exception = assertThrows(NotFoundException.class,
            () -> ubsService.getTariffInfoForLocation(1L, 1L));

        assertEquals(expectedErrorMessage, exception.getMessage());
        verify(courierRepository).existsCourierById(1L);
    }

    @Test
    void getInfoForCourierOrderingByCourierIdTest() {
        TariffsInfo tariff = getTariffInfo();
        Location location = getLocation();
        RegionDto regionDto = getRegionDto();

        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(orderRepository.getLastOrderOfUserByUUIDIfExists(anyString()))
            .thenReturn(Optional.of(getOrder()));
        when(locationRepository.findAllActiveLocationsByCourierId(1L)).thenReturn(List.of(location));
        when(modelMapper.map(location, RegionDto.class)).thenReturn(regionDto);
        when(tariffsInfoRepository.findTariffInfoByLocationIdAndCourierId(location.getId(), 1L))
            .thenReturn(Optional.of(tariff));
        when(modelMapper.map(tariff, TariffInfoDto.class)).thenReturn(getTariffInfoDto());

        OrderCourierPopUpDto dto = ubsService.getInfoForCourierOrderingByCourierId("35467585763t4sfgchjfuyetf",
            Optional.empty(), 1L);
        Assertions.assertTrue(dto.getOrderIsPresent());

        verify(courierRepository).existsCourierById(1L);
        verify(orderRepository).getLastOrderOfUserByUUIDIfExists(anyString());
        verify(locationRepository).findAllActiveLocationsByCourierId(1L);
        verify(modelMapper).map(location, RegionDto.class);
        verify(tariffsInfoRepository).findTariffInfoByLocationIdAndCourierId(location.getId(), 1L);
        verify(modelMapper).map(tariff, TariffInfoDto.class);
    }

    @Test
    void getInfoForCourierOrderingByCourierIdWhenCourierNotFoundTest() {
        Optional<String> changeLoc = Optional.empty();
        when(courierRepository.existsCourierById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> ubsService
            .getInfoForCourierOrderingByCourierId("35467585763t4sfgchjfuyetf", changeLoc, 1L));
        verify(courierRepository).existsCourierById(1L);
    }

    @Test
    void getInfoForCourierOrderingByCourierIdWhenOrderIsEmptyTest() {
        TariffsInfo tariff = getTariffInfo();
        Location location = getLocation();
        RegionDto regionDto = getRegionDto();
        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(orderRepository.getLastOrderOfUserByUUIDIfExists(anyString()))
            .thenReturn(Optional.empty());
        when(locationRepository.findAllActiveLocationsByCourierId(1L)).thenReturn(List.of(location));
        when(modelMapper.map(location, RegionDto.class)).thenReturn(regionDto);
        when(tariffsInfoRepository.findTariffInfoByLocationIdAndCourierId(location.getId(), 1L))
            .thenReturn(Optional.of(tariff));
        when(modelMapper.map(tariff, TariffInfoDto.class)).thenReturn(getTariffInfoDto());
        OrderCourierPopUpDto dto = ubsService.getInfoForCourierOrderingByCourierId("35467585763t4sfgchjfuyetf",
            Optional.empty(), 1L);
        assertFalse(dto.getOrderIsPresent());

        verify(courierRepository).existsCourierById(1L);
        verify(orderRepository).getLastOrderOfUserByUUIDIfExists(anyString());
        verify(locationRepository).findAllActiveLocationsByCourierId(1L);
        verify(modelMapper).map(location, RegionDto.class);
        verify(tariffsInfoRepository).findTariffInfoByLocationIdAndCourierId(location.getId(), 1L);
        verify(modelMapper).map(tariff, TariffInfoDto.class);
    }

    @Test
    void getInfoForCourierOrderingByCourierIdWhenChangeLocIsPresentTest() {
        TariffsInfo tariff = getTariffInfo();
        Location location = getLocation();
        RegionDto regionDto = getRegionDto();
        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(locationRepository.findAllActiveLocationsByCourierId(1L)).thenReturn(List.of(location));
        when(modelMapper.map(location, RegionDto.class)).thenReturn(regionDto);
        when(tariffsInfoRepository.findTariffInfoByLocationIdAndCourierId(location.getId(), 1L))
            .thenReturn(Optional.of(tariff));
        when(modelMapper.map(tariff, TariffInfoDto.class)).thenReturn(getTariffInfoDto());
        var dto = ubsService.getInfoForCourierOrderingByCourierId(
            "35467585763t4sfgchjfuyetf", Optional.of("w"), 1L);
        assertEquals(1, dto.getAllActiveLocationsDtos().size());
        assertFalse(dto.getOrderIsPresent());
        verify(courierRepository).existsCourierById(1L);
        verify(locationRepository).findAllActiveLocationsByCourierId(1L);
        verify(modelMapper).map(location, RegionDto.class);
        verify(tariffsInfoRepository).findTariffInfoByLocationIdAndCourierId(location.getId(), 1L);
        verify(modelMapper).map(tariff, TariffInfoDto.class);
    }

    @Test
    void getAllActiveCouriersTest() {
        when(courierRepository.getAllActiveCouriers()).thenReturn(List.of(getCourier()));
        when(modelMapper.map(getCourier(), CourierDto.class))
                .thenReturn(getCourierDto());

        assertEquals(getCourierDtoList(), ubsService.getAllActiveCouriers());

        verify(courierRepository).getAllActiveCouriers();
        verify(modelMapper).map(getCourier(), CourierDto.class);
    }

    @Test
    void checkIfAddressHasBeenDeletedTest() throws IllegalAccessException {

        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        Bag bag = getBagForOrder();

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.DELETED);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);

        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

    }

    @Test
    void checkAddressUserTest() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();

        UBSuser ubSuser = getUBSuser();

        OrderAddress address = ubSuser.getOrderAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);

        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

    }

    @Test
    void checkIfUserHaveEnoughPointsTest() throws IllegalAccessException {

        User user = getUserWithLastLocation();
        user.setCurrentPoints(100);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));
    }

    @Test
    void getTariffForOrderTest() {
        TariffsInfo tariffsInfo = getTariffInfo();
        when(tariffsInfoRepository.findByOrdersId(1L)).thenReturn(Optional.of(tariffsInfo));
        when(modelMapper.map(tariffsInfo, TariffsForLocationDto.class))
            .thenReturn(getTariffsForLocationDto());
        ubsService.getTariffForOrder(1L);
        verify(tariffsInfoRepository).findByOrdersId(1L);
        verify(modelMapper).map(tariffsInfo, TariffsForLocationDto.class);
    }

    @Test
    void getTariffForOrderFailTest() {
        when(tariffsInfoRepository.findByOrdersId(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsService.getTariffForOrder(1L));
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
        Employee employee = employeeOptional.orElseThrow(() -> new IllegalStateException("Employee not found"));
        when(userRemoteClient.getAllAuthorities(employee.getEmail()))
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
        List<Order> orderList = new ArrayList<>();

        bag.setCapacity(120);
        bag.setFullPrice(1200_00L);
        order.setUser(user);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);
        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
            .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
            .thenReturn(orderPaymentStatusTranslation);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().getFirst().getId(), order.getId());
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

    @Test
    void getPositionsAndRelatedAuthoritiesTest() {
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.ofNullable(getEmployee()));
        when(userRemoteClient.getPositionsAndRelatedAuthorities(TEST_EMAIL))
                .thenReturn(ModelUtils.getPositionAuthoritiesDto());

        PositionAuthoritiesDto actual = ubsService.getPositionsAndRelatedAuthorities(TEST_EMAIL);
        assertEquals(ModelUtils.getPositionAuthoritiesDto(), actual);

        verify(employeeRepository).findByEmail(TEST_EMAIL);
        verify(userRemoteClient).getPositionsAndRelatedAuthorities(TEST_EMAIL);
    }

    @Test
    void getPositionsAndRelatedAuthoritiesThrowsNotFoundExceptionTest() {
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsService.getPositionsAndRelatedAuthorities(TEST_EMAIL));
        verify(employeeRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void shouldExtractOrderIdFromDataSuccessfully() {
        String data = Base64.getEncoder().encodeToString("{\"order_id\":\"123\"}".getBytes());
        Long result = ubsClientService.extractOrderIdFromData(data);
        assertEquals(123L, result);
    }

    @Test
    void shouldExtractStatusFromDataSuccessfully() {
        String data = Base64.getEncoder().encodeToString("{\"status\":\"approved\"}".getBytes());
        String result = ubsClientService.extractStatusFromData(data);
        assertEquals("approved", result);
    }

    @Test
    void testValidatePaymentSuccess() {
        PaymentResponseDto response = getPaymentResponseDto();

        Order expectedOrder = getOrder2();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(expectedOrder));
        when(encryptionUtil.formResponseSignature(any(PaymentResponseWayForPay.class), eq(wayForPaySecret)))
            .thenReturn("signature");
        when(userNotificationRepository.findAllUserNotificationByOrderAndNotificationType(any(Order.class),
            any(NotificationType.class)))
            .thenReturn(List.of(getUserNotificationForUnpaidOrder()));
        when(notificationParameterRepository
            .findNotificationParameterByUserNotificationAndKey(any(UserNotification.class), anyString()))
            .thenReturn(getNotificationPaymentLink());

        PaymentResponseWayForPay result = ubsClientService.validatePayment(response);

        assertNotNull(result);
        assertEquals("accept", result.getStatus());
        assertEquals(response.getOrderReference(), result.getOrderReference());
        assertEquals("signature", result.getSignature());

        verify(orderRepository).findById(1L);
        verify(encryptionUtil).formResponseSignature(any(PaymentResponseWayForPay.class), eq(wayForPaySecret));
        verify(userNotificationRepository)
            .findAllUserNotificationByOrderAndNotificationType(any(Order.class), any(NotificationType.class));
        verify(notificationParameterRepository)
            .findNotificationParameterByUserNotificationAndKey(any(UserNotification.class), anyString());
    }

    @Test
    void testValidatePaymentOrderNotFound() {
        PaymentResponseDto response = getPaymentResponseDto();

        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> ubsClientService.validatePayment(response));

        assertEquals(PAYMENT_VALIDATION_ERROR, exception.getMessage());
        verify(orderRepository).findById(1L);
        verifyNoInteractions(encryptionUtil);
    }

    @Test
    void testMapPayment() {
        PaymentResponseDto response = PaymentResponseDto.builder()
            .orderReference("MV8xXzE=")
            .currency("USD")
            .amount("150")
            .transactionStatus("Approved")
            .phone("+1234567890")
            .cardPan("**** **** **** 1234")
            .cardType("Visa")
            .createdDate("2024-07-23T12:00:00")
            .paymentSystem("Visa")
            .email("testuser@example.com")
            .build();

        Payment result = invokeMapPayment(response);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("USD", result.getCurrency());
        assertEquals(15000L, result.getAmount());
        assertEquals(OrderStatus.FORMED, result.getOrderStatus());
        assertEquals("+1234567890", result.getSenderCellPhone());
        assertEquals("**** **** **** 1234", result.getMaskedCard());
        assertEquals("Visa", result.getCardType());
        assertEquals("2024-07-23T12:00:00", result.getOrderTime());
        assertEquals(LocalDate.now().toString(), result.getSettlementDate());
        assertEquals("Visa", result.getPaymentSystem());
        assertEquals("testuser@example.com", result.getSenderEmail());
        assertEquals(PaymentStatus.UNPAID, result.getPaymentStatus());
    }

    private Payment invokeMapPayment(PaymentResponseDto response) {
        try {
            var method = UBSClientServiceImpl.class
                .getDeclaredMethod("mapPayment", PaymentResponseDto.class, String.class);
            method.setAccessible(true);
            return (Payment) method.invoke(ubsClientService, response, "1_1_1");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testParseSettlementDateEmpty() {
        String settlementDate = "";

        String result = invokeParseSettlementDate(settlementDate);

        assertEquals(LocalDate.now().toString(), result);
    }

    @Test
    void testParseSettlementDateValidDate() {
        String settlementDate = "23.07.2024";

        String result = invokeParseSettlementDate(settlementDate);

        assertEquals("2024-07-23", result);
    }

    private String invokeParseSettlementDate(String settlementDate) {
        try {
            var method = UBSClientServiceImpl.class.getDeclaredMethod("parseSettlementDate", String.class);
            method.setAccessible(true);
            return (String) method.invoke(ubsClientService, settlementDate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void processOrderWFPClient2() throws Exception {
        Order order = getOrderCount();
        Certificate certificate = ModelUtils.getCertificate();

        HashMap<Integer, Integer> value = new HashMap<>();
        value.put(1, 22);
        order.setAmountOfBagsOrdered(value);
        order.setPointsToUse(100);
        order.setSumTotalAmountWithoutDiscounts(1000_00L);
        order.setCertificates(Set.of(certificate));
        order.setPayment(TEST_PAYMENT_LIST);
        User user = getUser();
        user.setCurrentPoints(100);
        user.setChangeOfPointsList(new ArrayList<>());
        order.setUser(user);

        OrderWayForPayClientDto dto = getOrderWayForPayClientDto();
        dto.setCertificates(Set.of("1111-1234"));

        order.setCertificates(Set.of(ModelUtils.getCertificate()));
        order.setPayment(TEST_PAYMENT_LIST);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        order.setPointsToUse(-10000);
        CertificateDto certificateDto = createCertificateDto();
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(user));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(certificateDto);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(certificateRepository.findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE)).thenReturn(Set.of(certificate));
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        when(wayForPayClient.getCheckOutResponse(any())).thenReturn("{\"invoiceUrl\":\"link\"}");

        ubsService.processOrder("uuid", dto);

        verify(userRepository).findUserByUuid("uuid");
        verify(certificateRepository).findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE);
        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
    }

    @Test
    void processOrderWFPClientCertificeteNotFoundExeption() throws Exception {
        Order order = getOrderCount();
        Certificate certificate = ModelUtils.getCertificate();
        certificate.setPoints(1500);
        HashMap<Integer, Integer> value = new HashMap<>();
        value.put(1, 22);
        order.setAmountOfBagsOrdered(value);
        order.setPointsToUse(100);
        order.setSumTotalAmountWithoutDiscounts(1000_00L);
        order.setCertificates(Set.of(certificate));
        order.setPayment(TEST_PAYMENT_LIST);
        User user = getUser();
        user.setCurrentPoints(100);
        user.setChangeOfPointsList(new ArrayList<>());
        order.setUser(user);
        OrderWayForPayClientDto dto = getOrderWayForPayClientDto();
        dto.setCertificates(Set.of("1111-1234"));

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        CertificateDto certificateDto = createCertificateDto();
        certificateDto.setPoints(1500);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(user));
        when(certificateRepository.findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE)).thenReturn(Collections.emptySet());
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(certificateDto);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        assertThrows(NotFoundException.class, () -> ubsService.processOrder("uuid", dto));
        verify(orderRepository).findById(1L);
        verify(userRepository).findUserByUuid("uuid");
        verify(certificateRepository).findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE);
        verify(modelMapper).map(certificate, CertificateDto.class);
        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
    }

    @Test
    void processOrderWFPClientCertificeteNotFoundExeption2() throws Exception {
        Order order = getOrderCount();
        Certificate certificate = ModelUtils.getCertificate();
        certificate.setPoints(1500);
        HashMap<Integer, Integer> value = new HashMap<>();
        value.put(1, 22);
        order.setAmountOfBagsOrdered(value);
        order.setPointsToUse(100);
        order.setSumTotalAmountWithoutDiscounts(1000_00L);
        order.setCertificates(Set.of(certificate));
        order.setPayment(TEST_PAYMENT_LIST);
        User user = getUser();
        user.setCurrentPoints(100);
        user.setChangeOfPointsList(new ArrayList<>());
        order.setUser(user);

        OrderWayForPayClientDto dto = getOrderWayForPayClientDto();
        dto.setCertificates(Set.of("1111-1234", "2222-1234"));

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        CertificateDto certificateDto = createCertificateDto();
        certificateDto.setPoints(1500);
        order.setPointsToUse(-1000);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(user));
        when(certificateRepository.findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE)).thenReturn(Set.of(certificate));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(certificateDto);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());

        assertThrows(NotFoundException.class, () -> ubsService.processOrder("uuid", dto));

        verify(orderRepository).findById(1L);
        verify(userRepository).findUserByUuid("uuid");
        verify(certificateRepository).findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE);
        verify(modelMapper).map(certificate, CertificateDto.class);
        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
    }

    @Test
    void processOrderWFPClientIfSumToPayLessThanPoints() throws Exception {
        Order order = getOrderCount();
        HashMap<Integer, Integer> value = new HashMap<>();
        value.put(1, 22);
        order.setAmountOfBagsOrdered(value);
        order.setPointsToUse(100);
        order.setSumTotalAmountWithoutDiscounts(1000_00L);
        order.setCertificates(Set.of(ModelUtils.getCertificate()));
        order.setPayment(TEST_PAYMENT_LIST);
        order.getPayment().getFirst().setAmount(1000_00L);
        User user = getUser();
        user.setCurrentPoints(100);
        user.setChangeOfPointsList(new ArrayList<>());
        order.setUser(user);
        order.setPointsToUse(-10000);
        order.updateWithNewOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        OrderWayForPayClientDto dto = getOrderWayForPayClientDto();
        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        Certificate certificate = ModelUtils.getCertificate();
        CertificateDto certificateDto = createCertificateDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(user));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(certificateDto);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        when(wayForPayClient.getCheckOutResponse(any())).thenReturn("{\"invoiceUrl\":\"link\"}");

        ubsService.processOrder("uuid", dto);

        verify(encryptionUtil).formRequestSignature(any(), any());
        verify(wayForPayClient).getCheckOutResponse(any());
    }

    @Test
    void processOrderWFPClientFailPaidOrder() {
        Order order = getOrderCountWithPaymentStatusPaid();
        OrderWayForPayClientDto dto = getOrderWayForPayClientDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
        assertThrows(BadRequestException.class, () -> ubsService.processOrder("uuid", dto));
    }

    @Test
    void checkIfTariffExistsByIdTest() {
        Long tariffId = 1L;
        when(tariffsInfoRepository.existsById(tariffId)).thenReturn(true);

        Assertions.assertTrue(ubsClientService.checkIfTariffExistsById(tariffId));
    }

    @Test
    void checkIfTariffExistsByIdTest_DoesNotExist() {
        Long tariffInfoId = 1L;
        when(tariffsInfoRepository.existsById(tariffInfoId)).thenReturn(false);

        assertFalse(ubsClientService.checkIfTariffExistsById(tariffInfoId));
    }

    @Test
    void getAllLocationsTest() {
        LocationsDto locationsDto = new LocationsDto();
        List<LocationsDto> expectedLocations = Collections.singletonList(locationsDto);
        when(locationRepository.findAllActiveLocations()).thenReturn(Collections.singletonList(new Location()));
        when(locationToLocationsDtoMapper.convert(any(Location.class))).thenReturn(locationsDto);

        List<LocationsDto> actualLocations = ubsClientService.getAllLocations();

        assertEquals(expectedLocations, actualLocations);

        verify(locationRepository).findAllActiveLocations();
        verify(locationToLocationsDtoMapper, times(expectedLocations.size())).convert(any(Location.class));
    }

    @Test
    void getTariffIdByLocationIdTest() {
        Long locationId = 1L;
        List<Long> tariffId = List.of(2L);
        when(tariffsInfoRepository.findTariffIdByLocationId(locationId)).thenReturn(Optional.of(tariffId));

        List<Long> actualTariffId = ubsClientService.getTariffIdByLocationId(locationId);

        assertEquals(tariffId, actualTariffId);

        verify(tariffsInfoRepository).findTariffIdByLocationId(locationId);
    }

    @Test
    void getTariffIdByLocationIdNotFoundTest() {
        Long locationId = 1L;
        when(tariffsInfoRepository.findTariffIdByLocationId(locationId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ubsClientService.getTariffIdByLocationId(locationId));

        verify(tariffsInfoRepository).findTariffIdByLocationId(locationId);
    }

    @Test
    void testGetAllLocationsByCourierId() {
        Long tariffId = 10L;
        Long id = 1L;
        Location location = new Location();
        location.setId(id);
        LocationsDto locationsDto = new LocationsDto();
        locationsDto.setId(id);

        when(locationRepository.findAllActiveLocationsByCourierId(id)).thenReturn(List.of(location));
        when(locationToLocationsDtoMapper.convert(location)).thenReturn(locationsDto);
        when(tariffsInfoRepository.findTariffIdByLocationIdAndCourierId(id, id)).thenReturn(Optional.of(tariffId));
        when(courierRepository.existsCourierById(id)).thenReturn(true);

        List<LocationsDto> result = ubsClientService.getAllLocationsByCourierId(id);

        assertEquals(1, result.size());
        assertEquals(tariffId, result.getFirst().getTariffsId());
        verify(locationRepository).findAllActiveLocationsByCourierId(id);
        verify(locationToLocationsDtoMapper).convert(location);
        verify(tariffsInfoRepository).findTariffIdByLocationIdAndCourierId(id, id);
    }

    @Test
    void testGetAllLocationsByCourierIdNotFound() {
        Long id = 1L;
        Location location = new Location();
        location.setId(id);
        LocationsDto locationsDto = new LocationsDto();
        locationsDto.setId(id);

        when(locationRepository.findAllActiveLocationsByCourierId(id)).thenReturn(List.of(location));
        when(locationToLocationsDtoMapper.convert(location)).thenReturn(locationsDto);
        when(tariffsInfoRepository.findTariffIdByLocationIdAndCourierId(id, id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            ubsClientService.getAllLocationsByCourierId(id);
        });

        assertEquals(COURIER_IS_NOT_FOUND_BY_ID + id, exception.getMessage());
        verify(locationRepository, never()).findAllActiveLocationsByCourierId(id);
        verify(locationToLocationsDtoMapper, never()).convert(location);
        verify(tariffsInfoRepository, never()).findTariffIdByLocationIdAndCourierId(id, id);
    }

    @Test
    void getAllLocationsByCourierId_ShouldReturnEmptyList_WhenNoLocationsExist() {
        Long id = 1L;
        when(locationRepository.findAllActiveLocationsByCourierId(id)).thenReturn(Arrays.asList());
        when(courierRepository.existsCourierById(id)).thenReturn(true);

        List<LocationsDto> result = ubsClientService.getAllLocationsByCourierId(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void processOrderWithMonoBankPaymentSystemTest() throws NoSuchFieldException, IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);
        OrderResponseDto dto = getOrderResponseDto();
        dto.setPaymentSystem(PaymentSystem.MONOBANK);

        List<BagDto> bags = new ArrayList<>();
        bags.add(new BagDto(1, 5));
        bags.add(new BagDto(2, 5));
        dto.setBags(bags);

        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);
        UBSuser ubSuser = getUBSuser();

        Field entityManagerField = UBSClientServiceImpl.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        EntityManager mockEntityManager = mock(EntityManager.class);
        entityManagerField.set(ubsClientService, mockEntityManager);

        when(userRepository.findByUuid(anyString())).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(anyLong())).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(monoBankClient.getCheckoutResponse(any(MonoBankPaymentRequestDto.class), eq(token)))
            .thenReturn(getCheckoutResponseFromMonoBank());
        doNothing().when(mockEntityManager).clear();

        PaymentSystemResponse result = ubsClientService.saveFullOrderToDB(dto, user.getUuid(), 1L);
        Assertions.assertNotNull(result);

        verify(userRepository, times(2)).findByUuid(anyString());
        verify(orderRepository, times(3)).findById(anyLong()); // Оновлено на 3 виклики
        verify(mockEntityManager, times(1)).clear();
        verify(monoBankClient, times(1)).getCheckoutResponse(any(MonoBankPaymentRequestDto.class), eq(token));
    }

    @Test
    void validatePaymentFromMonoBankWithSuccessStatusTest() {
        MonoBankPaymentResponseDto response = getMonoBankPaymentResponseDto("success");
        Order order = getOrder();

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(userNotificationRepository.findAllUserNotificationByOrderAndNotificationType(any(Order.class),
            any(NotificationType.class)))
            .thenReturn(List.of(getUserNotificationForUnpaidOrder()));
        when(notificationParameterRepository
            .findNotificationParameterByUserNotificationAndKey(any(UserNotification.class), anyString()))
            .thenReturn(getNotificationPaymentLink());

        ubsClientService.validatePaymentFromMonoBank(response);

        verify(orderRepository).findById(order.getId());
        verify(paymentRepository).save(any());
        verify(orderRepository).save(any());
        verify(eventService, times(2)).save(anyString(), anyString(), any());
        verify(userNotificationRepository)
            .findAllUserNotificationByOrderAndNotificationType(any(Order.class), any(NotificationType.class));
        verify(notificationParameterRepository)
            .findNotificationParameterByUserNotificationAndKey(any(UserNotification.class), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"failure", "reversed", "created", "processing", "hold", "expired"})
    void validatePaymentFromMonoBankWithErrorsTest(String status) {
        MonoBankPaymentResponseDto response = getMonoBankPaymentResponseDto(status);
        Order order = getOrder();

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        ubsClientService.validatePaymentFromMonoBank(response);

        verify(orderRepository).findById(order.getId());
        verify(paymentRepository).save(any());
    }

    @Test
    void validatePaymentFromMonoBankThrowExceptionTest() {
        MonoBankPaymentResponseDto response = getMonoBankPaymentResponseDto(null);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
            () -> ubsClientService.validatePaymentFromMonoBank(response));
    }

    @Test
    void processOrderIfPaidWithBonusesTest() throws NoSuchFieldException, IllegalAccessException {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        User user = getUserWithLastLocation();
        user.setCurrentPoints(360);
        String uuid = user.getUuid();
        OrderResponseDto dto = getOrderResponseDto();
        dto.setBags(Collections.singletonList(new BagDto(3, 3)));
        dto.setPointsToUse(360);
        TariffsInfo tariffsInfo = getTariffsInfo();
        UBSuser ubSuser = getUBSuser();

        Field entityManagerField = UBSClientServiceImpl.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        EntityManager mockEntityManager = mock(EntityManager.class);
        entityManagerField.set(ubsClientService, mockEntityManager);

        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(getAddress()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(ubsUserRepository.findById(anyLong())).thenReturn(Optional.of(ubSuser));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(getBag()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        doNothing().when(orderBagRepository).deleteAllByOrderId(anyLong());
        doNothing().when(mockEntityManager).clear();

        PaymentSystemResponse paymentSystemResponse = ubsClientService.saveFullOrderToDB(dto, uuid, 1L);

        verify(userRepository, times(2)).findByUuid(uuid);
        verify(addressRepository).findById(anyLong());
        verify(tariffsInfoRepository).findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(ubsUserRepository).findById(anyLong());
        verify(bagRepository).findActiveBagById(anyInt());
        verify(orderRepository, times(2)).findById(anyLong());
        verify(modelMapper).map(dto.getPersonalData(), UBSuser.class);
        verify(monoBankClient, times(0)).getCheckoutResponse(any(MonoBankPaymentRequestDto.class), eq(token));
        verify(mockEntityManager, times(1)).clear();

        assertEquals("", paymentSystemResponse.link());
        assertEquals(1L, paymentSystemResponse.orderId());
    }
}
