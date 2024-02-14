package greencity.dto.location;

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
public class LocationInfoDto {
    private Long regionId;
    private List<RegionTranslationDto> regionTranslationDtos;
    private List<LocationsDto> locationsDto;
}
