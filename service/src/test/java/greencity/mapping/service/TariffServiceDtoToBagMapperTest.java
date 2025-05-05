package greencity.mapping.service;

import greencity.ModelUtils;
import greencity.dto.service.TariffServiceDto;
import greencity.entity.order.Bag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TariffServiceDtoToBagMapperTest {
    @InjectMocks
    private TariffServiceDtoToBagMapper mapper;

    @Test
    void convert() {
        Bag expectedBag = ModelUtils.getTariffBag();
        TariffServiceDto dto = ModelUtils.TariffServiceDto();
        Bag actualBag = mapper.convert(dto);

        assertEquals(expectedBag.getCapacity(), actualBag.getCapacity());
        assertEquals(expectedBag.getPrice(), actualBag.getPrice());
        assertEquals(expectedBag.getCommission(), actualBag.getCommission());
        assertEquals(expectedBag.getFullPrice(), actualBag.getFullPrice());
        assertEquals(expectedBag.getNameUk(), actualBag.getNameUk());
        assertEquals(expectedBag.getDescriptionUk(), actualBag.getDescriptionUk());
        assertEquals(expectedBag.getNameEn(), actualBag.getNameEn());
        assertEquals(expectedBag.getDescriptionEn(), actualBag.getDescriptionEn());
        assertEquals(expectedBag.getLimitIncluded(), actualBag.getLimitIncluded());
        assertEquals(expectedBag.getCreatedAt(), actualBag.getCreatedAt());
    }
}
