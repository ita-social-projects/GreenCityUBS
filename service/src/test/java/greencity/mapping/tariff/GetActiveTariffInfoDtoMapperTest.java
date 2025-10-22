package greencity.mapping.tariff;

import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.entity.order.TariffsInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class GetActiveTariffInfoDtoMapperTest {
    @InjectMocks
    private GetActiveTariffInfoDtoMapper mapper;

    @Test
    void convert() {
        TariffsInfo tariffInfo = TariffsInfo.builder()
            .tariffNameUk("Тест")
            .tariffNameEn("Test")
            .id(1L)
            .build();
        GetActiveTariffInfoDto expected = GetActiveTariffInfoDto.builder()
            .tariffNameUk("Тест")
            .tariffNameEn("Test")
            .id(1L)
            .build();

        GetActiveTariffInfoDto actual = mapper.convert(tariffInfo);
        assertEquals(actual, expected);
    }
}
