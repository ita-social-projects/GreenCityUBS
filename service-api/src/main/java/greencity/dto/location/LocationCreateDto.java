package greencity.dto.location;

import lombok.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class LocationCreateDto {
    private Double latitude;
    private Double longitude;
    @Valid
    @NotNull
    private List<AddLocationTranslationDto> addLocationDtoList;
    @Valid
    @NotNull
    private List<RegionTranslationDto> regionTranslationDtos;
}
