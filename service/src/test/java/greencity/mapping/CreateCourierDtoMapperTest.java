package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.CreateCourierDto;
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

        Assertions.assertEquals(dto.getCreateCourierTranslationDtos().get(0).getName(),
            mapper.convert(courier).getCreateCourierTranslationDtos().get(0).getName());
        Assertions.assertEquals(dto.getCreateCourierTranslationDtos().get(0).getLimitDescription(),
            mapper.convert(courier).getCreateCourierTranslationDtos().get(0).getLimitDescription());
        Assertions.assertEquals(dto.getCreateCourierTranslationDtos().get(0).getLanguageId(),
            mapper.convert(courier).getCreateCourierTranslationDtos().get(0).getLanguageId());
    }
}
