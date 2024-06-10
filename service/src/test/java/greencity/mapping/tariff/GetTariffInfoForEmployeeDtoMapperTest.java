package greencity.mapping.tariff;

import greencity.ModelUtils;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.order.TariffsInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class GetTariffInfoForEmployeeDtoMapperTest {

    @InjectMocks
    GetTariffInfoForEmployeeDtoMapper mapper;

    @Test
    void convertTariffsInfoToGetTariffInfoForEmployeeDtoTest() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        GetTariffInfoForEmployeeDto dto = ModelUtils.getTariffInfoForEmployeeDto();
        assertEquals(mapper.convert(tariffsInfo), dto);
    }


    @Test
    void convertTariffsInfoToGetTariffInfoForEmployeeDtoWhenRegionIsNullTest() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffWithUknownRegionInfo();
        GetTariffInfoForEmployeeDto dto = ModelUtils.getTariffInfoForEmployeeDtoWithUnknownRegion();

        assertEquals(mapper.convert(tariffsInfo), dto);
    }
}
