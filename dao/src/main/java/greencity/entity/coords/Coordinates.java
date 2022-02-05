package greencity.entity.coords;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Embeddable
@ToString
@EqualsAndHashCode
public class Coordinates implements Serializable {
    @Column(nullable = true)
    private double latitude;
    @Column(nullable = true)
    private double longitude;
}
