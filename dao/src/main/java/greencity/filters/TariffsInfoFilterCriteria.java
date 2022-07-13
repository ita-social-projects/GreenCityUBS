package greencity.filters;

import greencity.entity.enums.LocationStatus;
import lombok.*;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class TariffsInfoFilterCriteria {
    private Integer region;
    private Integer[] location;
    private Integer courier;
    private Integer[] receivingStation;
    private LocationStatus status;
}
