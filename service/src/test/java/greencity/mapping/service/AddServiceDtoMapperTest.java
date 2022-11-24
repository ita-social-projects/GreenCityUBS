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

        Assertions.assertEquals(dto.getTariffTranslationDtoList().get(0).getName(),
            convertDto.getTariffTranslationDtoList().get(0).getName());

        Assertions.assertEquals(dto.getTariffTranslationDtoList().get(0).getDescription(),
            convertDto.getTariffTranslationDtoList().get(0).getDescription());

        Assertions.assertEquals(dto.getTariffTranslationDtoList().get(0).getNameEng(),
            convertDto.getTariffTranslationDtoList().get(0).getNameEng());

        Assertions.assertEquals(dto.getTariffTranslationDtoList().get(0).getDescriptionEng(),
                convertDto.getTariffTranslationDtoList().get(0).getDescriptionEng());

        Assertions.assertEquals(150, bag.getFullPrice());
    }
}
