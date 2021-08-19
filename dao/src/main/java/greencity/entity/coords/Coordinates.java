package greencity.entity.coords;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Embeddable
@ToString
@EqualsAndHashCode
public class Coordinates {
    @Column(nullable = true)
    private double latitude;
    @Column(nullable = true)
    private double longitude;
}
