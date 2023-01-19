package greencity.mapping.service;

import greencity.ModelUtils;
import greencity.dto.service.ServiceDto;
import greencity.entity.order.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceDtoMapperTest {
    @InjectMocks
    private ServiceDtoMapper mapper;

    @Test
    void convert() {
        Service service = ModelUtils.getService();
        ServiceDto convertDto = mapper.convert(service);

        Assertions.assertEquals(service.getId(), convertDto.getId());
        Assertions.assertEquals(service.getName(), convertDto.getName());
        Assertions.assertEquals(service.getNameEng(), convertDto.getNameEng());
        Assertions.assertEquals(service.getDescription(), convertDto.getDescription());
        Assertions.assertEquals(service.getNameEng(), convertDto.getNameEng());
        Assertions.assertEquals(service.getPrice(), convertDto.getPrice());
    }
}
