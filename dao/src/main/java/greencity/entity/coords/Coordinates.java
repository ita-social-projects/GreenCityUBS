package greencity.entity.coords;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Coordinates implements Serializable {
    @Column(nullable = true)
    private double latitude;
    @Column(nullable = true)
    private double longitude;
}
