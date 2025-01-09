package greencity.dto.location;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class CoordinatesDto {
    @NotBlank
    private double latitude;
    @NotBlank
    private double longitude;
}
