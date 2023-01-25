package greencity.dto;

import greencity.dto.courier.CourierDto;
import lombok.*;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class TariffsForLocationDto {
    private Long tariffInfoId;
    private Long min;
    private Long max;
    private String courierLimit;
    private RegionDto regionDto;
    private List<LocationsDtos> locationsDtosList;
    private CourierDto courierDto;
    private String limitDescription;
}
