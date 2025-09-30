package greencity.service.ubs.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.RegionDto;
import greencity.dto.TariffInfoDto;
import greencity.dto.courier.CourierDto;
import greencity.entity.order.Courier;
import greencity.entity.order.Order;
import greencity.entity.user.Location;
import greencity.exceptions.NotFoundException;
import greencity.repository.CourierRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TariffsInfoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class CourierServiceImplTest {
    @Mock
    private CourierRepository courierRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CourierServiceImpl courierService;

    @Test
    void getInfoForCourierOrderingByCourierId_CourierNotFound() {
        Long courierId = 1L;
        when(courierRepository.existsCourierById(courierId)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> courierService.getInfoForCourierOrderingByCourierId("uuid", Optional.empty(), courierId));
    }

    @Test
    void getInfoForCourierOrderingByCourierId_WithChangeLoc() {
        Long courierId = 1L;
        String uuid = "uuid";

        when(courierRepository.existsCourierById(courierId)).thenReturn(true);
        Location location = ModelUtils.getLocation();

        when(locationRepository.findAllActiveLocationsByCourierId(courierId))
            .thenReturn(List.of(location));
        when(modelMapper.map(any(Location.class), eq(RegionDto.class)))
            .thenReturn(new RegionDto(10L, "RegionEn", "RegionUk"));
        when(tariffsInfoRepository.findTariffInfoByLocationIdAndCourierId(location.getId(), courierId))
            .thenReturn(Optional.empty());
        when(modelMapper.map(isNull(), eq(TariffInfoDto.class))).thenReturn(null);

        OrderCourierPopUpDto result = courierService.getInfoForCourierOrderingByCourierId(
            uuid, Optional.of("something"), courierId);

        assertFalse(result.getOrderIsPresent());
        assertEquals(1, result.getAllActiveLocationsDtos().size());
    }

    @Test
    void getInfoForCourierOrderingByCourierId_LastOrderExists() {
        Long courierId = 1L;
        String uuid = "uuid";

        when(courierRepository.existsCourierById(courierId)).thenReturn(true);
        when(orderRepository.getLastOrderOfUserByUUIDIfExists(uuid))
            .thenReturn(Optional.of(new Order()));

        Location location = ModelUtils.getLocation();

        when(locationRepository.findAllActiveLocationsByCourierId(courierId))
            .thenReturn(List.of(location));
        when(modelMapper.map(any(Location.class), eq(RegionDto.class)))
            .thenReturn(new RegionDto(10L, "RegionEn", "RegionUk"));
        when(tariffsInfoRepository.findTariffInfoByLocationIdAndCourierId(location.getId(), courierId))
            .thenReturn(Optional.empty());
        when(modelMapper.map(isNull(), eq(TariffInfoDto.class))).thenReturn(null);

        OrderCourierPopUpDto result = courierService.getInfoForCourierOrderingByCourierId(
            uuid, Optional.empty(), courierId);

        assertTrue(result.getOrderIsPresent());
        assertEquals(1, result.getAllActiveLocationsDtos().size());
    }

    @Test
    void getInfoForCourierOrderingByCourierId_LastOrderNotExists() {
        Long courierId = 1L;
        String uuid = "uuid";

        when(courierRepository.existsCourierById(courierId)).thenReturn(true);
        when(orderRepository.getLastOrderOfUserByUUIDIfExists(uuid))
            .thenReturn(Optional.empty());

        Location location = ModelUtils.getLocation();

        when(locationRepository.findAllActiveLocationsByCourierId(courierId))
            .thenReturn(List.of(location));
        when(modelMapper.map(any(Location.class), eq(RegionDto.class)))
            .thenReturn(new RegionDto(20L, "RegionEn2", "RegionUk2"));
        when(tariffsInfoRepository.findTariffInfoByLocationIdAndCourierId(location.getId(), courierId))
            .thenReturn(Optional.empty());
        when(modelMapper.map(isNull(), eq(TariffInfoDto.class))).thenReturn(null);

        OrderCourierPopUpDto result = courierService.getInfoForCourierOrderingByCourierId(
            uuid, Optional.empty(), courierId);

        assertFalse(result.getOrderIsPresent());
        assertEquals(1, result.getAllActiveLocationsDtos().size());
    }

    @Test
    void getAllActiveCouriers_ReturnsMappedDtos() {
        Courier courier = new Courier();
        courier.setId(1L);

        CourierDto dto = new CourierDto();
        dto.setCourierId(1L);

        when(courierRepository.getAllActiveCouriers()).thenReturn(List.of(courier));
        when(modelMapper.map(courier, CourierDto.class)).thenReturn(dto);

        List<CourierDto> result = courierService.getAllActiveCouriers();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCourierId());
    }

    @Test
    void testGetInfoForCourierOrderingByCourierId_mergeFunctionTriggered() {
        Long courierId = 1L;
        String uuid = "test-uuid";
        Location loc1 = ModelUtils.getLocation();
        Location loc2 = ModelUtils.getLocation();
        loc2.setId(2L);
        RegionDto sameRegion = ModelUtils.getRegionDto();

        when(courierRepository.existsCourierById(courierId)).thenReturn(true);
        when(locationRepository.findAllActiveLocationsByCourierId(courierId))
            .thenReturn(List.of(loc1, loc2));
        when(modelMapper.map(any(Location.class), eq(RegionDto.class)))
            .thenReturn(sameRegion);
        when(tariffsInfoRepository.findTariffInfoByLocationIdAndCourierId(anyLong(), eq(courierId)))
            .thenReturn(Optional.empty());
        when(modelMapper.map(isNull(), eq(TariffInfoDto.class)))
            .thenReturn(null);
        OrderCourierPopUpDto result =
            courierService.getInfoForCourierOrderingByCourierId(uuid, Optional.of("change"), courierId);

        assertNotNull(result);
        assertFalse(result.getOrderIsPresent());
        assertEquals(1, result.getAllActiveLocationsDtos().size());
        assertEquals(2, result.getAllActiveLocationsDtos().get(0).getLocations().size());
    }
}