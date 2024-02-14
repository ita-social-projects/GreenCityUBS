package greencity.dto;

import greencity.dto.courier.CourierDto;
import greencity.enums.CourierLimit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
    private CourierLimit courierLimit;
    private RegionDto regionDto;
    private List<LocationsDtos> locationsDtosList;
    private CourierDto courierDto;
    private String limitDescription;
}
