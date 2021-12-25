package greencity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LocationCreateDto {
    private List<AddLocationTranslationDto> addLocationDtoList;
}
