package greencity.dto.position;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Builder
@Data
public class AddingPositionDto {
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String name;
    @Pattern(regexp = "[A-Za-z-'\\s.]{1,30}")
    private String nameEn;
}
