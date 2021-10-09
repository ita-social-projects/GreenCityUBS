package greencity.entity.user;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@EqualsAndHashCode(exclude = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "locations")
public class Location {
    @Id
    Long id;

    @Column(name = "location_name")
    String locationName;

    @Column(name = "min_amount_of_big_bags")
    Long minAmountOfBigBags;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lastLocation")
    List<User> user;
}
