package greencity.dto.order;

import greencity.dto.courier.ReceivingStationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
