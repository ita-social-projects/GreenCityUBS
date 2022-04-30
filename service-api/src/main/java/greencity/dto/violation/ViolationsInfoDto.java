package greencity.dto.violation;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ViolationsInfoDto {
    private Integer violationsAmount;
    private Map<Long, String> violationsDescription;
}
