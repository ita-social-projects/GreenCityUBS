package greencity.dto;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = {"allReceivingStations"})
public class ExportDetailsDto {
    String exportedDate;
    String exportedTime;
    String receivingStation;
    List<String> allReceivingStations;
}
