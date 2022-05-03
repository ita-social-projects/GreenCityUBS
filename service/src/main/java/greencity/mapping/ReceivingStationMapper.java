package greencity.mapping;

import greencity.dto.courier.ReceivingStationDto;
import greencity.entity.user.employee.ReceivingStation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ReceivingStationDto}
 * into {@link ReceivingStation}.
 */
@Component
public class ReceivingStationMapper extends AbstractConverter<ReceivingStationDto, ReceivingStation> {
    /**
     * Method convert {@link ReceivingStationDto} to {@link ReceivingStation}.
     *
     * @return {@link ReceivingStation}
     */
    @Override
    protected ReceivingStation convert(ReceivingStationDto receivingStationDto) {
        return ReceivingStation.builder()
            .id(receivingStationDto.getId())
            .name(receivingStationDto.getName())
            .build();
    }
}
