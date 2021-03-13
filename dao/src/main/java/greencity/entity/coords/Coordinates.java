package greencity.entity.coords;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
