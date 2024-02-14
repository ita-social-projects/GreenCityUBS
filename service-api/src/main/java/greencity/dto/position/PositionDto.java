package greencity.dto.position;

import lombok.Data;
import lombok.Builder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Builder
@Data
public class PositionDto {
    @Min(1)
    private Long id;
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String name;
    @Pattern(regexp = "[A-Za-z-'\\s.]{1,30}")
    private String nameEn;
}
