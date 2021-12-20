package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NewLocationForCourierDto {
    private Long courierId;
    private Long LocationId;
    private Long minAmountOfBigBag;
    private Long maxAmountOfBigBag;
    private Long minAmountOfOrder;
    private Long maxAmountOfOrder;
}
