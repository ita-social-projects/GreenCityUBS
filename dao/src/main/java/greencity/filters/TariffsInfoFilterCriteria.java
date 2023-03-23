package greencity.filters;

import greencity.enums.TariffStatus;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffsInfoFilterCriteria implements Serializable {
    private Integer region;
    private Integer[] location;
    private Integer courier;
    private Integer[] receivingStation;
    private TariffStatus status;
}
