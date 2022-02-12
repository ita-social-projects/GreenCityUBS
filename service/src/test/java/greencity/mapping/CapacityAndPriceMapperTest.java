package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.BagInfoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import greencity.entity.order.Bag;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CapacityAndPriceMapperTest {

    @InjectMocks
    CapacityAndPriceMapper capacityAndPriceMapper;

    @Test
    void convert() {
        Bag bag = ModelUtils.TEST_BAG;
        BagInfoDto actualBagInfoDto = capacityAndPriceMapper.convert(bag);

        assertEquals(bag.getCapacity(), actualBagInfoDto.getCapacity());
        assertEquals(bag.getFullPrice(), actualBagInfoDto.getPrice());
        assertEquals(bag.getId(), actualBagInfoDto.getId());
    }
}
