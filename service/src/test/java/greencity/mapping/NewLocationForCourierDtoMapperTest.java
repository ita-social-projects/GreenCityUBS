package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.NewLocationForCourierDto;
import greencity.entity.order.CourierLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NewLocationForCourierDtoMapperTest {
    @InjectMocks
    NewLocationForCourierDtoMapper mapper;

    @Test
    void convert() {
        NewLocationForCourierDto dto = ModelUtils.newLocationForCourierDto();
        CourierLocation courierLocation = ModelUtils.getCourierLocations();

        Assertions.assertEquals(courierLocation, mapper.convert(dto));
    }
}
