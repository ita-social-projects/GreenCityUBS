package greencity.dto.courier;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class CreateCourierDto {
    @NotNull
    @Pattern(regexp = "[A-Za-zА0-9'\\s]{1,30}",
        message = "use English letters, no longer than 30 symbols")
    private String nameEn;

    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA0-9'\\s]{1,30}",
        message = "use Ukrainian letters, no longer than 30 symbols")
    private String nameUk;
}
