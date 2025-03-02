package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
