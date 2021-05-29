package greencity.dto;

import java.util.Map;
import lombok.*;

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
