package greencity.service.ubs.order;

import static greencity.constant.ErrorMessage.LOCATION_IS_DEACTIVATED_FOR_TARIFF;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_OR_LOCATION_IS_DEACTIVATED;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.LocationStatus;
import greencity.enums.TariffStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.BagRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class OrderCheckoutServiceImplTest {

    @InjectMocks
    private OrderCheckoutServiceImpl orderCheckoutService;

    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private TariffLocationRepository tariffLocationRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private BagRepository bagRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderBagRepository orderBagRepository;
    @Mock
    private UBSUserRepository ubsUserRepository;
    @Mock
    private ModelMapper modelMapper;

    private TariffsInfo tariffsInfo;
    private Location location;
    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        tariffsInfo = ModelUtils.getTariffsInfo();
        tariffsInfo.setTariffStatus(TariffStatus.ACTIVE);
        location = ModelUtils.getLocation();
        user = ModelUtils.getUser();
        order = ModelUtils.getOrder();
        order.setId(200L);
        order.setUser(user);
        order.setTariffsInfo(tariffsInfo);
        UBSuser ubsUser = ModelUtils.getUBSuser();
        OrderAddress orderAddress = ModelUtils.getOrderAddress();
        orderAddress.setLocation(location);
        ubsUser.setOrderAddress(orderAddress);
        order.setUbsUser(ubsUser);
    }

    @Test
    void getFirstPageDataByTariffAndLocationId_success() {
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(TariffLocation.builder()
                .id(1L)
                .location(location)
                .tariffsInfo(tariffsInfo)
                .locationStatus(location.getLocationStatus())
                .build()));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(List.of(ModelUtils.getBag()));

        when(modelMapper.map(any(Bag.class), eq(BagTranslationDto.class)))
            .thenReturn(ModelUtils.getBagTranslationDto());

        UserPointsAndAllBagsDto result =
            orderCheckoutService.getFirstPageDataByTariffAndLocationId(1L, 10L);

        assertThat(result.getBags()).hasSize(1);
    }

    @Test
    void getFirstPageDataByTariffAndLocationId_tariffNotFound() {
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderCheckoutService.getFirstPageDataByTariffAndLocationId(1L, 10L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getFirstPageDataByOrderId_success() {
        when(userRepository.findUserByUuid("uuid-123")).thenReturn(Optional.of(user));
        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(TariffLocation.builder()
                .id(1L)
                .location(location)
                .tariffsInfo(tariffsInfo)
                .locationStatus(location.getLocationStatus())
                .build()));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(List.of(ModelUtils.getBag()));
        when(orderBagRepository.getAmountOfOrderBagsByOrderIdAndBagId(anyLong(), anyInt()))
            .thenReturn(Optional.of(2));

        UserPointsAndAllBagsDto result =
            orderCheckoutService.getFirstPageDataByOrderId("uuid-123", 200L);

        assertThat(result.getBags()).hasSize(1);
    }

    @Test
    void getFirstPageDataByOrderId_wrongUser() {
        User otherUser = new User();
        otherUser.setId(999L);
        order.setUser(otherUser);

        when(userRepository.findUserByUuid("uuid-123")).thenReturn(Optional.of(user));
        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderCheckoutService.getFirstPageDataByOrderId("uuid-123", 200L))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getSecondPageData_success() {
        user.setAlternateEmail("alt@mail.com");
        when(userRepository.findByUuid("uuid-123")).thenReturn(user);

        UBSuser ubsUser = UBSuser.builder().id(321L).build();
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(List.of(ubsUser));

        PersonalDataDto dto = new PersonalDataDto();
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(dto);

        PersonalDataDto result = orderCheckoutService.getSecondPageData("uuid-123");

        assertThat(result.getUbsUserId()).isEqualTo(321L);
        assertThat(result.getEmail()).isEqualTo("alt@mail.com");
    }

    @Test
    void getSecondPageData_noUbsUser() {
        when(userRepository.findByUuid("uuid-123")).thenReturn(user);
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(Collections.emptyList());

        PersonalDataDto dto = new PersonalDataDto();
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(dto);

        PersonalDataDto result = orderCheckoutService.getSecondPageData("uuid-123");

        assertThat(result.getUbsUserId()).isNull();
    }

    @Test
    void getFirstPageDataByOrderId_userNotFound() {
        when(userRepository.findUserByUuid("uuid-123")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderCheckoutService.getFirstPageDataByOrderId("uuid-123", 200L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST);
    }

    @Test
    void getFirstPageDataByOrderId_orderNotFound() {
        when(userRepository.findUserByUuid("uuid-123")).thenReturn(Optional.of(user));
        when(orderRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderCheckoutService.getFirstPageDataByOrderId("uuid-123", 200L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + 200L);
    }

    @Test
    void getFirstPageDataByTariffAndLocationId_tariffOrLocationDeactivated() {
        tariffsInfo.setTariffStatus(TariffStatus.DEACTIVATED);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));

        assertThatThrownBy(() -> orderCheckoutService.getFirstPageDataByTariffAndLocationId(1L, 10L))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining(TARIFF_OR_LOCATION_IS_DEACTIVATED);
    }

    @Test
    void getFirstPageDataByTariffAndLocationId_locationDeactivatedForTariff() {
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(TariffLocation.builder()
                .id(1L)
                .location(location)
                .tariffsInfo(tariffsInfo)
                .locationStatus(LocationStatus.ACTIVE)
                .build()));

        location.setLocationStatus(LocationStatus.ACTIVE);
        tariffsInfo.setTariffStatus(TariffStatus.ACTIVE);
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(TariffLocation.builder()
                .id(1L)
                .location(location)
                .tariffsInfo(tariffsInfo)
                .locationStatus(LocationStatus.DEACTIVATED)
                .build()));

        assertThatThrownBy(() ->
            orderCheckoutService.getFirstPageDataByTariffAndLocationId(1L, 10L))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining(LOCATION_IS_DEACTIVATED_FOR_TARIFF + 1L);
    }

    @Test
    void getSecondPageData_withoutAlternateEmail() {
        user.setAlternateEmail(null);
        when(userRepository.findByUuid("uuid-123")).thenReturn(user);

        UBSuser ubsUser = UBSuser.builder().id(123L).build();
        when(ubsUserRepository.findUBSuserByUser(user)).thenReturn(List.of(ubsUser));

        PersonalDataDto dto = new PersonalDataDto();
        when(modelMapper.map(user, PersonalDataDto.class)).thenReturn(dto);

        PersonalDataDto result = orderCheckoutService.getSecondPageData("uuid-123");

        assertThat(result.getEmail()).isNull();
    }

    @Test
    void isTariffAvailableForCurrentLocation_success() {
        when(tariffLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(TariffLocation.builder()
                .id(1L)
                .tariffsInfo(tariffsInfo)
                .location(location)
                .locationStatus(LocationStatus.ACTIVE)
                .build()));

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(bagRepository.findAllActiveBagsByTariffsInfoId(1L)).thenReturn(List.of(ModelUtils.getBag()));
        when(modelMapper.map(any(Bag.class), eq(BagTranslationDto.class))).thenReturn(ModelUtils.getBagTranslationDto());

        UserPointsAndAllBagsDto result = orderCheckoutService.getFirstPageDataByTariffAndLocationId(1L, 10L);

        assertThat(result.getBags()).isNotEmpty();
    }

    @Test
    void getFirstPageDataByTariffAndLocationId_locationStatusDeactivated() {
        tariffsInfo.setTariffStatus(TariffStatus.ACTIVE);
        location.setLocationStatus(LocationStatus.DEACTIVATED);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));

        assertThatThrownBy(() -> orderCheckoutService.getFirstPageDataByTariffAndLocationId(1L, 10L))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining(TARIFF_OR_LOCATION_IS_DEACTIVATED);
    }
}