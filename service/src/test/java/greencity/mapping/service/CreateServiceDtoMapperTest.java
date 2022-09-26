package greencity.mapping.service;

import greencity.ModelUtils;
import greencity.dto.service.CreateServiceDto;
import greencity.entity.order.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateServiceDtoMapperTest {
    @InjectMocks
    private CreateServiceDtoMapper mapper;

    @Test
    void convert() {
        Service service = ModelUtils.getService();
        CreateServiceDto dto = ModelUtils.getCreateServiceDto();

        Assertions.assertEquals(dto.getServiceTranslationDtoList().get(0).getName(),
            mapper.convert(service).getServiceTranslationDtoList().get(0).getName());
        Assertions.assertEquals(dto.getServiceTranslationDtoList().get(0).getDescription(),
            mapper.convert(service).getServiceTranslationDtoList().get(0).getDescription());
        Assertions.assertEquals(dto.getServiceTranslationDtoList().get(0).getNameEng(),
            mapper.convert(service).getServiceTranslationDtoList().get(0).getNameEng());
        Assertions.assertEquals(dto.getCapacity(), mapper.convert(service).getCapacity());
        Assertions.assertEquals(dto.getCommission(), mapper.convert(service).getCommission());
        Assertions.assertEquals(dto.getPrice(), mapper.convert(service).getPrice());
        Assertions.assertEquals(dto.getCourierId(), mapper.convert(service).getCourierId());
        Assertions.assertEquals(150, service.getFullPrice());
    }
}
