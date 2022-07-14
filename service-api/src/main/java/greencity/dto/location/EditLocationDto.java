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
    @Pattern(regexp = "^(?=.{3,40}$)([A-Z][a-z]*'?[a-z]+($|[ -](?=[A-Z])))+$",
        message = "use only English letters, a minimum of 3 to a maximum of 40 characters are allowed")
    private String nameEn;
    @Pattern(regexp = "^(?=.{3,40}$)([А-ЯЇІЄҐ][а-яіїєґ]*'?[а-яіїєґ]+($|[ -](?=[А-ЯЇІЄҐ])))+$",
        message = "use only Ukrainian letters, a minimum of 3 to a maximum of 40 characters are allowed")
    private String nameUa;
}
