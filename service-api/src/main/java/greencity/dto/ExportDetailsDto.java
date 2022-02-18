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
    String receivingStation;
    List<String> allReceivingStations;
}
