package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.client.FondyClient;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.LocationsDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.address.AddressDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagOrderDto;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.dto.order.EventDto;
import greencity.dto.order.FondyOrderResponse;
import greencity.dto.order.MakeOrderAgainDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderFondyClientDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.telegram.TelegramBot;
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
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.TariffStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
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
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderBagRepository;
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
import greencity.service.locations.LocationApiService;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;
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

import javax.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static greencity.ModelUtils.KYIV_REGION_EN;
import static greencity.ModelUtils.KYIV_REGION_UA;
import static greencity.ModelUtils.TEST_BAG_FOR_USER_DTO;
import static greencity.ModelUtils.TEST_EMAIL;
import static greencity.ModelUtils.TEST_ORDER_ADDRESS_DTO_REQUEST;
import static greencity.ModelUtils.TEST_PAYMENT_LIST;
import static greencity.ModelUtils.addressDto;
import static greencity.ModelUtils.addressDtoList;
import static greencity.ModelUtils.addressDtoListWithNullPlaceId;
import static greencity.ModelUtils.addressList;
import static greencity.ModelUtils.addressWithEmptyPlaceIdDto;
import static greencity.ModelUtils.addressWithKyivRegionDto;
import static greencity.ModelUtils.bagDto;
import static greencity.ModelUtils.botList;
import static greencity.ModelUtils.createCertificateDto;
import static greencity.ModelUtils.getAddress;
import static greencity.ModelUtils.getAddressDtoResponse;
import static greencity.ModelUtils.getAddressRequestDto;
import static greencity.ModelUtils.getAddressRequestToSaveDto;
import static greencity.ModelUtils.getAddressRequestToSaveDto_WithoutDistricts;
import static greencity.ModelUtils.getAddressRequestWithEmptyPlaceIdDto;
import static greencity.ModelUtils.getAddressRequestWithEmptyPlaceIdToSaveDto;
import static greencity.ModelUtils.getAddressWithKyivRegionRequestDto;
import static greencity.ModelUtils.getAddressWithKyivRegionToSaveRequestDto;
import static greencity.ModelUtils.getBag;
import static greencity.ModelUtils.getBag1list;
import static greencity.ModelUtils.getBag4list;
import static greencity.ModelUtils.getBagForOrder;
import static greencity.ModelUtils.getBagTranslationDto;
import static greencity.ModelUtils.getCancellationDto;
import static greencity.ModelUtils.getCertificate;
import static greencity.ModelUtils.getCourier;
import static greencity.ModelUtils.getCourierDto;
import static greencity.ModelUtils.getCourierDtoList;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getGeocodingResult;
import static greencity.ModelUtils.getGeocodingResultWithKyivRegion;
import static greencity.ModelUtils.getListOfEvents;
import static greencity.ModelUtils.getLocation;
import static greencity.ModelUtils.getMaximumAmountOfAddresses;
import static greencity.ModelUtils.getOrder;
import static greencity.ModelUtils.getOrderCount;
import static greencity.ModelUtils.getOrderCountWithPaymentStatusPaid;
import static greencity.ModelUtils.getOrderDetails;
import static greencity.ModelUtils.getOrderDetailsWithoutSender;
import static greencity.ModelUtils.getOrderDoneByUser;
import static greencity.ModelUtils.getOrderFondyClientDto;
import static greencity.ModelUtils.getOrderPaymentDetailDto;
import static greencity.ModelUtils.getOrderPaymentStatusTranslation;
import static greencity.ModelUtils.getOrderResponseDto;
import static greencity.ModelUtils.getOrderStatusDto;
import static greencity.ModelUtils.getOrderStatusTranslation;
import static greencity.ModelUtils.getOrderTest;
import static greencity.ModelUtils.getOrderWithAddressesResponseDto;
import static greencity.ModelUtils.getOrderWithEvents;
import static greencity.ModelUtils.getOrderWithTariffAndLocation;
import static greencity.ModelUtils.getOrderWithoutPayment;
import static greencity.ModelUtils.getOrdersDto;
import static greencity.ModelUtils.getPayment;
import static greencity.ModelUtils.getSuccessfulFondyResponse;
import static greencity.ModelUtils.getTariffInfo;
import static greencity.ModelUtils.getTariffInfoWithLimitOfBags;
import static greencity.ModelUtils.getTariffInfoWithLimitOfBagsAndMaxLessThanCountOfBigBag;
import static greencity.ModelUtils.getTariffLocation;
import static greencity.ModelUtils.getTariffsForLocationDto;
import static greencity.ModelUtils.getTariffsInfo;
import static greencity.ModelUtils.getTelegramBotNotifyTrue;
import static greencity.ModelUtils.getTestOrderAddressDtoRequest;
import static greencity.ModelUtils.getTestOrderAddressDtoRequestWithNullPlaceId;
import static greencity.ModelUtils.getTestOrderAddressLocationDto;
import static greencity.ModelUtils.getTestUser;
import static greencity.ModelUtils.getUBSuser;
import static greencity.ModelUtils.getUBSuserWithoutSender;
import static greencity.ModelUtils.getUbsCustomersDtoUpdate;
import static greencity.ModelUtils.getUbsUsers;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserForCreate;
import static greencity.ModelUtils.getUserInfoDto;
import static greencity.ModelUtils.getUserPointsAndAllBagsDto;
import static greencity.ModelUtils.getUserProfileCreateDto;
import static greencity.ModelUtils.getUserProfileUpdateDto;
import static greencity.ModelUtils.getUserProfileUpdateDtoWithBotsIsNotifyFalse;
import static greencity.ModelUtils.getUserWithBotNotifyTrue;
import static greencity.ModelUtils.getUserWithLastLocation;
import static greencity.ModelUtils.getViberBotNotifyTrue;
import static greencity.constant.ErrorMessage.ACTUAL_ADDRESS_NOT_FOUND;
import static greencity.constant.ErrorMessage.ADDRESS_ALREADY_EXISTS;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_PERSONAL_INFO;
import static greencity.constant.ErrorMessage.CANNOT_DELETE_ADDRESS;
import static greencity.constant.ErrorMessage.CANNOT_DELETE_ALREADY_DELETED_ADDRESS;
import static greencity.constant.ErrorMessage.CANNOT_MAKE_ACTUAL_DELETED_ADDRESS;
import static greencity.constant.ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.LOCATION_IS_DEACTIVATED_FOR_TARIFF;
import static greencity.constant.ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND;
import static greencity.constant.ErrorMessage.TARIFF_OR_LOCATION_IS_DEACTIVATED;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND_BY_LOCATION_ID;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
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
    private CourierRepository courierRepository;

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
    @Mock
    private UBSManagementService ubsManagementService;
    @InjectMocks
    private UBSClientServiceImpl ubsClientService;

    @Mock
    private LocationApiService locationApiService;
    @Mock
    private OrderBagService orderBagService;
    @Mock
    private OrderBagRepository orderBagRepository;
    @Mock
    private LocationToLocationsDtoMapper locationToLocationsDtoMapper;
    @Mock
    private NotificationService notificationService;

    @Test
    void testGetAllDistricts() {

        List<LocationDto> locationDtos;
        List<DistrictDto> districtDtos;
        locationDtos = Arrays.asList(LocationDto.builder().build(), LocationDto.builder().build());
        districtDtos = Arrays.asList(DistrictDto.builder().build(), DistrictDto.builder().build());

        when(locationApiService.getAllDistrictsInCityByNames(anyString(), anyString())).thenReturn(locationDtos);
        when(modelMapper.map(any(LocationDto.class), eq(DistrictDto.class))).thenAnswer(i -> new DistrictDto());

        List<DistrictDto> results = ubsClientService.getAllDistricts("region", "city");

        assertEquals(districtDtos.size(), results.size());
        verify(locationApiService, times(1)).getAllDistrictsInCityByNames(anyString(), anyString());
        verify(modelMapper, times(locationDtos.size())).map(any(LocationDto.class), eq(DistrictDto.class));
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
        when(modelMapper.map(bags.get(0), BagTranslationDto.class)).thenReturn(bagTranslationDto);

        var userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageDataByTariffAndLocationId(tariffsInfoId, locationId);

        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags(),
            userPointsAndAllBagsDtoActual.getBags());

        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);
        verify(bagRepository).findAllActiveBagsByTariffsInfoId(tariffsInfoId);
        verify(modelMapper).map(bags.get(0), BagTranslationDto.class);
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

        var exception = assertThrows(NotFoundException.class, () -> ubsService
            .getFirstPageDataByTariffAndLocationId(tariffsInfoId, locationId));

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

        var exception = assertThrows(NotFoundException.class, () -> ubsService
            .getFirstPageDataByTariffAndLocationId(tariffsInfoId, locationId));

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

        var exception = assertThrows(NotFoundException.class, () -> ubsService
            .getFirstPageDataByTariffAndLocationId(tariffId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(tariffsInfoRepository).findById(tariffId);

        verify(locationRepository, never()).findById(anyLong());
        verify(tariffLocationRepository, never()).findTariffLocationByTariffsInfoAndLocation(any(), any());
        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
        verify(modelMapper, never()).map(any(), anyLong());
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

        when(tariffsInfoRepository.findById(tariffsInfoId)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        var exception = assertThrows(BadRequestException.class, () -> ubsService
            .getFirstPageDataByTariffAndLocationId(tariffsInfoId, locationId));

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

        var exception = assertThrows(BadRequestException.class, () -> ubsService
            .getFirstPageDataByTariffAndLocationId(tariffsInfoId, locationId));

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

        var exception = assertThrows(BadRequestException.class, () -> ubsService
            .getFirstPageDataByTariffAndLocationId(tariffsInfoId, locationId));

        assertEquals(expectedErrorMessage, exception.getMessage());
        verify(tariffsInfoRepository).findById(tariffsInfoId);
        verify(locationRepository).findById(locationId);
        verify(tariffLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);

        verify(bagRepository, never()).findAllActiveBagsByTariffsInfoId(anyLong());
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
        var userPointsAndAllBagsDtoExpected = ModelUtils.getUserPointsAndAllBagsDtoWithQuantity();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(tariffsInfoId)).thenReturn(bags);
        when(orderBagRepository.getAmountOfOrderBagsByOrderIdAndBagId(anyLong(), anyInt()))
            .thenReturn(2);

        var userPointsAndAllBagsDtoActual =
            ubsService.getFirstPageDataByOrderId(uuid, orderId);

        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().get(0).toString(),
            userPointsAndAllBagsDtoActual.getBags().get(0).toString());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().get(0).getQuantity(),
            userPointsAndAllBagsDtoActual.getBags().get(0).getQuantity());
        assertEquals(
            userPointsAndAllBagsDtoExpected.getBags().get(0).getId(),
            userPointsAndAllBagsDtoActual.getBags().get(0).getId());
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
    void testSaveToDB_AddressNotEqualsUsers() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(15);
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
        tariffsInfo.setBags(Arrays.asList(bag));
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
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
        dto.getBags().get(0).setAmount(15);
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
        tariffsInfo.setBags(Arrays.asList(bag));
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user.setId(null), user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser.setId(null)));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser.setId(null));
        when(addressRepository.findById(any()))
            .thenReturn(Optional.of(getAddress().setAddressStatus(AddressStatus.DELETED)));
        when(locationRepository.findById(any())).thenReturn(Optional.of(getLocation()));
        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

    }

    @Test
    void testSaveToDbThrowBadRequestExceptionPriceLowerThanLimit() throws IllegalAccessException {
        User user = getUserWithLastLocation();
        user.setAlternateEmail("test@mail.com");
        user.setCurrentPoints(900);

        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(1);
        Bag bag = getBagForOrder();
        Order order = getOrder();
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        TariffsInfo tariffsInfo = getTariffInfo();
        tariffsInfo.setBags(Arrays.asList(bag));
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(1)).findActiveBagById(anyInt());
    }

    @Test
    void testSaveToDbThrowBadRequestExceptionPriceGreaterThanLimit() throws IllegalAccessException {
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

        assertThrows(BadRequestException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository, times(1)).findActiveBagById(anyInt());
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

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBagsAndMaxLessThanCountOfBigBag();
        bag.setTariffsInfo(tariffsInfo);

        UBSuser ubSuser = getUBSuser();

        OrderAddress orderAddress = ubSuser.getOrderAddress();
        orderAddress.setAddressStatus(AddressStatus.NEW);

        Order order1 = getOrder();
        order1.setPayment(new ArrayList<>());
        Payment payment1 = getPayment();
        payment1.setId(1L);
        order1.getPayment().add(payment1);

        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(Arrays.asList(bag));
        order.setTariffsInfo(tariffsInfo);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
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
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();

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

        Bag bag = getBagForOrder();

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
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null));

        verify(userRepository, times(1)).findByUuid(anyString());
        verify(tariffsInfoRepository, times(1))
            .findTariffsInfoByBagIdAndLocationId(anyList(), anyLong());
        verify(bagRepository).findActiveBagById(3);

    }

    @Test
    void saveToDBFailPaidOrder() {
        User user = getUserWithLastLocation();
        user.setCurrentPoints(1000);
        OrderResponseDto dto = getOrderResponseDto();
        dto.getBags().get(0).setAmount(5);
        Order order = getOrder();
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(Arrays.asList(bag));
        order.setTariffsInfo(tariffsInfo);
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
        dto.getBags().get(0).setAmount(3);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBags();
        bag.setTariffsInfo(tariffsInfo);

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
        List<UBSuser> ubsUser = Arrays.asList(getUBSuser());
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
        List<UBSuser> ubsUser = Arrays.asList(getUBSuser());
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
        List<UBSuser> ubsUser = Arrays.asList(getUBSuser());
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
        List<UBSuser> ubsUser = Arrays.asList(getUBSuser());
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
        assertThrows(NotFoundException.class, () -> ubsService.checkCertificate("randomstring"));
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
                        .price(100.)
                        .bagAmount(1)
                        .name("name")
                        .nameEng("nameEng")
                        .build(),
                    BagOrderDto.builder()
                        .bagId(2)
                        .capacity(10)
                        .price(100.)
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
        assertEquals(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, thrown.getMessage());
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
        assertEquals(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST, thrown.getMessage());
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
    void updateUbsUserInfoInOrderThrowUBSuserNotFoundExceptionTest() {
        UbsCustomersDtoUpdate request = getUbsCustomersDtoUpdate();

        when(ubsUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UBSuserNotFoundException.class,
            () -> ubsService.updateUbsUserInfoInOrder(request, "abc"));
        verify(ubsUserRepository).findById(1L);
    }

    @Test
    void updateUbsUserInfoInOrderTest() {
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

        verify(ubsUserRepository).findById(1L);
        verify(ubsUserRepository).save(user.get());
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
    void updateProfileDataWhenAddressPlaceIdIsNull() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        User user = getUserWithBotNotifyTrue();
        TelegramBot telegramBot = getTelegramBotNotifyTrue();
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
        doReturn(new OrderWithAddressesResponseDto()).when(ubsClientService)
            .updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserProfileUpdateDto.class)).thenReturn(userProfileUpdateDto);

        ubsClientService.updateProfileData(uuid, userProfileUpdateDto);

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
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressRequestToSaveDto();
        Address addressToSave = new Address();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResult().get(0));

        when(modelMapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(modelMapper.map(any(), eq(OrderAddressDtoRequest.class))).thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
        when(modelMapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        var addressDto = addressDto();
        addressDto.setDistrict("Район");
        addressDto.setDistrictEn("District");
        when(modelMapper.map(addresses.get(0), AddressDto.class)).thenReturn(addressDto);

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(getAddressDtoResponse(), actualWithSearchAddress);
        assertEquals(createAddressRequestToSaveDto.getDistrict(),
            actualWithSearchAddress.getAddressList().get(0).getDistrict());
        assertEquals(createAddressRequestToSaveDto.getDistrictEn(),
            actualWithSearchAddress.getAddressList().get(0).getDistrictEn());

        verify(addressRepository).save(addressToSave);

        verify(userRepository, times(2)).findByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());
        verify(googleApiService, times(2)).getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt());

        verify(modelMapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(modelMapper, times(2)).map(any(), eq(OrderAddressDtoRequest.class));
        verify(modelMapper).map(any(), eq(Address.class));
        verify(modelMapper).map(addresses.get(0), AddressDto.class);
    }

    @Test
    void testSaveCurrentAddressForOrder_WithoutDistricts() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        CreateAddressRequestDto createAddressRequestDto = getAddressRequestDto();
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressRequestToSaveDto_WithoutDistricts();
        Address addressToSave = new Address();
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResult().get(0));

        when(modelMapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(modelMapper.map(any(), eq(OrderAddressDtoRequest.class))).thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
        when(modelMapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        var addressDto = addressDto();
        addressDto.setDistrict(null);
        addressDto.setDistrictEn(null);
        when(modelMapper.map(addresses.get(0), AddressDto.class)).thenReturn(addressDto);

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(createAddressRequestToSaveDto.getDistrict(),
            actualWithSearchAddress.getAddressList().get(0).getDistrict());
        assertEquals(createAddressRequestToSaveDto.getDistrictEn(),
            actualWithSearchAddress.getAddressList().get(0).getDistrictEn());

        verify(addressRepository).save(addressToSave);

        verify(userRepository, times(2)).findByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());
        verify(googleApiService, times(2)).getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt());

        verify(modelMapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(modelMapper, times(2)).map(any(), eq(OrderAddressDtoRequest.class));
        verify(modelMapper).map(any(), eq(Address.class));
        verify(modelMapper).map(addresses.get(0), AddressDto.class);
    }

    @Test
    void saveCurrentAddressForOrderWithEmptyPlaceIdTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        CreateAddressRequestDto createAddressRequestDto = getAddressRequestWithEmptyPlaceIdDto();
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressRequestWithEmptyPlaceIdToSaveDto();
        Address addressToSave = new Address();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);

        when(modelMapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(modelMapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        when(modelMapper.map(addresses.get(0), AddressDto.class)).thenReturn(addressWithEmptyPlaceIdDto());

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(getOrderWithAddressesResponseDto(), actualWithSearchAddress);
        verify(addressRepository).save(addressToSave);

        verify(userRepository, times(2)).findByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());

        verify(modelMapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(modelMapper).map(any(), eq(Address.class));
        verify(modelMapper).map(addresses.get(0), AddressDto.class);
    }

    @Test
    void saveCurrentAddressForOrderForAddressesBelongToKyivEnTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        CreateAddressRequestDto createAddressRequestDto = getAddressWithKyivRegionRequestDto();
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressWithKyivRegionToSaveRequestDto();
        Address addressToSave = new Address();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResultWithKyivRegion().get(0));

        when(modelMapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(modelMapper.map(any(), eq(OrderAddressDtoRequest.class))).thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
        when(modelMapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        when(modelMapper.map(addresses.get(0), AddressDto.class)).thenReturn(addressWithKyivRegionDto());

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(KYIV_REGION_EN, actualWithSearchAddress.getAddressList().get(0).getRegionEn());

        verify(userRepository, times(2)).findByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());
        verify(googleApiService, times(2)).getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt());

        verify(modelMapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(modelMapper, times(2)).map(any(), eq(OrderAddressDtoRequest.class));
        verify(modelMapper).map(any(), eq(Address.class));
        verify(modelMapper).map(addresses.get(0), AddressDto.class);

        verify(addressRepository).save(addressToSave);
    }

    @Test
    void saveCurrentAddressForOrderForAddressesBelongToKyivUaTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.get(0).setActual(false);
        addresses.get(0).setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        CreateAddressRequestDto createAddressRequestDto = getAddressWithKyivRegionRequestDto();
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressWithKyivRegionToSaveRequestDto();
        Address addressToSave = new Address();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(googleApiService.getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt()))
            .thenReturn(getGeocodingResultWithKyivRegion().get(1));

        when(modelMapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(modelMapper.map(any(), eq(OrderAddressDtoRequest.class))).thenReturn(TEST_ORDER_ADDRESS_DTO_REQUEST);
        when(modelMapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        when(modelMapper.map(addresses.get(0), AddressDto.class)).thenReturn(addressWithKyivRegionDto());

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(KYIV_REGION_UA, actualWithSearchAddress.getAddressList().get(0).getRegion());

        verify(userRepository, times(2)).findByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());
        verify(googleApiService, times(2)).getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt());

        verify(modelMapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(modelMapper, times(2)).map(any(), eq(OrderAddressDtoRequest.class));
        verify(modelMapper).map(any(), eq(Address.class));
        verify(modelMapper).map(addresses.get(0), AddressDto.class);

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

        when(modelMapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(modelMapper.map(any(), eq(OrderAddressDtoRequest.class))).thenReturn(dtoRequest);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));

        assertEquals(ADDRESS_ALREADY_EXISTS, exception.getMessage());

        verify(userRepository).findByUuid(user.getUuid());
        verify(addressRepository).findAllNonDeletedAddressesByUserId(user.getId());
        verify(googleApiService, times(0)).getResultFromGeoCode(eq(dtoRequest.getPlaceId()), anyInt());

        verify(modelMapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(modelMapper).map(any(), eq(OrderAddressDtoRequest.class));
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
        var addressDto = addressDto();
        addressDto.setDistrict("Район");
        addressDto.setDistrictEn("District");
        when(modelMapper.map(addresses.get(0),
            AddressDto.class))
                .thenReturn(addressDto);

        OrderWithAddressesResponseDto actualWithSearchAddress =
            ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);

        Assertions.assertNotNull(updateAddressRequestDto.getSearchAddress());
        Assertions.assertNull(dtoRequest.getSearchAddress());
        assertEquals(getAddressDtoResponse(), actualWithSearchAddress);
        assertEquals(updateAddressRequestDto.getDistrict(),
            actualWithSearchAddress.getAddressList().get(0).getDistrict());
        assertEquals(updateAddressRequestDto.getDistrictEn(),
            actualWithSearchAddress.getAddressList().get(0).getDistrictEn());

        verify(googleApiService, times(2)).getResultFromGeoCode(eq(updateAddressRequestDto.getPlaceId()),
            anyInt());

        updateAddressRequestDto.setSearchAddress(null);
        OrderWithAddressesResponseDto actualWithoutSearchAddress =
            ubsService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);
        assertEquals(getAddressDtoResponse(), actualWithoutSearchAddress);
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
        assertEquals(OrderWithAddressesResponseDto.builder().addressList(Collections.emptyList()).build(),
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
    void testUpdateCurrentAddressForOrderWhenPlaceIdIsNull() {
        UBSClientService ubsClientService = spy(ubsService);

        long addressId = 1L;
        long userId = 2L;
        String oldComment = "comment";
        String newComment = "newComment";

        User user = getUserForCreate();
        user.setId(userId);
        String uuid = user.getUuid();
        Address address = getAddress();
        address.setId(addressId);
        address.setAddressComment(oldComment);
        address.setUser(user);
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(addressId);
        dtoRequest.setAddressComment(newComment);
        dtoRequest.setPlaceId(null);

        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        doReturn(new OrderWithAddressesResponseDto()).when(ubsClientService).findAllAddressesForCurrentOrder(uuid);
        when(modelMapper.map(dtoRequest, Address.class)).thenReturn(address.setAddressComment(newComment));

        ubsClientService.updateCurrentAddressForOrder(dtoRequest, uuid);

        assertEquals(address.getAddressComment(), newComment);

        verify(userRepository).findByUuid(uuid);
        verify(addressRepository).findById(addressId);
        verify(ubsClientService).findAllAddressesForCurrentOrder(uuid);
        verify(modelMapper).map(dtoRequest, Address.class);
        verify(addressRepository).save(address);
    }

    @Test
    void testDeleteCurrentAddressForOrderWhenAddressIsActual() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        Long firstAddressId = 1L;
        Long secondAddressId = 2L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setActual(true);
        Address secondAddress = getAddress();
        secondAddress.setId(secondAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findAnyByUserIdAndAddressStatusNotDeleted(user.getId()))
            .thenReturn(Optional.of(secondAddress));
        doReturn(new OrderWithAddressesResponseDto()).when(ubsClientService).findAllAddressesForCurrentOrder(uuid);

        ubsClientService.deleteCurrentAddressForOrder(firstAddressId, uuid);

        Assertions.assertFalse(firstAddress.getActual());
        assertEquals(AddressStatus.DELETED, firstAddress.getAddressStatus());
        Assertions.assertTrue(secondAddress.getActual());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findAnyByUserIdAndAddressStatusNotDeleted(user.getId());
        verify(ubsClientService).findAllAddressesForCurrentOrder(uuid);
    }

    @Test
    void testDeleteCurrentAddressForOrderWhenAddressIsNotActual() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        doReturn(new OrderWithAddressesResponseDto()).when(ubsClientService).findAllAddressesForCurrentOrder(uuid);

        ubsClientService.deleteCurrentAddressForOrder(firstAddressId, uuid);

        assertEquals(AddressStatus.DELETED, firstAddress.getAddressStatus());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(ubsClientService).findAllAddressesForCurrentOrder(uuid);
    }

    @Test
    void testDeleteCurrentAddressForOrderWithUnexistingAddress() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        Long addressId = 1L;

        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsClientService.deleteCurrentAddressForOrder(addressId, "qwe"));

        assertEquals(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId, exception.getMessage());

        verify(addressRepository).findById(addressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(ubsClientService, times(0)).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void testDeleteCurrentAddressForOrderForWrongUser() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = "qwe";

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> ubsClientService.deleteCurrentAddressForOrder(firstAddressId, uuid));

        assertEquals(CANNOT_DELETE_ADDRESS, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(ubsClientService, times(0)).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void testDeleteCurrentAddressForOrderWhenAddressAlreadyDeleted() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setAddressStatus(AddressStatus.DELETED);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsClientService.deleteCurrentAddressForOrder(firstAddressId, uuid));

        assertEquals(CANNOT_DELETE_ALREADY_DELETED_ADDRESS, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(ubsClientService, times(0)).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void testDeleteCurrentAddressForOrderWhenItIsLastAddress() {
        UBSClientServiceImpl ubsClientService = spy(ubsService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setActual(true);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findAnyByUserIdAndAddressStatusNotDeleted(user.getId())).thenReturn(Optional.empty());
        doReturn(new OrderWithAddressesResponseDto()).when(ubsClientService).findAllAddressesForCurrentOrder(uuid);

        ubsClientService.deleteCurrentAddressForOrder(firstAddressId, uuid);

        Assertions.assertFalse(firstAddress.getActual());
        assertEquals(AddressStatus.DELETED, firstAddress.getAddressStatus());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findAnyByUserIdAndAddressStatusNotDeleted(user.getId());
        verify(ubsClientService).findAllAddressesForCurrentOrder(uuid);
    }

    @Test
    void testMakeAddressActual() {
        Long firstAddressId = 1L;
        Long secondAddressId = 2L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        Address secondAddress = getAddress();
        secondAddress.setId(secondAddressId);
        secondAddress.setActual(true);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findByUserIdAndActualTrue(user.getId())).thenReturn(Optional.of(secondAddress));

        ubsService.makeAddressActual(firstAddressId, uuid);

        Assertions.assertTrue(firstAddress.getActual());
        assertFalse(secondAddress.getActual());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findByUserIdAndActualTrue(user.getId());
        verify(modelMapper).map(firstAddress, AddressDto.class);
    }

    @Test
    void testMakeAddressActualWhereUserNotHaveActualAddress() {
        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findByUserIdAndActualTrue(user.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.makeAddressActual(firstAddressId, uuid));

        assertEquals(ACTUAL_ADDRESS_NOT_FOUND, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findByUserIdAndActualTrue(user.getId());
        verify(modelMapper, times(0)).map(any(), any());
    }

    @Test
    void testMakeAddressActualWhenAddressIsAlreadyActual() {
        Long firstAddressId = 1L;
        User user = getUser();
        String uuid = user.getUuid();
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setUser(user);
        firstAddress.setActual(true);

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        ubsService.makeAddressActual(firstAddressId, uuid);

        Assertions.assertTrue(firstAddress.getActual());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndActualTrue(anyLong());
        verify(modelMapper).map(firstAddress, AddressDto.class);
    }

    @Test
    void testMakeAddressActualWhenAddressNotFound() {
        Long firstAddressId = 1L;
        User user = getUser();
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsService.makeAddressActual(firstAddressId, uuid));

        assertEquals(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + firstAddressId, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndActualTrue(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }

    @Test
    void testMakeAddressActualWhenAddressNotBelongsToUser() {
        Long firstAddressId = 1L;
        Long userId = 2L;
        User user = getUser();
        user.setId(userId);
        String uuid = user.getUuid();
        Address firstAddress = getAddress();
        User addressOwner = getUser();
        addressOwner.setUuid("randomUuid");
        firstAddress.setId(firstAddressId);
        firstAddress.setUser(addressOwner);

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> ubsService.makeAddressActual(firstAddressId, uuid));

        assertEquals(CANNOT_ACCESS_PERSONAL_INFO, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndActualTrue(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
    }

    @Test
    void testMakeAddressActualWhenAddressIdDeleted() {
        Long firstAddressId = 1L;
        User user = getUser();
        String uuid = user.getUuid();
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setUser(user);
        firstAddress.setAddressStatus(AddressStatus.DELETED);
        user.setAddresses(List.of(firstAddress));

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ubsService.makeAddressActual(firstAddressId, uuid));

        assertEquals(CANNOT_MAKE_ACTUAL_DELETED_ADDRESS, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndActualTrue(anyLong());
        verify(modelMapper, times(0)).map(any(), any());
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
        Certificate certificate = getCertificate();
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
    void testGelAllEventsFromOrderByOrderId() {
        List<Event> orderEvents = getListOfEvents();
        when(orderRepository.findById(1L)).thenReturn(getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(orderEvents);
        List<EventDto> eventDTOS = orderEvents.stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(Collectors.toList());
        assertEquals(eventDTOS, ubsService.getAllEventsForOrder(1L, anyString(), "ua"));
    }

    @Test
    void testGelAllEventsFromOrderByOrderIdWithEng() {
        List<Event> orderEvents = getListOfEvents();
        when(orderRepository.findById(1L)).thenReturn(getOrderWithEvents());
        when(eventRepository.findAllEventsByOrderId(1L)).thenReturn(orderEvents);
        List<EventDto> eventDTOS = orderEvents.stream()
            .peek(event -> {
                event.setEventName(event.getEventNameEng());
                event.setAuthorName(event.getAuthorNameEng());
            })
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(toList());
        assertEquals(eventDTOS, ubsService.getAllEventsForOrder(1L, anyString(), "en"));
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

        assertThrows(NotFoundException.class, () -> {
            ubsService.deleteOrder("UUID", 1L);
        });
    }

    @Test
    void processOrderFondyClientCertificeteNotFoundExeption() throws Exception {
        Order order = getOrderCount();
        Certificate certificate = getCertificate();
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
        OrderFondyClientDto dto = getOrderFondyClientDto();
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
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        when(orderBagService.getActualBagsAmountForOrder(Arrays.asList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(user));
        when(certificateRepository.findAllByCodeAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE)).thenReturn(Collections.emptySet());
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(certificateDto);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        assertThrows(NotFoundException.class, () -> ubsService.processOrderFondyClient(dto, "uuid"));
        verify(orderRepository).findById(1L);
        verify(userRepository).findUserByUuid("uuid");
        verify(certificateRepository).findAllByCodeAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE);
        verify(modelMapper).map(certificate, CertificateDto.class);
        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
    }

    @Test
    void processOrderFondyClientCertificeteNotFoundExeption2() throws Exception {
        Order order = getOrderCount();
        Certificate certificate = getCertificate();
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

        OrderFondyClientDto dto = getOrderFondyClientDto();
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
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findUserByUuid("uuid")).thenReturn(Optional.of(user));
        when(certificateRepository.findAllByCodeAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE)).thenReturn(Set.of(certificate));
        when(modelMapper.map(certificate, CertificateDto.class)).thenReturn(certificateDto);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderBagService.getActualBagsAmountForOrder(Arrays.asList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());

        assertThrows(NotFoundException.class, () -> ubsService.processOrderFondyClient(dto, "uuid"));

        verify(orderRepository).findById(1L);
        verify(userRepository).findUserByUuid("uuid");
        verify(certificateRepository).findAllByCodeAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
            CertificateStatus.ACTIVE);
        verify(modelMapper).map(certificate, CertificateDto.class);
        verify(modelMapper).map(any(OrderBag.class), eq(BagForUserDto.class));
    }

    @Test
    void processOrderFondyClientFailPaidOrder() {
        Order order = getOrderCountWithPaymentStatusPaid();
        OrderFondyClientDto dto = getOrderFondyClientDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
        assertThrows(BadRequestException.class, () -> ubsService.processOrderFondyClient(dto, "uuid"));
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

        Bag bag = getBagForOrder();
        bag.setTariffsInfo(getTariffInfoWithLimitOfBags());

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
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));
        assertThrows(BadRequestException.class, () -> {
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

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBags();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMin(50000L);
        bag.setTariffsInfo(tariffsInfo);

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
            .thenReturn(Optional.of(tariffsInfo));

        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class, () -> {
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

        Bag bag = getBagForOrder();
        TariffsInfo tariffsInfo = getTariffInfoWithLimitOfBags();
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMax(500L);
        bag.setTariffsInfo(tariffsInfo);

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
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findActiveBagById(3)).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class, () -> {
            ubsService.saveFullOrderToDB(dto, "35467585763t4sfgchjfuyetf", null);
        });
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
        bag.setFullPrice(1200_00L);
        order.setAmountOfBagsOrdered(Map.of(1, 10));
        bags.add(bag);
        order.setUser(user);
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(ordersForUserRepository.getAllByUserUuidAndId(user.getUuid(), order.getId()))
            .thenReturn(order);
        when(orderBagService.getActualBagsAmountForOrder(Arrays.asList(ModelUtils.getOrderBag())))
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
        bag.setFullPrice(1200_00L);
        order.setAmountOfBagsOrdered(Map.of(1, 10));
        bags.add(bag);
        order.setUser(user);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));

        when(ordersForUserRepository.getAllByUserUuid(pageable, user.getUuid()))
            .thenReturn(page);
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        when(orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue()))
                .thenReturn(Optional.of(orderStatusTranslation));
        when(orderPaymentStatusTranslationRepository.getById(
            (long) order.getOrderPaymentStatus().getStatusValue()))
                .thenReturn(orderPaymentStatusTranslation);
        when(orderBagService.getActualBagsAmountForOrder(Arrays.asList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());

        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());

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
        OrdersDataForUserDto ordersDataForUserDto = getOrderStatusDto();
        Order order = getOrderTest();
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        User user = getTestUser();
        Bag bag = bagDto();

        List<Bag> bags = new ArrayList<>();
        List<Order> orderList = new ArrayList<>();

        BagForUserDto bagForUserDto = ordersDataForUserDto.getBags().get(0);
        bag.setCapacity(120);
        bag.setFullPrice(1200_00L);
        order.setExportedQuantity(Map.of(1, 10));
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
        when(orderBagService.getActualBagsAmountForOrder(Arrays.asList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        when(modelMapper.map(any(OrderBag.class), eq(BagForUserDto.class))).thenReturn(TEST_BAG_FOR_USER_DTO);
        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());

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
        OrdersDataForUserDto ordersDataForUserDto = getOrderStatusDto();
        Order order = getOrderTest();
        User user = getTestUser();
        Bag bag = bagDto();

        List<Bag> bags = new ArrayList<>();
        List<Order> orderList = new ArrayList<>();

        BagForUserDto bagForUserDto = ordersDataForUserDto.getBags().get(0);
        bag.setCapacity(120);
        bag.setFullPrice(1200_00L);
        order.setConfirmedQuantity(Map.of(1, 10));
        bags.add(bag);
        order.setUser(user);
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
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
        when(orderBagService.getActualBagsAmountForOrder(Arrays.asList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);

        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());

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
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);

        TariffsInfo tariffsInfo = getTariffsInfo();
        tariffsInfo.setBags(Arrays.asList(getBag()));
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
        when(orderBagService.getActualBagsAmountForOrder(Arrays.asList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());
        PageableDto<OrdersDataForUserDto> dto = ubsService.getOrdersForUser(user.getUuid(), pageable, null);
        assertEquals(dto.getTotalElements(), orderList.size());
        assertEquals(dto.getPage().get(0).getId(), order.getId());
    }

    @Test
    void getTariffInfoForLocationTest() {
        var tariff = getTariffInfo();
        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariff));
        OrderCourierPopUpDto dto = ubsService.getTariffInfoForLocation(1L, 1L);
        assertTrue(dto.getOrderIsPresent());
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
        var exception = assertThrows(NotFoundException.class,
            () -> ubsService.getTariffInfoForLocation(1L, 1L));

        assertEquals(expectedErrorMessage, exception.getMessage());
        verify(courierRepository).existsCourierById(1L);
    }

    @Test
    void getInfoForCourierOrderingByCourierIdTest() {
        var tariff = getTariffInfo();

        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(orderRepository.getLastOrderOfUserByUUIDIfExists(anyString()))
            .thenReturn(Optional.of(getOrder()));
        when(tariffsInfoRepository.findTariffsInfoByOrdersId(anyLong())).thenReturn(tariff);

        OrderCourierPopUpDto dto = ubsService.getInfoForCourierOrderingByCourierId("35467585763t4sfgchjfuyetf",
            Optional.empty(), 1L);
        assertTrue(dto.getOrderIsPresent());

        verify(courierRepository).existsCourierById(1L);
        verify(modelMapper).map(tariff, TariffsForLocationDto.class);
        verify(orderRepository).getLastOrderOfUserByUUIDIfExists(anyString());
        verify(tariffsInfoRepository).findTariffsInfoByOrdersId(anyLong());
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
        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        when(orderRepository.getLastOrderOfUserByUUIDIfExists(anyString()))
            .thenReturn(Optional.empty());

        OrderCourierPopUpDto dto = ubsService.getInfoForCourierOrderingByCourierId("35467585763t4sfgchjfuyetf",
            Optional.empty(), 1L);
        assertFalse(dto.getOrderIsPresent());

        verify(courierRepository).existsCourierById(1L);
        verify(orderRepository).getLastOrderOfUserByUUIDIfExists(anyString());
    }

    @Test
    void getInfoForCourierOrderingByCourierIdWhenChangeLocIsPresentTest() {
        when(courierRepository.existsCourierById(1L)).thenReturn(true);
        var dto = ubsService.getInfoForCourierOrderingByCourierId(
            "35467585763t4sfgchjfuyetf", Optional.of("w"), 1L);
        assertEquals(0, dto.getAllActiveLocationsDtos().size());
        verify(courierRepository).existsCourierById(1L);
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
        dto.getBags().get(0).setAmount(15);
        Order order = getOrder();
        user.setOrders(new ArrayList<>());
        user.getOrders().add(order);
        user.setChangeOfPointsList(new ArrayList<>());
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        Bag bag = getBagForOrder();

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
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(Arrays.asList(bag));
        order.setTariffsInfo(tariffsInfo);
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(anyList(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(bagRepository.findActiveBagById(any())).thenReturn(Optional.of(bag));
        when(ubsUserRepository.findById(1L)).thenReturn(Optional.of(ubSuser));
        when(modelMapper.map(dto, Order.class)).thenReturn(order);
        when(modelMapper.map(dto.getPersonalData(), UBSuser.class)).thenReturn(ubSuser);

        assertThrows(NotFoundException.class, () -> {
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

        Bag bag = getBagForOrder();

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
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(Arrays.asList(bag));
        order.setTariffsInfo(tariffsInfo);
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
        dto.getBags().get(0).setAmount(15);
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
        tariffsInfo.setBags(Arrays.asList(bag));
        order.setTariffsInfo(tariffsInfo);

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
        assertThrows(EntityNotFoundException.class, () -> ubsService.getTariffForOrder(1L));
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
        bag.setFullPrice(1200_00L);
        bags.add(bag);
        order.setUser(user);
        order.updateWithNewOrderBags(Arrays.asList(ModelUtils.getOrderBag()));
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        orderList.add(order);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("order_date").descending());
        Page<Order> page = new PageImpl<>(orderList, pageable, 1);
        TariffsInfo tariffsInfo = getTariffsInfo();
        bag.setTariffsInfo(tariffsInfo);
        tariffsInfo.setBags(Arrays.asList(bag));
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
        when(orderBagService.getActualBagsAmountForOrder(Arrays.asList(ModelUtils.getOrderBag())))
            .thenReturn(ModelUtils.getAmount());

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
    void getEmployeeLoginPositionNamesTest() {
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.ofNullable(getEmployee()));
        when(userRemoteClient.getEmployeeLoginPositionNames(TEST_EMAIL)).thenReturn(List.of("Admin"));

        List<String> actual = ubsService.getEmployeeLoginPositionNames(TEST_EMAIL);
        assertEquals(List.of("Admin"), actual);

        verify(employeeRepository).findByEmail(TEST_EMAIL);
        verify(userRemoteClient).getEmployeeLoginPositionNames(TEST_EMAIL);
    }

    @Test
    void getEmployeeLoginPositionNamesThrowsNotFoundExceptionTest() {
        when(employeeRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsService.getEmployeeLoginPositionNames(TEST_EMAIL));
        verify(employeeRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void checkIfTariffExistsByIdTest() {
        Long tariffId = 1L;
        when(tariffsInfoRepository.existsById(tariffId)).thenReturn(true);

        Assertions.assertTrue(ubsClientService.checkIfTariffExistsById(tariffId));

        verify(tariffsInfoRepository).existsById(tariffId);
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
        Long tariffId = 2L;
        when(tariffsInfoRepository.findTariffIdByLocationId(locationId)).thenReturn(Optional.of(tariffId));

        Long actualTariffId = ubsClientService.getTariffIdByLocationId(locationId);

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
    void getAllLocationsByCourierId_ShouldReturnLocations_WhenLocationsExist() {
        Long id = 1L;
        Location location = new Location();
        LocationsDto locationsDto = new LocationsDto();
        locationsDto.setId(id);
        when(locationRepository.findAllActiveLocationsByCourierId(id)).thenReturn(Arrays.asList(location));
        when(locationToLocationsDtoMapper.convert(location)).thenReturn(locationsDto);
        when(tariffsInfoRepository.findTariffIdByLocationId(id)).thenReturn(Optional.of(id));

        List<LocationsDto> result = ubsClientService.getAllLocationsByCourierId(id);

        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId());
        assertEquals(id, result.get(0).getTariffsId());
    }

    @Test
    void getAllLocationsByCourierId_ShouldThrowNotFoundException_WhenTariffNotFound() {
        Long id = 1L;
        Location location = new Location();
        LocationsDto locationsDto = new LocationsDto();
        locationsDto.setId(id);
        when(locationRepository.findAllActiveLocationsByCourierId(id)).thenReturn(Arrays.asList(location));
        when(locationToLocationsDtoMapper.convert(location)).thenReturn(locationsDto);
        when(tariffsInfoRepository.findTariffIdByLocationId(id)).thenReturn(Optional.empty());

        NotFoundException exception =
            assertThrows(NotFoundException.class, () -> ubsClientService.getAllLocationsByCourierId(id));

        assertEquals(String.format(TARIFF_NOT_FOUND_BY_LOCATION_ID, id), exception.getMessage());
    }

    @Test
    void getAllLocationsByCourierId_ShouldReturnEmptyList_WhenNoLocationsExist() {
        Long id = 1L;
        when(locationRepository.findAllActiveLocationsByCourierId(id)).thenReturn(Arrays.asList());

        List<LocationsDto> result = ubsClientService.getAllLocationsByCourierId(id);

        assertTrue(result.isEmpty());
    }
}
