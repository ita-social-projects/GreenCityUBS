package greencity.service.ubs.tariff;

import static greencity.ModelUtils.getTariffInfoByLocationDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.entity.order.Courier;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.exceptions.NotFoundException;
import greencity.repository.CourierRepository;
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
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TariffServiceImpl tariffService;

    @Test
    void getTariffInfo() {
        Long tariffId = 1L;

        TariffInfoByLocationDto tariffInfoByLocationDto = getTariffInfoByLocationDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(tariffsInfoRepository.findById(tariffId))
            .thenReturn(Optional.ofNullable(tariffsInfo));
        when(modelMapper.map(tariffsInfo, TariffsForLocationDto.class))
            .thenReturn(tariffInfoByLocationDto.getTariffsForLocationDto());

        TariffInfoByLocationDto result = tariffService.getTariffInfo(tariffId);
        assertEquals(tariffInfoByLocationDto, result);
    }

    @Test
    void getTariffInfo_TariffNotFound() {
        Long tariffId = 1L;

        when(tariffsInfoRepository.findById(tariffId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> tariffService.getTariffInfo(tariffId));
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
        Courier courier = ModelUtils.getCourier();

        when(tariffsInfoRepository.findAllActiveTariffsInfoWithCourierId(courier)).thenReturn(List.of());
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        List<GetActiveTariffInfoDto> result = tariffService.getTariffsInfo(1L);

        assertThat(result).isEmpty();
        verify(tariffsInfoRepository).findAllActiveTariffsInfoWithCourierId(courier);
    }

    @Test
    void getTariffsInfo_whenSingleTariffWithOneLocation_thenDtoWithoutDescriptionMessages() {
        TariffsInfo entity = TariffsInfo.builder()
            .tariffLocations(Set.of(new TariffLocation()))
            .build();

        GetActiveTariffInfoDto mappedDto = new GetActiveTariffInfoDto();
        Courier courier = ModelUtils.getCourier();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(tariffsInfoRepository.findAllActiveTariffsInfoWithCourierId(courier)).thenReturn(List.of(entity));
        when(modelMapper.map(entity, GetActiveTariffInfoDto.class)).thenReturn(mappedDto);

        List<GetActiveTariffInfoDto> result = tariffService.getTariffsInfo(1L);

        assertThat(result).hasSize(1);
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

        Courier courier = ModelUtils.getCourier();

        GetActiveTariffInfoDto mappedDto = GetActiveTariffInfoDto
            .builder()
            .build();
        when(modelMapper.map(entity, GetActiveTariffInfoDto.class)).thenReturn(mappedDto);
        when(tariffsInfoRepository.findAllActiveTariffsInfoWithCourierId(courier)).thenReturn(List.of(entity));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        List<GetActiveTariffInfoDto> result = tariffService.getTariffsInfo(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTariffsInfo_whenCourierNotFound_shouldThrowNotFoundException() {
        Long courierId = 99L;
        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tariffService.getTariffsInfo(courierId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + courierId);
    }
}