package greencity.dto.location;

import lombok.*;

import javax.validation.constraints.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EditLocationDto {
    @NotNull
    @Min(1)
    private Long locationId;
    @Pattern(regexp = "^([A-Z][a-z]{0,39}'?[a-z]{1,39}($|[ -](?=[A-Z]))){1,10}$",
        message = "use only English letters")
    @Size(min = 3, max = 40, message = "a minimum of 3 to a maximum of 40 characters are allowed")
    private String nameEn;
    @Pattern(regexp = "^([А-ЯЇІЄҐ][а-яіїєґ]{0,39}'?[а-яіїєґ]{1,39}($|[ -](?=[А-ЯЇІЄҐ]))){1,10}$",
        message = "use only Ukrainian letters")
    @Size(min = 3, max = 40, message = "a minimum of 3 to a maximum of 40 characters are allowed")
    private String nameUa;
}
