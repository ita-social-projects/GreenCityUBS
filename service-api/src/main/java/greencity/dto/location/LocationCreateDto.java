package greencity.dto.location;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
