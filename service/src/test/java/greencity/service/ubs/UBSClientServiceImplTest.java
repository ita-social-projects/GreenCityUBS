package greencity.service.ubs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
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
import greencity.dto.payment.PaymentCancellationWayForPayRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
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
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
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
import greencity.exceptions.certificate.CertificateIsNotActivated;
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
import greencity.repository.TelegramChatRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserNotificationRepository;
import greencity.repository.UserRepository;
import greencity.service.google.GoogleApiService;
import greencity.service.notification.NotificationServiceImpl;
import greencity.persistence.JpqlQueryHelperImpl;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
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
import static greencity.ModelUtils.getCertificate;
import static greencity.ModelUtils.getCourier;
import static greencity.ModelUtils.getCourierDto;
import static greencity.ModelUtils.getCourierDtoList;
import static greencity.ModelUtils.getDtoWithLanguage;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getEvent1;
import static greencity.ModelUtils.getEvent2;
import static greencity.ModelUtils.getGeocodingResultWithKyivRegion;
import static greencity.ModelUtils.getLocation;
import static greencity.ModelUtils.getNotificationPaymentLink;
import static greencity.ModelUtils.getOrder;
import static greencity.ModelUtils.getOrder2;
import static greencity.ModelUtils.getOrderAddress;
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
import static greencity.ModelUtils.getUserWithInitializedFields;
import static greencity.ModelUtils.getUserWithLastLocation;
import static greencity.constant.AppConstant.USER_WITH_PREFIX;
import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.CERTIFICATE_EXPIRED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_NOT_ACTIVATED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_USED;
import static greencity.constant.ErrorMessage.CERTIFICATE_NOT_FOUND_BY_CODE;
import static greencity.constant.ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.LOCATION_IS_DEACTIVATED_FOR_TARIFF;
import static greencity.constant.ErrorMessage.NOT_ENOUGH_BAGS_EXCEPTION;
import static greencity.constant.ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER;
import static greencity.constant.ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER;
import static greencity.constant.ErrorMessage.ORDER_IN_ONGOING_PROCESSING;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.PAYMENT_VALIDATION_ERROR;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_GREATER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_LOWER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND;
import static greencity.constant.ErrorMessage.TARIFF_OR_LOCATION_IS_DEACTIVATED;
import static greencity.constant.ErrorMessage.TOO_MANY_BAGS_EXCEPTION;
import static greencity.constant.ErrorMessage.TOO_MANY_CERTIFICATES;
import static greencity.constant.ErrorMessage.UNABLE_TO_CANCEL_PAYMENT_INVOICE;
import static greencity.constant.ErrorMessage.USER_DONT_HAVE_ENOUGH_POINTS;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS;
import static greencity.constant.QuartzConstants.NO_PAYMENT_ATTEMPT_FOR_ORDER;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_CANCEL_EXCEPTION;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_GROUP;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_KEY;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_SCHEDULE_EXCEPTION;
import static greencity.constant.QuartzConstants.QUARTZ_SCHEDULER_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
    private ObjectMapper objectMapper;

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
    private TelegramChatRepository telegramBotRepository;

    @Mock
    private Scheduler quartzScheduler;

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
    private JpqlQueryHelperImpl jpqlQueryHelperImpl;

    @Mock
    private NotificationServiceImpl notificationServiceImpl;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private NotificationParameterRepository notificationParameterRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private JobDetail jobDetail;

    private static MockedStatic<SecurityContextHolder> mockedContextHolder;

    @BeforeAll
    static void setUp() {
        mockedContextHolder = mockStatic(SecurityContextHolder.class);
    }

    @AfterAll
    static void tearDown() {
        mockedContextHolder.close();
    }

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
        User user = getUser();
        String uuid = user.getUuid();

        Order order = getOrderWithTariffAndLocation();
        order.setUser(user);
        Long orderId = order.getId();

        TariffsInfo tariffsInfo = order.getTariffsInfo();
        Long tariffsInfoId = tariffsInfo.getId();
        Location location = order
            .getUbsUser()
            .getOrderAddress()
            .getLocation();
        TariffLocation tariffLocation = getTariffLocation();

        var bags = getBag1list();
        var userPointsAndAllBagsDtoExpected = ModelUtils.getUserPointsAndAllBagsDtoWithQuantity();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(tariffsInfoId)).thenReturn(bags);
        when(orderBagRepository.getAmountOfOrderBagsByOrderIdAndBagId(anyLong(), anyInt()))
            .thenReturn(Optional.of(2));

        UserPointsAndAllBagsDto userPointsAndAllBagsDtoActual =
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
        User user = getUser();
        String uuid = user.getUuid();

        Order order = getOrderWithoutPayment();
        Long orderId = order.getId();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> ubsService.getFirstPageDataByOrderId(
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
        User user = getUser();
        String uuid = user.getUuid();

        Order order = getOrderWithoutPayment();
        Long orderId = order.getId();

        String expectedErrorMessage = ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId;

        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.getFirstPageDataByOrderId(uuid, orderId));
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
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        Order order = getOrder();
        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        UBSuser ubsUser = getUBSuser();

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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

        when(locationRepository.findAddressAndLocationNamesMatch(anyLong(), anyLong()))
            .thenReturn(Optional.of("Bearded Lady"));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("http://example.com/invoice", result.link());
    }

    @Test
    void testSaveToDBThrowsAddressNotWithinLocationAreaException() {
        OrderResponseDto dto = getOrderResponseDto();
        dto.setAddressId(1L);
        dto.setLocationId(3L);

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(locationRepository.findAddressAndLocationNamesMatch(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(AddressNotWithinLocationAreaException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));

        verify(addressRepository, times(1)).findById(anyLong());
        verify(locationRepository, times(1)).findAddressAndLocationNamesMatch(anyLong(), anyLong());
        verify(userRepository, never()).findByUuid(anyString());
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

        assertThrows(AddressNotWithinLocationAreaException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));

        verify(addressRepository).findById(anyLong());
    }

    @Test
    void testSaveToDBWithoutAddressThrowsNotFoundException() throws NotFoundException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setAddressId(1L);
        dto.setLocationId(1L);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);

        assertThrows(NotFoundException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void testSaveToDB_AddressNotEqualsUsers() throws IllegalAccessException {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));
        user.setChangeOfPointsList(new ArrayList<>());

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(addressRepository.findById(any())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user.setId(null), user);
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(addressRepository.findById(any())).thenReturn(Optional.of(getAddress().setUser(getTestUser())));
        when(locationRepository.findById(any())).thenReturn(Optional.of(getLocation()));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER));
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        when(addressRepository.findById(any())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user.setId(null), user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser.setId(null));
        Address address = getAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.DELETED);
        when(addressRepository.findById(any()))
            .thenReturn(Optional.of(address));
        when(locationRepository.findById(any())).thenReturn(Optional.of(getLocation()));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER));
    }

    @Test
    void testSaveToDBWithTwoBags() throws IllegalAccessException {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setBags(List.of(BagDto.builder().id(1).amount(1).build(), BagDto.builder().id(3).amount(15).build()));
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);

        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag1 = getBagForOrder();
        bag1.setId(1);
        bag1.setCapacity(100);
        bag1.setLimitIncluded(false);
        Bag bag3 = getBagForOrder();
        bag3.setCapacity(1000);
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Arrays.asList(bag1, bag3));

        Order order1 = getOrder();
        order1.setPayment(List.of(getPayment()));
        order.setOrderBags(
            Arrays.asList(ModelUtils.getOrderBag(), ModelUtils.getOrderBag(), ModelUtils.getOrderBag()));
        order1
            .setOrderBags(
                Arrays.asList(ModelUtils.getOrderBag(), ModelUtils.getOrderBag(), ModelUtils.getOrderBag()));

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        UBSuser ubsUser = getUBSuser();

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(1)).thenReturn(Optional.of(bag1));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag3));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf");
        Assertions.assertNotNull(result);
    }

    @Test
    void testSaveToDBWithCertificates() throws IllegalAccessException {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setCertificates(Set.of("4444-4444"));
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Collections.singletonList(bag));

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

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
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(certificateRepository.findById(anyString())).thenReturn(Optional.of(getActiveCertificateWith10Points()));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf");
        Assertions.assertNotNull(result);
    }

    @Test
    void testSaveToDBWithMarkCertificateAsUsedIfNoPaymentNeeded() {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setCertificates(Set.of("4444-4444"));

        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        user.setOrders(new ArrayList<>(List.of(order)));

        Bag bag = getBagForOrder();

        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Collections.singletonList(bag));
        Certificate certificate = getActiveCertificateWith10Points();
        certificate.setPoints(1000_00);

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(certificateRepository.findById(anyString())).thenReturn(Optional.of(certificate));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        PaymentSystemResponse result = ubsService
            .processExistingOrder(dto, "35467585763t4sfgchjfuyetf", order.getId());
        Assertions.assertNotNull(result);

        verify(userRepository, times(1)).findByUuid("35467585763t4sfgchjfuyetf");
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    void testSaveToDBWhenSumToPayLessThanPoints() throws IllegalAccessException {
        User user = getUserWithInitializedFields();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(5_000);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(2);
        dto.setPointsToUse(2_000);
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Collections.singletonList(bag));

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

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
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf");
        Assertions.assertNotNull(result);
    }

    @Test
    void testSaveToDbThrowBadRequestExceptionPriceLowerThanLimit() {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(1);
        Bag bag = getBagForOrder();
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));
        user.setChangeOfPointsList(new ArrayList<>());

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMin(1000L);
        tariffsInfo.setBags(Collections.singletonList(bag));

        UBSuser ubsUser = getUBSuser();

        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);

        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(PRICE_OF_ORDER_LOWER_THAN_LIMIT));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(1)).findActiveBagById(anyInt());
    }

    @Test
    void testSaveToDbThrowBadRequestExceptionPriceGreaterThanLimit() {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(100);
        dto.setAddressId(1L);
        dto.setLocationId(1L);

        Order order = getOrder();

        Bag bag = getBagForOrder();
        bag.setFullPrice(100_000L);

        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMax(10_000L);
        tariffsInfo.setBags(Collections.singletonList(bag));

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(order);
        when(modelMapper.map(any(PersonalDataDto.class), eq(UBSuser.class))).thenReturn(ubsUser);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(PRICE_OF_ORDER_GREATER_THAN_LIMIT));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(1)).findActiveBagById(anyInt());
    }

    @Test
    void testSaveToDBWithTooManyBagsThrowsBadRequestException() {
        User user = getUserWithInitializedFields();
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

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(order);
        when(modelMapper.map(any(PersonalDataDto.class), eq(UBSuser.class))).thenReturn(ubsUser);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(TOO_MANY_BAGS_EXCEPTION));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(1)).findActiveBagById(anyInt());
    }

    @Test
    void testSaveToDBWShouldThrowTariffNotFoundException() {
        User user = getUserWithInitializedFields();
        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        when(locationRepository.findAddressAndLocationNamesMatch(anyLong(), anyLong()))
            .thenReturn(Optional.of("Test City"));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(userRepository.findByUuid(anyString())).thenReturn(user);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(orderAddressRepository.save(any(OrderAddress.class))).thenReturn(orderAddress);

        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertEquals(
            String.format(TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST, List.of(3), 1L), exception.getMessage());

        verify(tariffsInfoRepository).findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
    }

    @Test
    void testSaveToDBThrowsBagNotFoundException() {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));
        user.setChangeOfPointsList(new ArrayList<>());

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubSuser = getUBSuser();

        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfo()));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(BAG_NOT_FOUND));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository).findActiveBagById(3);

    }

    @Test
    void testSaveToDBWithoutOrderUnpaid() {
        User user = getUserWithInitializedFields();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);
        OrderResponseDto dto = getOrderResponseDto(false);
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        user.setOrders(new ArrayList<>(List.of(order)));
        user.setChangeOfPointsList(new ArrayList<>());
        order.setUser(user);
        Bag bag = getBagForOrder();

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        PaymentSystemResponse result = ubsClientService
            .processExistingOrder(dto, "35467585763t4sfgchjfuyetf", 1L);
        Assertions.assertNotNull(result);

        verify(userRepository, times(1)).findByUuid("35467585763t4sfgchjfuyetf");
        verify(tariffsInfoRepository, times(1)).findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(modelMapper, times(1)).map(dto.getPersonalData(), UBSuser.class);
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @ParameterizedTest
    @EnumSource(value = OrderPaymentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "UNPAID")
    void saveToDBFailPaidOrder(OrderPaymentStatus orderPaymentStatus) {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(1000);

        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(orderPaymentStatus);

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processExistingOrder(dto, "35467585763t4sfgchjfuyetf", 1L));
        assertTrue(exception.getMessage().contains(ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED));
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "FORMED")
    void saveToDBFailOrderStatusNotFormed(OrderStatus orderStatus) {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(1000);

        OrderResponseDto dto = getOrderResponseDto();
        dto.setShouldBePaid(false);
        Order order = getOrder();
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        order.setOrderStatus(orderStatus);

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getAddress()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processExistingOrder(dto, "35467585763t4sfgchjfuyetf", 1L));
        assertTrue(exception.getMessage().contains(ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED));
    }

    @Test
    void testSaveToDBWithoutEnoughBagsThrowsBadRequestException() {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(3);
        dto.setShouldBePaid(false);
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBags();
        bag.setTariffsInfo(tariffsInfo);

        Address address = getAddress();
        address.setUser(user);
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(order);
        when(modelMapper.map(any(PersonalDataDto.class), eq(UBSuser.class))).thenReturn(ubsUser);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(NOT_ENOUGH_BAGS_EXCEPTION));
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
            () -> ubsService.updateUbsUserInfoInOrder(request, userUuid));

        verify(ubsUserRepository).findById(1L);

        mockedContextHolder.verify(SecurityContextHolder::getContext, atLeastOnce());
        verify(securityContext).getAuthentication();
        verify(authentication).getAuthorities();
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
        TelegramChat telegramBot = getTelegramBotNotifyTrue();
        List<AddressDto> addressDto = addressDtoList();
        List<Bot> botList = botList();
        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDto();
        String uuid = UUID.randomUUID().toString();
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(telegramBotRepository.findByUser(user)).thenReturn(Optional.of(telegramBot));
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

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramBotRepository).findByUser(user);
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
        TelegramChat telegramBot = getTelegramBotNotifyTrue();
        List<AddressDto> addressDto = addressDtoListWithNullPlaceId();

        UserProfileUpdateDto userProfileUpdateDto = getUserProfileUpdateDto();
        userProfileUpdateDto.getAddressDto().get(0).setPlaceId(null);
        userProfileUpdateDto.getAddressDto().get(1).setPlaceId(null);

        String uuid = UUID.randomUUID().toString();
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequestWithNullPlaceId();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(telegramBotRepository.findByUser(user)).thenReturn(Optional.of(telegramBot));
        when(modelMapper.map(addressDto.get(0), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        when(modelMapper.map(addressDto.get(1), OrderAddressDtoRequest.class)).thenReturn(updateAddressRequestDto);
        doReturn(new OrderWithAddressesResponseDto()).when(addressService)
            .updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        ubsClientServiceSpy.updateProfileData(uuid, userProfileUpdateDto);

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramBotRepository).findByUser(user);
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
        String language = "uk";
        Event event1 = getEvent1();
        Event event2 = getEvent2();
        EventDto eventDto1 = getDtoWithLanguage(language, event1);
        EventDto eventDto2 = getDtoWithLanguage(language, event2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(new Order()));
        when(eventRepository.findAllEventsByOrderId(anyLong())).thenReturn(List.of(event1, event2));
        when(modelMapper.map(event1, EventDto.class)).thenReturn(eventDto1);
        when(modelMapper.map(event2, EventDto.class)).thenReturn(eventDto2);

        List<EventDto> result = ubsService.getAllEventsForOrder(orderId, anyString(), "uk");

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
    void deleteOrder() throws SchedulerException {
        Order order = getOrder();
        when(ordersForUserRepository.getAllByUserUuidAndId(order.getUser().getUuid(), order.getId()))
            .thenReturn(order);
        when(quartzScheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        ubsService.deleteOrder(order.getUser().getUuid(), 1L);

        verify(orderRepository).saveAndFlush(order);
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
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setCertificates(Set.of(getActiveCertificateWith10Points().getCode()));
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));

        Bag bag = getBag();
        bag.setCapacity(100);
        bag.setFullPrice(400_00L);
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubSuser = getUBSuser();

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        order1.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

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

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(getActiveCertificateWith10Points()));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order1));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String mockWayForPayResponse = "{\"invoiceUrl\": \"http://example.com/invoice\"}";
        when(wayForPayClient.getCheckOutResponse(any(PaymentWayForPayRequestDto.class)))
            .thenReturn(mockWayForPayResponse);

        PaymentSystemResponse result = ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf");
        Assertions.assertNotNull(result);
    }

    @Test
    void saveFullOrderToDBWhenSumToPayEqualsZero() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(6000);
        user.setUbsUsers(getUbsUsers());

        OrderResponseDto dto = getOrderResponseDto();
        dto.setAddressId(1L);
        dto.setPointsToUse(6000);
        dto.getBags().getFirst().setAmount(15);
        dto.setShouldBePaid(true);
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);

        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());
        order.setOrderBags(Arrays.asList(ModelUtils.getOrderBag(), ModelUtils.getOrderBag()));
        Bag bag = getBagForOrder();
        UBSuser ubSuser = getUBSuser().setId(null);
        Address address = getAddress();
        address.setUser(user);
        var location = getLocation();
        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setLocation(location);
        orderAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);
        address.getBaseAddress().setAddressStatus(AddressStatus.NEW);

        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffsInfo()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(addressRepository.findById(any())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        PaymentSystemResponse result =
            ubsClientService.processExistingOrder(dto, "35467585763t4sfgchjfuyetf", order.getId());

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.link() == null || result.link().isEmpty());
        Assertions.assertEquals(0, user.getCurrentPoints());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testSaveToDBfromIForIFThrowsException() throws IllegalAccessException {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(3);
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));

        Bag bag = getBagForOrder();
        bag.setTariffsInfo(getTariffInfoWithLimitOfBags());

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

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

        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(order);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffInfoWithLimitOfBags()));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void testCheckSumIfCourierLimitBySumOfOrderForIF1() throws IllegalAccessException {
        User user = getUserWithInitializedFields();
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

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(order);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));

        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(PRICE_OF_ORDER_LOWER_THAN_LIMIT));
    }

    @Test
    void testCheckSumIfCourierLimitBySumOfOrderForIF2() throws IllegalAccessException {
        User user = getUserWithInitializedFields();
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

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(order);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(PRICE_OF_ORDER_GREATER_THAN_LIMIT));
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

        TypedQuery<Order> query = mock(TypedQuery.class);

        when(jpqlQueryHelperImpl
            .createPageableTypedQueryWithEntityGraph(eq(Order.class), anyString(), anyList(), any(Pageable.class)))
            .thenReturn(query);
        when(jpqlQueryHelperImpl.runPageableTypedQueryWithEntityGraph(eq(query), any(Pageable.class)))
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
    }

    @Test
    void testOrdersForUserWithExportedQuantity() {
        OrderStatusTranslation orderStatusTranslation = getOrderStatusTranslation();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = getOrderPaymentStatusTranslation();
        Order order = getOrderTest();
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
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

        TypedQuery<Order> query = mock(TypedQuery.class);

        when(jpqlQueryHelperImpl
            .createPageableTypedQueryWithEntityGraph(eq(Order.class), anyString(), anyList(), any(Pageable.class)))
            .thenReturn(query);
        when(jpqlQueryHelperImpl.runPageableTypedQueryWithEntityGraph(eq(query), any(Pageable.class)))
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        TypedQuery<Order> query = mock(TypedQuery.class);

        when(jpqlQueryHelperImpl
            .createPageableTypedQueryWithEntityGraph(eq(Order.class), anyString(), anyList(), any(Pageable.class)))
            .thenReturn(query);
        when(jpqlQueryHelperImpl.runPageableTypedQueryWithEntityGraph(eq(query), any(Pageable.class)))
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        TariffsInfo tariffsInfo = getTariffsInfo();
        tariffsInfo.setBags(Collections.singletonList(getBag()));
        order.setTariffsInfo(tariffsInfo);

        TypedQuery<Order> query = mock(TypedQuery.class);

        when(jpqlQueryHelperImpl
            .createPageableTypedQueryWithEntityGraph(eq(Order.class), anyString(), anyList(), any(Pageable.class)))
            .thenReturn(query);
        when(jpqlQueryHelperImpl.runPageableTypedQueryWithEntityGraph(eq(query), any(Pageable.class)))
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
    void checkIfAddressHasBeenDeletedTest() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        Order order = getOrder();

        Address address = ModelUtils.getAddress();
        address.getBaseAddress().setAddressStatus(AddressStatus.DELETED);
        address.setUser(user);

        Location location = getLocation();

        UBSuser mappedFromDtoUser = getUBSuser();
        mappedFromDtoUser.setId(null);

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(mappedFromDtoUser);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER));
    }

    @Test
    void saveOrderAddressWithLocationFailsWhenAddressBelongsToDifferentUser() {
        User user = getUserWithLastLocation();
        user.setId(1L);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        dto.setPaymentSystem(PaymentSystem.WAY_FOR_PAY);

        Address address = getAddress();
        address.setUser(ModelUtils.getTestUser().setId(2L));

        Location location = getLocation();

        UBSuser mappedFromDtoUser = getUBSuser();
        mappedFromDtoUser.setId(null);
        Order order = getOrder();

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(mappedFromDtoUser);
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(order);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertTrue(exception.getMessage().contains(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER));
    }

    @Test
    void checkIfUserHaveEnoughPointsTest() throws IllegalAccessException {
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(100);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>(List.of(order)));

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

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

        UBSuser ubsUser = getUBSuser();

        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processNewOrder(dto, "35467585763t4sfgchjfuyetf"));
        assertEquals(USER_DONT_HAVE_ENOUGH_POINTS, exception.getMessage());
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(List.of(bag));
        order.setTariffsInfo(tariffsInfo);

        TypedQuery<Order> query = mock(TypedQuery.class);

        when(jpqlQueryHelperImpl
            .createPageableTypedQueryWithEntityGraph(eq(Order.class), anyString(), anyList(), any(Pageable.class)))
            .thenReturn(query);
        when(jpqlQueryHelperImpl.runPageableTypedQueryWithEntityGraph(eq(query), any(Pageable.class)))
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
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> ubsService.createUserProfile(userProfileCreateDto));
        assertEquals(USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS, ex.getMessage());
        verify(userRemoteClient, times(1)).checkIfUserExistsByUuid(userProfileCreateDto.getUuid());
        verify(userRepository, times(1)).findByUuid(userProfileCreateDto.getUuid());
        verify(userRepository, times(0)).save(any(User.class));
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
    void testValidatePaymentSuccess() throws SchedulerException {
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
        when(quartzScheduler.deleteJob(any(JobKey.class))).thenReturn(true);

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
    void testValidatePaymentWhenSchedulerFailsToCancelJob() throws SchedulerException {
        PaymentResponseDto response = getPaymentResponseDto();

        Order expectedOrder = getOrder2();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(expectedOrder));
        when(userNotificationRepository.findAllUserNotificationByOrderAndNotificationType(any(Order.class),
            any(NotificationType.class)))
            .thenReturn(List.of(getUserNotificationForUnpaidOrder()));
        when(notificationParameterRepository
            .findNotificationParameterByUserNotificationAndKey(any(UserNotification.class), anyString()))
            .thenReturn(getNotificationPaymentLink());
        doThrow(SchedulerException.class).when(quartzScheduler).deleteJob(any(JobKey.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ubsClientService.validatePayment(response));

        assertEquals(QUARTZ_SCHEDULER_EXCEPTION, exception.getMessage());

        verify(orderRepository).findById(1L);
        verify(userNotificationRepository)
            .findAllUserNotificationByOrderAndNotificationType(any(Order.class), any(NotificationType.class));
        verify(notificationParameterRepository)
            .findNotificationParameterByUserNotificationAndKey(any(UserNotification.class), anyString());
    }

    @Test
    void testConvertMapIntoPaymentResponseDto_emptyMap() {
        PaymentResponseWayForPay result = ubsClientService.convertMapIntoPaymentResponseDto(Collections.emptyMap());

        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertNull(result.getOrderReference());
    }

    @Test
    void testConvertMapIntoPaymentResponseDto_invalidJson() throws Exception {
        Map<String, String> params = Map.of("invalid", "not_json");

        when(objectMapper.readValue(anyString(), eq(PaymentResponseDto.class)))
            .thenThrow(new JsonProcessingException("malformed JSON") {
            });

        PaymentResponseWayForPay result = ubsClientService.convertMapIntoPaymentResponseDto(params);

        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertNull(result.getOrderReference());
    }

    @Test
    void testConvertMapIntoPaymentResponseDto_invalidSignature() throws Exception {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setMerchantSignature("wrong");
        dto.setOrderReference("ORD123");

        Map<String, String> params = Map.of("json", "dummy");

        when(objectMapper.readValue(anyString(), eq(PaymentResponseDto.class))).thenReturn(dto);
        when(encryptionUtil.generateResponseSignature(dto, wayForPaySecret)).thenReturn("correct");

        PaymentResponseWayForPay result = ubsClientService.convertMapIntoPaymentResponseDto(params);

        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertNull(result.getOrderReference());
    }

    @Test
    void testConvertMapIntoPaymentResponseDto_valid() throws Exception {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setMerchantSignature("correct");
        dto.setOrderReference("ORD123");

        Map<String, String> params = Map.of("json", "dummy");

        when(objectMapper.readValue(anyString(), eq(PaymentResponseDto.class))).thenReturn(dto);
        when(encryptionUtil.generateResponseSignature(dto, wayForPaySecret)).thenReturn("correct");

        UBSClientServiceImpl spyService = Mockito.spy(ubsClientService);
        PaymentResponseWayForPay expected = new PaymentResponseWayForPay();
        doReturn(expected).when(spyService).validatePayment(dto);

        PaymentResponseWayForPay result = spyService.convertMapIntoPaymentResponseDto(params);

        assertSame(expected, result);
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        order.setPointsToUse(-10000);
        CertificateDto certificateDto = createCertificateDto();
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
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
    void processOrderWFPClientWhenSchedulerFails() throws Exception {
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
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));
        Field[] fields = UBSClientServiceImpl.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("merchantId")) {
                f.setAccessible(true);
                f.set(ubsService, "1");
            }
        }

        order.setPointsToUse(-10000);
        CertificateDto certificateDto = createCertificateDto();
        order.setOrderBags(Collections.singletonList(ModelUtils.getOrderBag()));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(user));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(certificateDto);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(certificateRepository.findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE)).thenReturn(Set.of(certificate));
        when(orderBagService.getActualBagsAmountForOrder(Collections.singletonList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        when(wayForPayClient.getCheckOutResponse(any())).thenReturn("{\"invoiceUrl\":\"link\"}");
        doThrow(SchedulerException.class).when(quartzScheduler)
            .scheduleJob(any(JobDetail.class), any(Trigger.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ubsService.processOrder("uuid", dto));

        assertEquals(PAYMENT_EXPIRY_SCHEDULE_EXCEPTION, exception.getMessage());

        verify(userRepository).findUserByUuid("uuid");
        verify(certificateRepository).findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE);
        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
    }

    @Test
    void processOrderClientWhenInPayment() {
        OrderWayForPayClientDto dto = getOrderWayForPayClientDto();

        Order order = getOrder();
        order.setPaymentLink("https://pay.example.com/invoice/TEST123");

        User user = getUser();
        String uuid = user.getUuid();

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsClientService.processOrder(uuid, dto));

        assertEquals(ORDER_IN_ONGOING_PROCESSING, exception.getMessage());
    }

    @Test
    void processExistingOrderWhenInPayment() {
        OrderResponseDto dto = getOrderResponseDto();

        Order order = getOrder();
        order.setPaymentLink("https://pay.example.com/invoice/TEST123");
        Long orderId = order.getId();

        User user = getUser();
        String uuid = user.getUuid();

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.processExistingOrder(dto, uuid, orderId));

        assertEquals(ORDER_IN_ONGOING_PROCESSING, exception.getMessage());
    }

    @Test
    void formedLinkWhenInPayment() {
        Order order = getOrder();
        order.setPaymentLink("https://pay.example.com/invoice/TEST123");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsClientService.formedLink(order, 500));

        assertEquals(ORDER_IN_ONGOING_PROCESSING, exception.getMessage());
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

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsClientService.getAllLocationsByCourierId(id));

        assertEquals(COURIER_IS_NOT_FOUND_BY_ID + id, exception.getMessage());
        verify(locationRepository, never()).findAllActiveLocationsByCourierId(id);
        verify(locationToLocationsDtoMapper, never()).convert(location);
        verify(tariffsInfoRepository, never()).findTariffIdByLocationIdAndCourierId(id, id);
    }

    @Test
    void getAllLocationsByCourierId_ShouldReturnEmptyList_WhenNoLocationsExist() {
        Long id = 1L;
        when(locationRepository.findAllActiveLocationsByCourierId(id)).thenReturn(List.of());
        when(courierRepository.existsCourierById(id)).thenReturn(true);

        List<LocationsDto> result = ubsClientService.getAllLocationsByCourierId(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void processOrderIfPaidWithBonusesTest() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        User user = getUserWithInitializedFields();
        user.setCurrentPoints(360);
        String uuid = user.getUuid();
        OrderResponseDto dto = getOrderResponseDto();
        dto.setBags(Collections.singletonList(new BagDto(3, 3)));
        dto.setPointsToUse(360);
        TariffsInfo tariffsInfo = getTariffsInfo();

        Address address = getAddress();
        address.setUser(user);
        Location location = getLocation();
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setLocation(location);

        UBSuser ubsUser = getUBSuser();

        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(address, OrderAddress.class)).thenReturn(orderAddress);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(getBag()));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubsUser);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        PaymentSystemResponse paymentSystemResponse = ubsClientService
            .processExistingOrder(dto, uuid, 1L);

        assertEquals("", paymentSystemResponse.link());
        assertEquals(1L, paymentSystemResponse.orderId());

        verify(userRepository, times(1)).findByUuid(uuid);
        verify(addressRepository, times(2)).findById(anyLong());
        verify(tariffsInfoRepository).findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository).findActiveBagById(anyInt());
        verify(orderRepository, times(1)).findById(anyLong());
        verify(modelMapper).map(dto.getPersonalData(), UBSuser.class);
    }

    @Test
    void getOrdersForUserWhenStatusesProvided() {
        String uuid = "user-uuid";
        Pageable pageable = PageRequest.of(0, 1);
        List<OrderStatus> statuses = List.of(OrderStatus.FORMED);
        Order order = getOrderTest();
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        TypedQuery<Order> query = mock(TypedQuery.class);

        when(jpqlQueryHelperImpl
            .createPageableTypedQueryWithEntityGraph(eq(Order.class), anyString(), anyList(), any(Pageable.class)))
            .thenReturn(query);
        when(jpqlQueryHelperImpl.runPageableTypedQueryWithEntityGraph(eq(query), any(Pageable.class)))
            .thenReturn(page);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(anyLong()))
            .thenReturn(Optional.of(getOrderStatusTranslation()));
        when(orderPaymentStatusTranslationRepository.getById(anyLong()))
            .thenReturn(getOrderPaymentStatusTranslation());
        when(orderBagService.getActualBagsAmountForOrder(any())).thenReturn(ModelUtils.getAmount());

        PageableDto<OrdersDataForUserDto> result = ubsService.getOrdersForUser(uuid, pageable, statuses);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateUbsUserInfoInOrderWithUBSEmployeeRole() {
        UbsCustomersDtoUpdate update = UbsCustomersDtoUpdate.builder()
            .customerId(1L)
            .build();
        UBSuser ubsUser = getUBSuser();
        User user = getUser();
        ubsUser.setUser(user);

        mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_UBS_EMPLOYEE")))
            .when(authentication).getAuthorities();
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubsUser));
        when(ubsUserRepository.save(any())).thenReturn(ubsUser);
        doNothing().when(eventService).save(anyString(), anyString(), any());

        ubsService.updateUbsUserInfoInOrder(update, user.getUuid());

        verify(eventService).save(anyString(), any(), any());
    }

    @Test
    void processOrderWhenSumToPayInCoinsLessThenZero() {
        Certificate cert = ModelUtils.getCertificate();
        cert.setPoints(5);
        cert.setCertificateStatus(CertificateStatus.ACTIVE);

        Order order = getOrderCount();
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        order.setOrderStatus(OrderStatus.FORMED);
        order.setCounterOrderPaymentId(null);
        order.setUser(getUser().setCurrentPoints(200));
        order.setPointsToUse(0);
        order.setCertificates(Set.of(cert));
        order.setSumTotalAmountWithoutDiscounts(100L);

        OrderWayForPayClientDto dto = getOrderWayForPayClientDto();
        dto.setPointsToUse(1);
        dto.setCertificates(Set.of("cert1"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(order.getUser()));
        when(certificateRepository.findByCodeInAndCertificateStatus(any(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(Set.of(cert));
        when(orderBagService.getActualBagsAmountForOrder(any())).thenReturn(ModelUtils.getAmount());
        when(modelMapper.map(any(Certificate.class), eq(CertificateDto.class)))
            .thenReturn(CertificateDto.builder().code("cert1").points(5).build());
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(wayForPayClient.getCheckOutResponse(any())).thenReturn("{\"invoiceUrl\":\"link\"}");

        ubsService.processOrder(order.getUser().getUuid(), dto);
        verify(orderRepository, atLeastOnce()).save(order);
        verify(certificateRepository).findByCodeInAndCertificateStatus(any(), eq(CertificateStatus.ACTIVE));
    }

    @Test
    void processNewOrderWhenTooManyCertificates() {
        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(3);
        dto.setCertificates(Set.of("cert1", "cert2", "cert3", "cert4", "cert5", "cert6"));
        String userUuid = "test-uuid";
        User testUser = ModelUtils.getUser();
        testUser.setUuid(userUuid);
        testUser.setCurrentPoints(1000);
        Address address = ModelUtils.getAddress();
        address.setUser(testUser);

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(new HashSet<>());
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(userRepository.findByUuid(userUuid)).thenReturn(testUser);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(getLocation()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffsInfo()));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(getBag()));
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(getOrder());
        when(modelMapper.map(any(Address.class), eq(OrderAddress.class))).thenReturn(getOrderAddress());
        when(modelMapper.map(any(PersonalDataDto.class), eq(UBSuser.class))).thenReturn(getUBSuser());

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> ubsClientService.processNewOrder(dto, userUuid));
        assertEquals(TOO_MANY_CERTIFICATES, ex.getMessage());
    }

    @Test
    void processNewOrderWhenCertificateNotFound() {
        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().getFirst().setAmount(3);
        dto.setCertificates(Set.of("cert1"));
        String userUuid = "test-uuid";
        User testUser = ModelUtils.getUser();
        testUser.setUuid(userUuid);
        testUser.setCurrentPoints(1000);
        Address address = ModelUtils.getAddress();
        address.setUser(testUser);

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(new HashSet<>());
        when(certificateRepository.findById(anyString())).thenReturn(Optional.empty());
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(userRepository.findByUuid(userUuid)).thenReturn(testUser);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(getLocation()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffsInfo()));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(getBag()));
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(getOrder());
        when(modelMapper.map(any(Address.class), eq(OrderAddress.class))).thenReturn(getOrderAddress());
        when(modelMapper.map(any(PersonalDataDto.class), eq(UBSuser.class))).thenReturn(getUBSuser());

        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> ubsClientService.processNewOrder(dto, userUuid));
        assertEquals(CERTIFICATE_NOT_FOUND_BY_CODE + "cert1", ex.getMessage());
    }

    @Test
    void processNewOrderWithNotActivatedException() {
        OrderResponseDto dto = getOrderResponseDto();
        dto.setCertificates(Set.of("cert1"));
        dto.getBags().getFirst().setAmount(3);
        String userUuid = "test-uuid";
        User testUser = ModelUtils.getUser();
        testUser.setUuid(userUuid);
        testUser.setCurrentPoints(1000);
        Address address = ModelUtils.getAddress();
        address.setUser(testUser);
        Certificate certificate = getActiveCertificateWith10Points();
        certificate.setCertificateStatus(CertificateStatus.NEW);

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(new HashSet<>());
        when(certificateRepository.findById("cert1")).thenReturn(Optional.of(certificate));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(userRepository.findByUuid(userUuid)).thenReturn(testUser);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(getLocation()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffsInfo()));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(getBag()));
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(getOrder());
        when(modelMapper.map(any(Address.class), eq(OrderAddress.class))).thenReturn(getOrderAddress());
        when(modelMapper.map(any(PersonalDataDto.class), eq(UBSuser.class))).thenReturn(getUBSuser());

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(new HashSet<>());

        CertificateIsNotActivated ex = assertThrows(CertificateIsNotActivated.class,
            () -> ubsClientService.processNewOrder(dto, userUuid));
        assertEquals(CERTIFICATE_IS_NOT_ACTIVATED + certificate.getCode(), ex.getMessage());
    }

    @Test
    void processNewOrderWithUsedException() {
        OrderResponseDto dto = getOrderResponseDto();
        dto.setCertificates(Set.of("cert1"));
        dto.getBags().getFirst().setAmount(3);
        String userUuid = "test-uuid";
        User testUser = ModelUtils.getUser();
        testUser.setUuid(userUuid);
        testUser.setCurrentPoints(1000);
        Address address = ModelUtils.getAddress();
        address.setUser(testUser);
        Certificate certificate = getActiveCertificateWith10Points();
        certificate.setCertificateStatus(CertificateStatus.USED);

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(new HashSet<>());
        when(certificateRepository.findById("cert1")).thenReturn(Optional.of(certificate));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(userRepository.findByUuid(userUuid)).thenReturn(testUser);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(getLocation()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffsInfo()));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(getBag()));
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(getOrder());
        when(modelMapper.map(any(Address.class), eq(OrderAddress.class))).thenReturn(getOrderAddress());
        when(modelMapper.map(any(PersonalDataDto.class), eq(UBSuser.class))).thenReturn(getUBSuser());

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(new HashSet<>());

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> ubsClientService.processNewOrder(dto, userUuid));
        assertEquals(CERTIFICATE_IS_USED + certificate.getCode(), ex.getMessage());
    }

    @Test
    void processNewOrderWithExpiredException() {
        OrderResponseDto dto = getOrderResponseDto();
        dto.setCertificates(Set.of("cert1"));
        dto.getBags().getFirst().setAmount(3);
        String userUuid = "test-uuid";
        User testUser = ModelUtils.getUser();
        testUser.setUuid(userUuid);
        testUser.setCurrentPoints(1000);
        Address address = ModelUtils.getAddress();
        address.setUser(testUser);
        Certificate certificate = getActiveCertificateWith10Points();
        certificate.setCertificateStatus(CertificateStatus.ACTIVE);
        certificate.setExpirationDate(LocalDate.now().minusDays(1));

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(new HashSet<>());
        when(certificateRepository.findById("cert1")).thenReturn(Optional.of(certificate));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(userRepository.findByUuid(userUuid)).thenReturn(testUser);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(getLocation()));
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(getTariffsInfo()));
        when(bagRepository.findActiveBagById(anyInt())).thenReturn(Optional.of(getBag()));
        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), eq(CertificateStatus.ACTIVE)))
            .thenReturn(new HashSet<>());
        when(modelMapper.map(any(OrderResponseDto.class), eq(Order.class))).thenReturn(getOrder());
        when(modelMapper.map(any(Address.class), eq(OrderAddress.class))).thenReturn(getOrderAddress());
        when(modelMapper.map(any(PersonalDataDto.class), eq(UBSuser.class))).thenReturn(getUBSuser());

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> ubsClientService.processNewOrder(dto, userUuid));
        assertEquals(CERTIFICATE_EXPIRED + certificate.getCode(), ex.getMessage());
    }

    @Test
    void getTariffInfoForLocationWithInvalidLocation() {
        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(locationRepository.existsById(2L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsClientService.getTariffInfoForLocation(1L, 2L));
        assertTrue(exception.getMessage().contains(ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID + "2"));
    }

    @Test
    void cancelPaymentAttempt() throws SchedulerException, JSONException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", 0);
        jobDataMap.put("certificateCodes", new HashSet<>());

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        String response = new JSONObject().put("reason", "Removed").toString();

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(quartzScheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(quartzScheduler.deleteJob(jobKey)).thenReturn(true);
        when(wayForPayClient.getCancellationResponse(any(PaymentCancellationWayForPayRequestDto.class)))
            .thenReturn(response);

        ubsClientService.cancelPaymentAttempt(uuid, orderId);
    }

    @Test
    void cancelPaymentAttemptWhenNoPending() {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsClientService.cancelPaymentAttempt(uuid, orderId));

        assertTrue(exception.getMessage().contains(NO_PAYMENT_ATTEMPT_FOR_ORDER));
    }

    @Test
    void cancelPaymentAttemptWhenSchedulerFailsToCheck() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        doThrow(SchedulerException.class).when(quartzScheduler).checkExists(any(JobKey.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ubsClientService.cancelPaymentAttempt(uuid, orderId));

        assertEquals(QUARTZ_SCHEDULER_EXCEPTION, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenOrderNotFound() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsClientService.cancelPaymentAttempt(uuid, orderId));

        assertTrue(exception.getMessage().contains(ORDER_NOT_FOUND_BY_ID));
    }

    @Test
    void cancelPaymentAttemptWhenOrderNotBelongsToUser() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        order.setUser(getUserWithInitializedFields().setId(2L));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        when(quartzScheduler
            .checkExists(JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP)))
            .thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> ubsClientService.cancelPaymentAttempt(uuid, orderId));

        assertEquals(ORDER_DOES_NOT_BELONG_TO_USER, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenSchedulerFailsToGetJobData() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doThrow(SchedulerException.class).when(quartzScheduler).getJobDetail(jobKey);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ubsClientService.cancelPaymentAttempt(uuid, orderId));

        assertEquals(QUARTZ_SCHEDULER_EXCEPTION, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenSchedulerReturnsFalseOnCancel() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", 0);
        jobDataMap.put("certificateCodes", new HashSet<>());

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(quartzScheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ubsClientService.cancelPaymentAttempt(uuid, orderId));

        assertEquals(PAYMENT_EXPIRY_CANCEL_EXCEPTION, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenSchedulerFailsToCancelJob() throws SchedulerException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", 0);
        jobDataMap.put("certificateCodes", new HashSet<>());

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(quartzScheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        doThrow(SchedulerException.class).when(quartzScheduler).deleteJob(jobKey);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ubsClientService.cancelPaymentAttempt(uuid, orderId));

        assertEquals(QUARTZ_SCHEDULER_EXCEPTION, exception.getMessage());
    }

    @Test
    void cancelPaymentAttemptWhenWayForPayDeclines() throws SchedulerException, JSONException {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now().plusDays(10));
        Long orderId = order.getId();

        User user = getUserWithInitializedFields();
        String uuid = user.getUuid();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", 0);
        jobDataMap.put("certificateCodes", new HashSet<>());

        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        String response = new JSONObject().put("reason", "Any wrong reason").toString();

        when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(quartzScheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(quartzScheduler.deleteJob(jobKey)).thenReturn(true);
        when(wayForPayClient.getCancellationResponse(any(PaymentCancellationWayForPayRequestDto.class)))
            .thenReturn(response);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsClientService.cancelPaymentAttempt(uuid, orderId));

        assertEquals(UNABLE_TO_CANCEL_PAYMENT_INVOICE, exception.getMessage());
    }

    @Test
    void expirePaymentAttempt() {
        Order order = getOrder();
        HashSet<Certificate> certificates = new HashSet<>(List.of(
            getCertificate().setCode("7777-7777").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(100).setPoints(100),
            getCertificate().setCode("1111-1111").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(120).setPoints(100)));
        order.setCertificates(certificates);
        HashSet<String> certificateCodes = new HashSet<>(List.of("7777-7777", "1111-1111"));
        List<String> certificateCodesList = certificateCodes.stream().toList();
        Long orderId = order.getId();
        int pointsUsed = order.getPointsToUse();

        User user = getUserWithInitializedFields();
        int pointsBefore = user.getCurrentPoints();
        order.setUser(user);

        HashSet<Certificate> expectedCertificates = new HashSet<>();
        Certificate expectedCertificate1 = getCertificate()
            .setCode("7777-7777")
            .setOrder(null)
            .setDateOfUse(null)
            .setCertificateStatus(CertificateStatus.ACTIVE)
            .setInitialPointsValue(100)
            .setPoints(100);
        expectedCertificate1.setPoints(expectedCertificate1.getInitialPointsValue());
        expectedCertificates.add(expectedCertificate1);
        Certificate expectedCertificate2 = getCertificate()
            .setCode("1111-1111")
            .setOrder(null)
            .setDateOfUse(null)
            .setCertificateStatus(CertificateStatus.ACTIVE)
            .setInitialPointsValue(120)
            .setPoints(120);
        expectedCertificate2.setPoints(expectedCertificate2.getInitialPointsValue());
        expectedCertificates.add(expectedCertificate2);

        when(certificateRepository.findAllByCodesAndOrderId(certificateCodesList, orderId))
            .thenReturn(certificates);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ubsClientService.expirePaymentAttempt(orderId, pointsUsed, certificateCodes);

        assertEquals(pointsBefore + pointsUsed, user.getCurrentPoints());
        assertEquals(expectedCertificates, certificates);
        assertEquals("", order.getPaymentLink());
        assertNull(order.getPaymentLinkExpiry());

        verify(certificateRepository).findAllByCodesAndOrderId(certificateCodesList, orderId);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    void expirePaymentAttemptWhenNoBonusesSpecified() {
        Order order = getOrder();
        HashSet<Certificate> certificates = new HashSet<>(List.of(
            getCertificate().setCode("7777-7777").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(100).setPoints(100),
            getCertificate().setCode("1111-1111").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(120).setPoints(100)));
        order.setCertificates(certificates);
        Long orderId = order.getId();
        int orderPointsBefore = order.getPointsToUse();

        User user = getUserWithInitializedFields();
        int userPointsBefore = user.getCurrentPoints();
        order.setUser(user);

        HashSet<Certificate> expectedCertificates = new HashSet<>(List.of(
            getCertificate().setCode("7777-7777").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(100).setPoints(100),
            getCertificate().setCode("1111-1111").setCertificateStatus(CertificateStatus.USED)
                .setInitialPointsValue(120).setPoints(100)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ubsClientService.expirePaymentAttempt(orderId, 0, new HashSet<>());

        assertEquals(orderPointsBefore, order.getPointsToUse());
        assertEquals(userPointsBefore, user.getCurrentPoints());
        assertEquals(expectedCertificates, certificates);
        assertEquals("", order.getPaymentLink());
        assertNull(order.getPaymentLinkExpiry());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);

        verifyNoInteractions(certificateRepository);
    }

    @Test
    void expirePaymentAttemptWhenOrderNotExists() {
        Long orderId = 1L;
        HashSet<String> certificateCodes = new HashSet<>(List.of("7777-7777", "1111-1111"));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsClientService.expirePaymentAttempt(orderId, 0, certificateCodes));

        assertTrue(exception.getMessage().contains(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
    }

    @Test
    void formedLink() {
        Order order = getOrderCount();
        order.setPayment(List.of(getPayment()));
        order.setPaymentLink(" ");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(wayForPayClient.getCheckOutResponse(any()))
            .thenReturn("{\"invoiceUrl\":\"https://pay.example.com/invoice/TEST123\"}");

        String result = ubsService.formedLink(order, 560);

        assertEquals("https://pay.example.com/invoice/TEST123", result);
    }
}
