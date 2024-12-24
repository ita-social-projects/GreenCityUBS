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
        assertEquals(expectedBag.getName(), actualBag.getName());
        assertEquals(expectedBag.getDescription(), actualBag.getDescription());
        assertEquals(expectedBag.getNameEng(), actualBag.getNameEng());
        assertEquals(expectedBag.getDescriptionEng(), actualBag.getDescriptionEng());
        assertEquals(expectedBag.getLimitIncluded(), actualBag.getLimitIncluded());
        assertEquals(expectedBag.getCreatedAt(), actualBag.getCreatedAt());
    }
}
