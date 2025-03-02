package greencity.mapping.station;

import greencity.ModelUtils;
import greencity.dto.courier.ReceivingStationDto;
import greencity.entity.user.employee.ReceivingStation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ReceivingStationMapperTest {

    @InjectMocks
    ReceivingStationMapper mapper;

    @Test
    void convert() {
        ReceivingStation expected = ModelUtils.getReceivingStation();
        ReceivingStationDto receivingStationDto = ModelUtils.getReceivingStationDto();

        assertEquals(expected.getName(), mapper.convert(receivingStationDto).getName());
    }

}
