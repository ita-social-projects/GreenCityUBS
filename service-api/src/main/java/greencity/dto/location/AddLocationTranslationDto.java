package greencity.dto.location;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class AddLocationTranslationDto {
    @Pattern(regexp = "[A-Za-zА-Яа-яёЁЇїІіЄєҐґ '-]*", message = "use only English or Ukrainian letters")
    @NotEmpty(message = "name must not be empty")
    private String locationName;
    @Pattern(regexp = "[A-Za-zА']*", message = "use only English letters")
    @NotEmpty(message = "language code must not be empty")
    private String languageCode;
}
