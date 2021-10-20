package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EditTariffDescriptionTranslationDto {
    private String limitDescription;
    private Long languageId;
}
