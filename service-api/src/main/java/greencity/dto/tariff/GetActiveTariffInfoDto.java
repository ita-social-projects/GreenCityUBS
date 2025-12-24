package greencity.dto.tariff;

import greencity.dto.location.LocationsForTariffDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetActiveTariffInfoDto {
    private Long id;
    private String tariffNameUk;
    private String tariffNameEn;
    private List<LocationsForTariffDto> tariffLocations;
}
