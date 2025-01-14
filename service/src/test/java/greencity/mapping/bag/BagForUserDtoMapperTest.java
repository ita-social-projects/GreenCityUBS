package greencity.mapping.bag;

import greencity.ModelUtils;
import greencity.dto.bag.BagForUserDto;
import greencity.entity.order.OrderBag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BagForUserDtoMapperTest {
    @InjectMocks
    BagForUserDtoMapper bagForUserDtoMapper;

    @Test
    void convert() {
        BagForUserDto expected = ModelUtils.TEST_BAG_FOR_USER_DTO;
        OrderBag orderBag = ModelUtils.TEST_ORDER_BAG;
        BagForUserDto actual = bagForUserDtoMapper.convert(orderBag);

        assertEquals(expected.getService(), actual.getService());
        assertEquals(expected.getServiceEng(), actual.getServiceEng());
        assertEquals(expected.getCapacity(), actual.getCapacity());
        assertEquals(expected.getFullPrice(), actual.getFullPrice());
    }

}
