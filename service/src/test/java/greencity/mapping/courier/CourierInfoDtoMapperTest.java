package greencity.mapping.courier;

import greencity.ModelUtils;
import greencity.dto.courier.CourierInfoDto;
import greencity.entity.enums.CourierLimit;
import greencity.entity.order.TariffsInfo;
import greencity.mapping.courier.CourierInfoDtoMapper;
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
            .maxAmountOfBigBags(20L)
            .maxPriceOfOrder(20000L)
            .minAmountOfBigBags(2L)
            .minPriceOfOrder(500L)
            .build();

        CourierInfoDto actual = courierInfoDtoMapper.convert(courierLocation);
        Assertions.assertEquals(actual.getCourierLimit(), expected.getCourierLimit());
        Assertions.assertEquals(actual.getMaxAmountOfBigBags(), expected.getMaxAmountOfBigBags());
        Assertions.assertEquals(actual.getMaxPriceOfOrder(), expected.getMaxPriceOfOrder());
        Assertions.assertEquals(actual.getMinAmountOfBigBags(), expected.getMinAmountOfBigBags());
        Assertions.assertEquals(actual.getMinPriceOfOrder(), expected.getMinPriceOfOrder());

    }
}
