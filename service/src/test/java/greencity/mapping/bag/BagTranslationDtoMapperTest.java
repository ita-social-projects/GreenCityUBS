package greencity.mapping.bag;

import greencity.ModelUtils;
import greencity.dto.bag.BagTranslationDto;
import greencity.entity.order.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BagTranslationDtoMapperTest {
    @InjectMocks
    BagTranslationDtoMapper mapper;

    @Test
    void convert() {
        Bag bag = ModelUtils.getTariffBag();
        BagTranslationDto dto = ModelUtils.getBagTranslationDto();
        BagTranslationDto convertDto = mapper.convert(bag);

        Assertions.assertEquals(dto.getId(), convertDto.getId());
        Assertions.assertEquals(dto.getCapacity(), convertDto.getCapacity());
        Assertions.assertEquals(dto.getPrice(), convertDto.getPrice());
        Assertions.assertEquals(dto.getName(), convertDto.getName());
        Assertions.assertEquals(dto.getNameEng(), convertDto.getNameEng());
        Assertions.assertEquals(dto.getLimitedIncluded(), convertDto.getLimitedIncluded());
    }
}
