package greencity.dto.location;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CoordinatesDto {
    @NotBlank
    private double latitude;
    @NotBlank
    private double longitude;

    private static final double DELTA = 0.001;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CoordinatesDto that = (CoordinatesDto) o;
        return Math.abs(this.latitude - that.latitude) <= DELTA
            && Math.abs(this.longitude - that.longitude) <= DELTA;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
