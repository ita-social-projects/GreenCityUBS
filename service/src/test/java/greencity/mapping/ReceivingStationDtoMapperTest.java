package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.ReceivingStationDto;
import greencity.entity.user.employee.ReceivingStation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ReceivingStationDtoMapperTest {
    @InjectMocks
    ReceivingStationDtoMapper mapper;

    @Test
    void convert() {
        ReceivingStationDto expected = ModelUtils.getReceivingStationDto();
        ReceivingStation receivingStation = ModelUtils.getReceivingStation();

        assertEquals(expected, mapper.convert(receivingStation));
    }
}
