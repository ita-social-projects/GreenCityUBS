package greencity.filters;

import greencity.enums.TariffStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
