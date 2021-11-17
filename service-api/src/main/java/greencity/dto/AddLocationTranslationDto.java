package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddLocationTranslationDto {
    private String locationName;
    private String region;
    private Long languageId;
}
