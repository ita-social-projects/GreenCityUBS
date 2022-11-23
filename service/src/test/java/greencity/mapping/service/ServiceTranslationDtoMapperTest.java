package greencity.mapping.service;

import greencity.ModelUtils;
import greencity.dto.service.ServiceTranslationDto;
import greencity.entity.order.ServiceTranslation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceTranslationDtoMapperTest {
    @InjectMocks
    private SerivceTransaltionDtoMapper mapper;

    @Test
    void convert() {
        ServiceTranslation serviceTranslation = ModelUtils.getServiceTranslation();
        ServiceTranslationDto dto = ModelUtils.getServiceTranslationDto();

        Assertions.assertEquals(dto.getName(), mapper.convert(serviceTranslation).getName());
        Assertions.assertEquals(dto.getDescription(), mapper.convert(serviceTranslation).getDescription());
        Assertions.assertEquals(dto.getNameEng(), mapper.convert(serviceTranslation).getNameEng());
        Assertions.assertEquals(dto.getDescriptionEng(), mapper.convert(serviceTranslation).getDescriptionEng());
    }
}
