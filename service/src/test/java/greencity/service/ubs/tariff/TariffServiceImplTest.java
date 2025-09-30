package greencity.service.ubs.tariff;

import static greencity.ModelUtils.getTariffInfoByLocationDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import greencity.entity.order.TariffsInfo;
import greencity.exceptions.NotFoundException;
import greencity.repository.CourierRepository;
import greencity.repository.LocationRepository;
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
class TariffServiceImplTest {
    @Mock
    private CourierRepository courierRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TariffServiceImpl tariffService;

    @Test
    void getTariffInfoForLocation() {
        Long courierId = 1L;
        Long locationId = 2L;

        TariffInfoByLocationDto tariffInfoByLocationDto = getTariffInfoByLocationDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(courierRepository.existsCourierById(courierId)).thenReturn(true);
        when(locationRepository.existsById(locationId)).thenReturn(true);
        when(tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(courierId, locationId))
            .thenReturn(Optional.ofNullable(tariffsInfo));
        when(modelMapper.map(tariffsInfo, TariffsForLocationDto.class)).thenReturn(tariffInfoByLocationDto.getTariffsForLocationDto());

        TariffInfoByLocationDto result = tariffService.getTariffInfoForLocation(courierId, locationId);

        assertEquals(tariffInfoByLocationDto, result);
    }

    @Test
    void getTariffInfoForLocation_CourierNotFound() {
        Long courierId = 1L;
        Long locationId = 2L;

        when(courierRepository.existsCourierById(courierId)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> tariffService.getTariffInfoForLocation(courierId, locationId));
    }

    @Test
    void getTariffInfoForLocation_LocationNotFound() {
        Long courierId = 1L;
        Long locationId = 2L;

        when(courierRepository.existsCourierById(courierId)).thenReturn(true);
        when(locationRepository.existsById(locationId)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> tariffService.getTariffInfoForLocation(courierId, locationId));
    }

    @Test
    void getTariffInfoForLocation_TariffNotFound() {
        Long courierId = 1L;
        Long locationId = 2L;

        when(courierRepository.existsCourierById(courierId)).thenReturn(true);
        when(locationRepository.existsById(locationId)).thenReturn(true);

        assertThrows(NotFoundException.class,
            () -> tariffService.getTariffInfoForLocation(courierId, locationId));
    }

    @Test
    void getTariffForOrder_Found() {
        Long orderId = 10L;
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        TariffsForLocationDto expected = new TariffsForLocationDto();

        when(tariffsInfoRepository.findByOrdersId(orderId)).thenReturn(Optional.of(tariffsInfo));
        when(modelMapper.map(tariffsInfo, TariffsForLocationDto.class)).thenReturn(expected);

        TariffsForLocationDto result = tariffService.getTariffForOrder(orderId);

        assertEquals(expected, result);
    }

    @Test
    void getTariffForOrder_NotFound() {
        Long orderId = 10L;
        when(tariffsInfoRepository.findByOrdersId(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tariffService.getTariffForOrder(orderId));
    }

    @Test
    void checkIfTariffExistsById_ReturnsTrue() {
        Long tariffId = 5L;
        when(tariffsInfoRepository.existsById(tariffId)).thenReturn(true);

        boolean result = tariffService.checkIfTariffExistsById(tariffId);

        assertTrue(result);
    }

    @Test
    void checkIfTariffExistsById_ReturnsFalse() {
        Long tariffId = 5L;
        when(tariffsInfoRepository.existsById(tariffId)).thenReturn(false);

        boolean result = tariffService.checkIfTariffExistsById(tariffId);

        assertFalse(result);
    }

    @Test
    void getTariffIdByLocationId_Found() {
        Long locationId = 2L;
        List<Long> ids = List.of(1L, 2L, 3L);

        when(tariffsInfoRepository.findTariffIdByLocationId(locationId)).thenReturn(Optional.of(ids));

        List<Long> result = tariffService.getTariffIdByLocationId(locationId);

        assertEquals(ids, result);
    }

    @Test
    void getTariffIdByLocationId_EmptyList() {
        Long locationId = 2L;

        when(tariffsInfoRepository.findTariffIdByLocationId(locationId)).thenReturn(Optional.of(List.of()));

        assertThrows(NotFoundException.class, () -> tariffService.getTariffIdByLocationId(locationId));
    }

    @Test
    void getTariffIdByLocationId_NotFound() {
        Long locationId = 2L;

        when(tariffsInfoRepository.findTariffIdByLocationId(locationId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tariffService.getTariffIdByLocationId(locationId));
    }
}