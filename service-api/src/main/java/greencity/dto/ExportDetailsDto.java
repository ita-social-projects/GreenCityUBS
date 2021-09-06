package greencity.dto;

import lombok.*;

import java.util.List;

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
