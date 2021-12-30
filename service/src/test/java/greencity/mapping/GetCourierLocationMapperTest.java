package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.GetCourierLocationDto;
import greencity.entity.order.CourierLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCourierLocationMapperTest {
    @InjectMocks
    private GetCourierLocationMapper mapper;

    @Test
    void convert() {
        CourierLocation courierLocation = ModelUtils.getCourierLocations();
        GetCourierLocationDto getCourierLocationDto = ModelUtils.getCourierLocationsDto();

        Assertions.assertEquals(getCourierLocationDto, mapper.convert(courierLocation));
    }
}
