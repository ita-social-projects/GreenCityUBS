package greencity.mapping.bag;

import greencity.dto.bag.BagInfoDto;
import greencity.entity.order.OrderBag;
import greencity.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getOrderBag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OrderBagToBagInfoDtoMapperTest {
    @InjectMocks
    private OrderBagToBagInfoDtoMapper mapper;

    @Test
    void convertTest() {
        OrderBag orderBag = getOrderBag();

        BagInfoDto result = mapper.convert(orderBag);

        assertNotNull(result, "The result should not be null");
        assertEquals(orderBag.getBag().getId(), result.getId(), "Bag ID should match");
        assertEquals(120.00, result.getPrice(), "Price should be converted to decimal format");
        assertEquals(orderBag.getCapacity(), result.getCapacity(), "Capacity should match");
        assertEquals(orderBag.getNameUk(), result.getName(), "Name should match");
        assertEquals(orderBag.getNameEn(), result.getNameEng(), "NameEn should match");
    }

    @Test
    void convertNullSourceTest() {
        OrderBag orderBag = null;
        assertThrows(BadRequestException.class, () -> mapper.convert(orderBag));
    }

    @Test
    void convertNullBagTest() {
        OrderBag orderBag = getOrderBag();
        orderBag.setBag(null);
        assertThrows(BadRequestException.class, () -> mapper.convert(orderBag));
    }
}
