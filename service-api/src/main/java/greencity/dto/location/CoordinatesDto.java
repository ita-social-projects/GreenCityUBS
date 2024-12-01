package greencity.dto.location;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

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
