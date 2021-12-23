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
public class FindInfoAboutLocationDto {
    private Long regionId;
    private List<RegionTranslationDto> regionTranslationDtos;
    private List<LocationsDto> locationsDto;
}
