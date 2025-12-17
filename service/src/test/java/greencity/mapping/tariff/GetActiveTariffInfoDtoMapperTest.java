package greencity.mapping.tariff;

import greencity.ModelUtils;
import greencity.dto.location.LocationsForTariffDto;
import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.mapping.location.LocationToLocationsForTariffDtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class GetActiveTariffInfoDtoMapperTest {
    @InjectMocks
    private GetActiveTariffInfoDtoMapper mapper;

    @Mock
    LocationToLocationsForTariffDtoMapper locationToLocationsForTariffDtoMapper;

    @Test
    void testConvert_OnlyOneLocationsForTariff_EntityConverted() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();

        LocationsForTariffDto dto = ModelUtils.getLocationDtoFromDao();

        Mockito.when(locationToLocationsForTariffDtoMapper.convert(any(Location.class))).thenReturn(dto);

        GetActiveTariffInfoDto expected = GetActiveTariffInfoDto.builder()
            .tariffNameUk("Тариф")
            .tariffNameEn("Tariff")
            .tariffLocations(List.of(dto))
            .id(1L)
            .build();

        GetActiveTariffInfoDto actual = mapper.convert(tariffInfo);
        assertEquals(expected, actual);
    }

    @Test
    void testConvert_MoreThanOnLocationsForTariff_EntityConverted() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfoWIthMultipleLocations();

        LocationsForTariffDto dto = ModelUtils.getLocationDtoFromDao();

        Mockito.when(locationToLocationsForTariffDtoMapper.convert(any(Location.class))).thenReturn(dto);

        GetActiveTariffInfoDto expected = GetActiveTariffInfoDto.builder()
            .tariffNameUk("Тариф")
            .tariffNameEn("Tariff")
            .descriptionMessageUk("До тарифу також включені Київ, Київ")
            .descriptionMessageEn("The tariff also includes Kyiv, Kyiv")
            .tariffLocations(List.of(dto, dto))
            .id(1L)
            .build();

        GetActiveTariffInfoDto actual = mapper.convert(tariffInfo);
        assertEquals(expected, actual);
    }
}
