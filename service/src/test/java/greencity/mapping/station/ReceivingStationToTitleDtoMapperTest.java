package greencity.mapping.station;

import greencity.ModelUtils;
import greencity.dto.OptionForColumnDTO;
import greencity.dto.courier.ReceivingStationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ReceivingStationToTitleDtoMapperTest {

    @InjectMocks
    ReceivingStationToTitleDtoMapper mapper;

    @Test
    void convert() {
        OptionForColumnDTO expected = ModelUtils.getOptionForColumnDTO();
        ReceivingStationDto receivingStationDto = ModelUtils.getOptionReceivingStationDto();

        assertEquals(expected.getEn(), mapper.convert(receivingStationDto).getEn());
        assertEquals(expected.getKey(), mapper.convert(receivingStationDto).getKey());
        assertEquals(expected.getUa(), mapper.convert(receivingStationDto).getUa());
    }
}
