package greencity.dto;

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
