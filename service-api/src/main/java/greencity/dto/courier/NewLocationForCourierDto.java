package greencity.dto.courier;

import greencity.dto.location.RangeDto;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NewLocationForCourierDto {
    private Long courierId;
    private Long locationId;
    private RangeDto amountOfBigBag;
    private RangeDto amountOfOrder;
}
