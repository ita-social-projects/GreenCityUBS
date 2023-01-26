package greencity.mapping.bag;

import greencity.ModelUtils;
import greencity.dto.bag.BagInfoDto;
import greencity.entity.order.Bag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BagInfoDtoMapperTest {

    @InjectMocks
    BagInfoDtoMapper bagInfoDtoMapper;

    @Test
    void convert() {
        Bag bag = ModelUtils.TEST_BAG;
        BagInfoDto actualBagInfoDto = bagInfoDtoMapper.convert(bag);

        assertEquals(bag.getId(), actualBagInfoDto.getId());
        assertEquals(bag.getName(), actualBagInfoDto.getName());
        assertEquals(bag.getNameEng(), actualBagInfoDto.getNameEng());
        assertEquals(bag.getCapacity(), actualBagInfoDto.getCapacity());
        assertEquals(bag.getFullPrice(), actualBagInfoDto.getPrice());
    }
}
