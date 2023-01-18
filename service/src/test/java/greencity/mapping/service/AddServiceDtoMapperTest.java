package greencity.mapping.service;

import greencity.ModelUtils;
import greencity.dto.service.AddServiceDto;
import greencity.entity.order.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddServiceDtoMapperTest {
    @InjectMocks
    private AddServiceDtoMapper mapper;

    @Test
    void convert() {
        Bag bag = ModelUtils.getTariffBag();
        AddServiceDto dto = ModelUtils.addServiceDto();
        AddServiceDto convertDto = mapper.convert(bag);

        Assertions.assertEquals(dto.getTariffTranslationDto().getName(),
            convertDto.getTariffTranslationDto().getName());
        Assertions.assertEquals(dto.getTariffTranslationDto().getDescription(),
            convertDto.getTariffTranslationDto().getDescription());
        Assertions.assertEquals(dto.getTariffTranslationDto().getNameEng(),
            convertDto.getTariffTranslationDto().getNameEng());
        Assertions.assertEquals(dto.getTariffTranslationDto().getDescriptionEng(),
            convertDto.getTariffTranslationDto().getDescriptionEng());
        Assertions.assertEquals(150, bag.getFullPrice());
    }
}
