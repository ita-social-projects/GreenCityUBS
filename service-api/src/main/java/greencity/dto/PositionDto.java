package greencity.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class PositionDto {
    @Min(1)
    private Long id;
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String name;
}
