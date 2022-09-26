package greencity.entity.coords;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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
