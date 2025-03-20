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
        GetServiceDto expectedDto = ModelUtils.getGetServiceDto();
        GetServiceDto actualDto = mapper.convert(service);

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId());
        Assertions.assertEquals(expectedDto.getName(), actualDto.getName());
        Assertions.assertEquals(expectedDto.getNameEng(), actualDto.getNameEng());
        Assertions.assertEquals(expectedDto.getDescription(), actualDto.getDescription());
        Assertions.assertEquals(expectedDto.getDescriptionEng(), actualDto.getDescriptionEng());
        Assertions.assertEquals(expectedDto.getPrice(), actualDto.getPrice());
    }
}
