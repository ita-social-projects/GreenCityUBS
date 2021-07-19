package greencity.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExportDetailsDtoRequest {
    String exportedDate;
    String exportedTime;
    String receivingStation;
}
