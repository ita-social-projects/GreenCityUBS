package greencity.dto.location;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EditLocationDto {
    @NotNull
    @Min(1)
    private Long locationId;
    @Pattern(regexp = "^([A-Z]{1}[a-z]{2,}([\\-| ]{1}[A-Z]{1}[a-zA-Z]{1,})?)",
        message = "use only English letters")
    @NotEmpty(message = "en name must not be empty")
    private String nameEn;
    @Pattern(regexp = "^([А-ЯЇІЄҐ]{1}[а-яіїєґ]{1,}[ -]?[А-ЯЇІЄҐ]{1}[а-яіїєґ]{1,})",
        message = "use only Ukrainian letters")
    @NotEmpty(message = "ua name must not be empty")
    private String nameUa;
}
