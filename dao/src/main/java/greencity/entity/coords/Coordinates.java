package greencity.entity.coords;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Coordinates implements Serializable {
    @Column
    private double latitude;
    @Column
    private double longitude;
}
