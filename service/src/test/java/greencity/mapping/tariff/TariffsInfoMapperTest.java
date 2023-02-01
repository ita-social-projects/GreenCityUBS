package greencity.mapping.tariff;

import greencity.ModelUtils;
import greencity.dto.tariff.TariffsInfoDto;
import greencity.entity.order.TariffsInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TariffsInfoMapperTest {
    @InjectMocks
    private TariffsInfoMapper mapper;

    @Test
    void convertTariffsInfo() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        TariffsInfoDto dto = mapper.convert(tariffsInfo);
        Assertions.assertEquals(tariffsInfo.getId(), dto.getId());
        Assertions.assertEquals(tariffsInfo.getCourier().getId(), dto.getCourier().getCourierId());
        Assertions.assertEquals(tariffsInfo.getTariffStatus(), dto.getTariffStatus());

    }
}
