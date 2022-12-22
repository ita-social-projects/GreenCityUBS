package greencity.mapping.station;

import greencity.dto.courier.ReceivingStationDto;
import greencity.entity.user.employee.ReceivingStation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ReceivingStation} into
 * {@link ReceivingStationDto}.
 */
@Component
public class ReceivingStationDtoMapper extends AbstractConverter<ReceivingStation, ReceivingStationDto> {
    /**
     * Method convert {@link ReceivingStation} to {@link ReceivingStationDto}.
     *
     * @return {@link ReceivingStationDto}
     */
    @Override
    protected ReceivingStationDto convert(ReceivingStation receivingStation) {
        return ReceivingStationDto.builder()
            .id(receivingStation.getId())
            .name(receivingStation.getName())
            .createDate(receivingStation.getCreateDate())
            .createdBy(receivingStation.getCreatedBy().getRecipientName() + " "
                + receivingStation.getCreatedBy().getRecipientSurname())
                .stationStatus(receivingStation.getStationStatus())
            .build();
    }
}
