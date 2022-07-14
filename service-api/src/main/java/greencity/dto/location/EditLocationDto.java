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
    @Pattern(regexp = "^([A-Z][a-z]*'?[a-z]+($|[ -](?=[A-Z])))+$",
        message = "use only English letters")
    @Size(min = 3, max = 40, message = "a minimum of 3 to a maximum of 40 characters are allowed")
    private String nameEn;
    @Pattern(regexp = "^([А-ЯЇІЄҐ][а-яіїєґ]*'?[а-яіїєґ]+($|[ -](?=[А-ЯЇІЄҐ])))+$",
        message = "use only Ukrainian letters")
    @Size(min = 3, max = 40, message = "a minimum of 3 to a maximum of 40 characters are allowed")
    private String nameUa;
}
