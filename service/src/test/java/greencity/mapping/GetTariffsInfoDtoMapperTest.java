package greencity.mapping;

import greencity.ModelUtils;
import greencity.entity.order.TariffsInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTariffsInfoDtoMapperTest {
    @InjectMocks
    private GetTariffsInfoDtoMapper mapper;

    @Test
    void convert() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        var dto = mapper.convert(tariffsInfo);
        Assertions.assertEquals(tariffsInfo.getId(), dto.getCardId());
        Assertions.assertEquals(tariffsInfo.getCourierLimit().toString(), dto.getCourierLimit());
        Assertions.assertEquals(tariffsInfo.getCreatedAt(), dto.getCreatedAt());
        Assertions.assertEquals(tariffsInfo.getLocationStatus(), dto.getTariffStatus());

    }
}
