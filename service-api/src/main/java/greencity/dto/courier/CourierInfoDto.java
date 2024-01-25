package greencity.dto.courier;

import greencity.enums.CourierLimit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
