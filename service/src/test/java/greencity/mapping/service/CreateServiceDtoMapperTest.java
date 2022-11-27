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
        CreateServiceDto convertDto = mapper.convert(service);

        Assertions.assertEquals(dto.getServiceTranslationDtoList().get(0).getName(),
            convertDto.getServiceTranslationDtoList().get(0).getName());

        Assertions.assertEquals(dto.getServiceTranslationDtoList().get(0).getNameEng(),
            convertDto.getServiceTranslationDtoList().get(0).getNameEng());

        Assertions.assertEquals(dto.getServiceTranslationDtoList().get(0).getDescription(),
            convertDto.getServiceTranslationDtoList().get(0).getDescription());

        Assertions.assertEquals(dto.getServiceTranslationDtoList().get(0).getNameEng(),
            convertDto.getServiceTranslationDtoList().get(0).getNameEng());

        Assertions.assertEquals(dto.getCapacity(), convertDto.getCapacity());
        Assertions.assertEquals(dto.getCommission(), convertDto.getCommission());
        Assertions.assertEquals(dto.getPrice(), convertDto.getPrice());
        Assertions.assertEquals(dto.getCourierId(), convertDto.getCourierId());
        Assertions.assertEquals(150, service.getFullPrice());
    }
}
