package greencity.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExportDetailsDtoUpdate {
    String exportedDate;
    String exportedTime;
    String receivingStation;
}
