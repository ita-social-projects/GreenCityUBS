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
        GetServiceDto dto = ModelUtils.getServiceDto();
        GetServiceDto convertDto = mapper.convert(service);

        Assertions.assertEquals(dto.getId(), convertDto.getId());
        Assertions.assertEquals(dto.getName(), convertDto.getName());
        Assertions.assertEquals(dto.getNameEng(), convertDto.getNameEng());
        Assertions.assertEquals(dto.getDescription(), convertDto.getDescription());
        Assertions.assertEquals(dto.getNameEng(), convertDto.getNameEng());
        Assertions.assertEquals(dto.getPrice(), convertDto.getPrice());
    }
}
