package greencity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddLocationDto {
    private List<AddLocationTranslationDto> addLocationDtoList;
}
