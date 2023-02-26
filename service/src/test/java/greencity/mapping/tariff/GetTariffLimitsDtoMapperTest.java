package greencity.mapping.tariff;

import greencity.ModelUtils;
import greencity.dto.tariff.GetTariffLimitsDto;
import greencity.entity.order.TariffsInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetTariffLimitsDtoMapperTest {
    @InjectMocks
    private GetTariffLimitsDtoMapper mapper;

    @Test
    void convertTariffInfoLimits() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        GetTariffLimitsDto dto = mapper.convert(tariffsInfo);
        Assertions.assertEquals(tariffsInfo.getMin(), dto.getMin());
        Assertions.assertEquals(tariffsInfo.getMax(), dto.getMax());
        Assertions.assertEquals(tariffsInfo.getCourierLimit(), dto.getCourierLimit());
    }
}
