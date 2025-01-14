package greencity.mapping.station;

import greencity.ModelUtils;
import greencity.dto.courier.ReceivingStationDto;
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.StationStatus;
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

        assertEquals(expected.getName(), mapper.convert(receivingStation).getName());
    }

    @Test
    void convertStatus() {
        StationStatus stationStatus = StationStatus.ACTIVE;
        ReceivingStationDto expected = ModelUtils.getReceivingStationDto();
        expected.setStationStatus(stationStatus);
        ReceivingStation receivingStation = ModelUtils.getReceivingStation();
        receivingStation.setStationStatus(stationStatus);

        assertEquals(expected.getStationStatus(), mapper.convert(receivingStation).getStationStatus());
    }
}
