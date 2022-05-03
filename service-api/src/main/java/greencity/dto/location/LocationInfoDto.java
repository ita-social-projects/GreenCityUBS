package greencity.dto.location;

import lombok.*;

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
