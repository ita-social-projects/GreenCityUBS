package greencity.dto.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class CoordinatesDto implements Serializable {
    @NotNull
    @JsonProperty("lat")
    private Double latitude;
    @NotNull
    @JsonProperty("lng")
    private Double longitude;
}
