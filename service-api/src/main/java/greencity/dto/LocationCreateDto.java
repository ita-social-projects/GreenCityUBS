package greencity.dto;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
