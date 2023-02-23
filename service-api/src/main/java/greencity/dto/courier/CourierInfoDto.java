package greencity.dto.courier;

import greencity.enums.CourierLimit;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
/**
 * {@inheritDoc}
 */
public class CourierInfoDto {
    Long min;
    Long max;
    CourierLimit courierLimit;
}
