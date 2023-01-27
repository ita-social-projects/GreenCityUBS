package greencity.dto.courier;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class GetReceivingStationDto {
    private Long stationId;
    private String name;
}
