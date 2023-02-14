package greencity.mapping.service;

import greencity.ModelUtils;
import greencity.dto.service.GetServiceDto;
import greencity.entity.order.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetServiceDtoMapperTest {
    @InjectMocks
    private GetServiceDtoMapper mapper;

    @Test
    void convert() {
        Service service = ModelUtils.getService();
        GetServiceDto convertDto = mapper.convert(service);

        Assertions.assertEquals(service.getId(), convertDto.getId());
        Assertions.assertEquals(service.getName(), convertDto.getName());
        Assertions.assertEquals(service.getNameEng(), convertDto.getNameEng());
        Assertions.assertEquals(service.getDescription(), convertDto.getDescription());
        Assertions.assertEquals(service.getDescriptionEng(), convertDto.getDescriptionEng());
        Assertions.assertEquals(service.getPrice(), convertDto.getPrice());
    }
}
