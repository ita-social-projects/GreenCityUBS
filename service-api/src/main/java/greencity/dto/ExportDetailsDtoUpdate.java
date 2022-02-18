package greencity.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExportDetailsDtoUpdate {
    String dateExport;
    String timeDeliveryFrom;
    String timeDeliveryTo;
    Long receivingStationId;
}
