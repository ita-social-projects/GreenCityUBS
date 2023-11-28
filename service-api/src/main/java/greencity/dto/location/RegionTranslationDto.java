package greencity.dto.location;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class RegionTranslationDto {
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ '-]*", message = "use only English or Ukrainian letters")
    @NotEmpty(message = "name must not be empty")
    private String regionName;
    @Pattern(regexp = "[A-Za-zА']*", message = "use only English letters")
    @NotEmpty(message = "language code must not be empty")
    private String languageCode;
}
