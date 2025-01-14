package greencity.mapping.courier;

import greencity.ModelUtils;
import greencity.dto.courier.CreateCourierDto;
import greencity.entity.order.Courier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCourierDtoMapperTest {
    @InjectMocks
    private CreateCourierDtoMapper mapper;

    @Test
    void convert() {
        Courier courier = ModelUtils.getCourier();
        CreateCourierDto dto = ModelUtils.getCreateCourierDto();

        CreateCourierDto createCourierDto = mapper.convert(courier);
        Assertions.assertEquals(createCourierDto.getNameUk(), dto.getNameUk());
        Assertions.assertEquals(createCourierDto.getNameEn(), dto.getNameEn());
    }
}
