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

        Assertions.assertEquals(dto, mapper.convert(serviceTranslation));
    }
}
