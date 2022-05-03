package greencity.dto.location;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class AddLocationTranslationDto {
    @Pattern(regexp = "[A-Za-zА-Яа-яёЁЇїІіЄєҐґ ']*", message = "use only English,Ukrainian or Russian letters")
    @NotEmpty(message = "name must not be empty")
    private String locationName;
    @Pattern(regexp = "[A-Za-zА']*", message = "use only English letters")
    @NotEmpty(message = "language code must not be empty")
    private String languageCode;
}
