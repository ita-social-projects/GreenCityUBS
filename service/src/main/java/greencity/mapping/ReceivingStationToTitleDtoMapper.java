package greencity.mapping;

import greencity.dto.ReceivingStationDto;
import greencity.dto.TitleDto;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map
 * {@link ReceivingStationDto} into {@link TitleDto}.
 */
@Component
public class ReceivingStationToTitleDtoMapper extends AbstractConverter<ReceivingStationDto, TitleDto> {
    /**
     * Method convert {@link ReceivingStationDto} to {@link TitleDto}.
     *
     * @return {@link TitleDto}
     */
    @Override
    protected TitleDto convert(ReceivingStationDto receivingStationDto) {
        return TitleDto.builder()
            .key(receivingStationDto.getId().toString())
            .ua(receivingStationDto.getName())
            .en(receivingStationDto.getName())
            .build();
    }
}
