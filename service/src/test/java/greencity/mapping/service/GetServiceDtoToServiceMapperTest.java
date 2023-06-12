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
class GetServiceDtoToServiceMapperTest {
    @InjectMocks
    private GetServiceDtoToServiceMapper mapper;

    @Test
    void convert() {
        Service expectedService = ModelUtils.getService();
        GetServiceDto getServiceDto = ModelUtils.getGetServiceDto();
        Service actualService = mapper.convert(getServiceDto);

        Assertions.assertEquals(expectedService.getId(), actualService.getId());
        Assertions.assertEquals(expectedService.getName(), actualService.getName());
        Assertions.assertEquals(expectedService.getNameEng(), actualService.getNameEng());
        Assertions.assertEquals(expectedService.getDescription(), actualService.getDescription());
        Assertions.assertEquals(expectedService.getDescriptionEng(), actualService.getDescriptionEng());
        Assertions.assertEquals(expectedService.getPrice(), actualService.getPrice());
    }
}
