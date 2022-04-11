package greencity.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
/**
 * {@inheritDoc}
 */
public class CourierTranslationDto {
    private String name;
    private String languageCode;
}
