package greencity.mapping.courier;

import greencity.ModelUtils;
import greencity.dto.courier.CourierInfoDto;
import greencity.enums.CourierLimit;
import greencity.entity.order.TariffsInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourierInfoDtoMapperTest {

    @InjectMocks
    CourierInfoDtoMapper courierInfoDtoMapper;

    @Test
    void convert() {

        TariffsInfo courierLocation = ModelUtils.getTariffsInfo();

        CourierInfoDto expected = CourierInfoDto.builder()
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .min(2L)
            .max(20L)
            .build();

        CourierInfoDto actual = courierInfoDtoMapper.convert(courierLocation);
        Assertions.assertEquals(actual.getCourierLimit(), expected.getCourierLimit());
        Assertions.assertEquals(actual.getMax(), expected.getMax());
        Assertions.assertEquals(actual.getMin(), expected.getMin());

    }
}
