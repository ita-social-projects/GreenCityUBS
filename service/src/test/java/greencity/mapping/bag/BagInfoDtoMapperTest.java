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
        BagInfoDto expectedBagInfoDto = ModelUtils.TEST_BAG_INFO_DTO;
        Bag bag = ModelUtils.TEST_BAG;
        BagInfoDto actualBagInfoDto = bagInfoDtoMapper.convert(bag);

        assertEquals(expectedBagInfoDto.getId(), actualBagInfoDto.getId());
        assertEquals(expectedBagInfoDto.getName(), actualBagInfoDto.getName());
        assertEquals(expectedBagInfoDto.getNameEng(), actualBagInfoDto.getNameEng());
        assertEquals(expectedBagInfoDto.getCapacity(), actualBagInfoDto.getCapacity());
        assertEquals(expectedBagInfoDto.getPrice(), actualBagInfoDto.getPrice());
    }
}
