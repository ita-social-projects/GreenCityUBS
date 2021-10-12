package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class GetLocationTranslationDto {
    private Long id;
    private String name;
    private String locationStatus;
    private String languageCode;
}
