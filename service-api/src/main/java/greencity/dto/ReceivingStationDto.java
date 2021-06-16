package greencity.dto;

import lombok.*;

import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ReceivingStationDto {
    @Min(1)
    private Long id;
    private String receivingStation;
}
