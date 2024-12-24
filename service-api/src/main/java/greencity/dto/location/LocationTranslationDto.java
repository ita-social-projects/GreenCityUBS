package greencity.dto.location;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class LocationTranslationDto {
    private String locationName;
    private String languageCode;
}
