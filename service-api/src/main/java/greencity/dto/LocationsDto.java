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
public class LocationsDto {
    private Long locationId;
    private String locationStatus;
    private List<LocationTranslationDto> locationTranslationDtoList;
}
