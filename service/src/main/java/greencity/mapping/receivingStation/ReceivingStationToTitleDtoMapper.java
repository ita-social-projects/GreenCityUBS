package greencity.mapping.receivingStation;

import greencity.dto.OptionForColumnDTO;
import greencity.dto.courier.ReceivingStationDto;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ReceivingStationDto}
 * into {@link OptionForColumnDTO}.
 */
@Component
public class ReceivingStationToTitleDtoMapper extends AbstractConverter<ReceivingStationDto, OptionForColumnDTO> {
    /**
     * Method convert {@link ReceivingStationDto} to {@link OptionForColumnDTO}.
     *
     * @return {@link OptionForColumnDTO}
     */
    @Override
    protected OptionForColumnDTO convert(ReceivingStationDto receivingStationDto) {
        return OptionForColumnDTO.builder()
            .key(receivingStationDto.getId().toString())
            .ua(receivingStationDto.getName())
            .en(receivingStationDto.getName())
            .build();
    }
}
