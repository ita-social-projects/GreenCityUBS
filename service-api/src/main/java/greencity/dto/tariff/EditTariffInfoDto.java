package greencity.dto.tariff;

import greencity.enums.CourierLimit;
import greencity.enums.MinAmountOfBag;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EditTariffInfoDto {
    private Long minAmountOfBigBag;
    private Long maxAmountOfBigBag;
    private Long minAmountOfOrder;
    private Long maxAmountOfOrder;
    private CourierLimit courierLimitsBy;
    private MinAmountOfBag minimalAmountOfBagStatus;
    private Long courierId;
    private Integer bagId;
    private String limitDescription;
    private Long locationId;
}
