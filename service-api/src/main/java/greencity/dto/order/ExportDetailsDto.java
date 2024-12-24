package greencity.dto.order;

import greencity.dto.courier.ReceivingStationDto;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = {"allReceivingStations"})
@ToString
public class ExportDetailsDto {
    String dateExport;
    String timeDeliveryFrom;
    String timeDeliveryTo;
    Long receivingStationId;
    List<ReceivingStationDto> allReceivingStations;
}
