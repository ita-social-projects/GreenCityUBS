package greencity.dto.courier;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddingReceivingStationDto {
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z0-9-'\\s]{3,40}")
    private String name;
}
