package greencity.service.ubs.tariff;

import static greencity.ModelUtils.getTariffInfoByLocationDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.exceptions.NotFoundException;
import greencity.repository.CourierRepository;
import greencity.repository.LocationRepository;
import greencity.repository.TariffsInfoRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        when(modelMapper.map(tariffsInfo, TariffsForLocationDto.class))
            .thenReturn(tariffInfoByLocationDto.getTariffsForLocationDto());

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

    @Test
    void getTariffsInfo_whenRepositoryReturnsEmptyList_thenReturnEmptyList() {
        when(tariffsInfoRepository.findAllActiveTariffsInfo()).thenReturn(List.of());

        List<GetActiveTariffInfoDto> result = tariffService.getTariffsInfo();

        assertThat(result).isEmpty();
        verify(tariffsInfoRepository).findAllActiveTariffsInfo();
    }

    @Test
    void getTariffsInfo_whenSingleTariffWithOneLocation_thenDtoWithoutDescriptionMessages() {
        TariffsInfo entity = TariffsInfo.builder()
            .tariffLocations(Set.of(new TariffLocation()))
            .build();

        GetActiveTariffInfoDto mappedDto = new GetActiveTariffInfoDto();
        when(modelMapper.map(entity, GetActiveTariffInfoDto.class)).thenReturn(mappedDto);
        when(tariffsInfoRepository.findAllActiveTariffsInfo()).thenReturn(List.of(entity));

        List<GetActiveTariffInfoDto> result = tariffService.getTariffsInfo();

        assertThat(result).hasSize(1);
        GetActiveTariffInfoDto dto = result.get(0);
        assertThat(dto.getDescriptionMessageUk()).isNull();
        assertThat(dto.getDescriptionMessageEn()).isNull();
    }

    @Test
    void getTariffsInfo_whenSingleTariffWithMultipleLocations_thenDtoWithDescriptionMessages() {
        Location loc1 = Location.builder()
            .nameUk("Київ")
            .nameEn("Kyiv")
            .build();
        Location loc2 = Location.builder()
            .nameUk("Львів")
            .nameEn("Lviv")
            .build();

        TariffLocation tl1 = TariffLocation.builder()
            .location(loc1)
            .build();
        TariffLocation tl2 = TariffLocation.builder()
            .location(loc2)
            .build();

        TariffsInfo entity = TariffsInfo.builder()
            .tariffLocations(Set.of(tl1, tl2))
            .build();

        GetActiveTariffInfoDto mappedDto = new GetActiveTariffInfoDto();
        when(modelMapper.map(entity, GetActiveTariffInfoDto.class)).thenReturn(mappedDto);
        when(tariffsInfoRepository.findAllActiveTariffsInfo()).thenReturn(List.of(entity));

        List<GetActiveTariffInfoDto> result = tariffService.getTariffsInfo();

        assertThat(result).hasSize(1);
        GetActiveTariffInfoDto dto = result.get(0);

        assertThat(dto.getDescriptionMessageUk())
            .contains("До тарифу також включені")
            .contains("Київ")
            .contains("Львів");

        assertThat(dto.getDescriptionMessageEn())
            .contains("The tariff also includes")
            .contains("Kyiv")
            .contains("Lviv");
    }
}