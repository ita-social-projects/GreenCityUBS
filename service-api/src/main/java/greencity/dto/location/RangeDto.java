package greencity.dto.location;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class RangeDto {
    private Long min;
    private Long max;
}
