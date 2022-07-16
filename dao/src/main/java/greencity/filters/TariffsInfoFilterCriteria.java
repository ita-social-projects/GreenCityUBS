package greencity.filters;

import greencity.entity.enums.LocationStatus;
import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class TariffsInfoFilterCriteria implements Serializable {
    private Integer region;
    private Integer[] location;
    private Integer courier;
    private Integer[] receivingStation;
    private LocationStatus status;
}
