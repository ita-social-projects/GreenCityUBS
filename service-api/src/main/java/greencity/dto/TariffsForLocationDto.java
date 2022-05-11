package greencity.dto;

import greencity.dto.courier.CourierTranslationDto;
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
    private Long minAmountOfBigBags;
    private Long maxAmountOfBigBags;
    private Long minPriceOfOrder;
    private Long maxPriceOfOrder;
    private String courierLimit;
    private String courierStatus;
    private RegionDto regionDto;
    private List<LocationsDtos> locationsDtosList;
    private List<CourierTranslationDto> courierTranslationDtos;
}
