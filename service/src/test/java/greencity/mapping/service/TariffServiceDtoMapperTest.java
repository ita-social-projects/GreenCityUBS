package greencity.mapping.service;

import greencity.ModelUtils;
import greencity.dto.service.TariffServiceDto;
import greencity.entity.order.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TariffServiceDtoMapperTest {
    @InjectMocks
    private TariffServiceDtoMapper mapper;

    @Test
    void convert() {
        Bag bag = ModelUtils.getTariffBag();
        TariffServiceDto dto = ModelUtils.TariffServiceDto();
        TariffServiceDto convertDto = mapper.convert(bag);

        Assertions.assertEquals(dto.getCapacity(), convertDto.getCapacity());
        Assertions.assertEquals(dto.getPrice(), convertDto.getPrice());
        Assertions.assertEquals(dto.getCommission(), convertDto.getCommission());
        Assertions.assertEquals(dto.getNameUk(), convertDto.getNameUk());
        Assertions.assertEquals(dto.getDescriptionUk(), convertDto.getDescriptionUk());
        Assertions.assertEquals(dto.getNameEn(), convertDto.getNameEn());
        Assertions.assertEquals(dto.getDescriptionEn(), convertDto.getDescriptionEn());
    }
}
