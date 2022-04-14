package greencity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class GetCourierLocationDto {
    private Long courierLocationId;
    private Long minAmountOfBigBags;
    private Long maxAmountOfBigBags;
    private Long minPriceOfOrder;
    private Long maxPriceOfOrder;
    private String courierLimit;
    private List<LocationInfoDto> locationInfoDtos;
    private List<CourierDto> courierDtos;
}
