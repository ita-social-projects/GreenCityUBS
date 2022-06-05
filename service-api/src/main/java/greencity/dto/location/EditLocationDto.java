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
    @Pattern(regexp = "[A-Za-zА-Яа-яёЁЇїІіЄєҐґ ']*", message = "use only English or Ukrainian letters")
    @NotEmpty(message = "en name must not be empty")
    private String nameEn;
    @Pattern(regexp = "[A-Za-zА-Яа-яёЁЇїІіЄєҐґ ']*", message = "use only English or Ukrainian letters")
    @NotEmpty(message = "ua name must not be empty")
    private String nameUa;
}
