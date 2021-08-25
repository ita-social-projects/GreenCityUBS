package greencity.entity.user;

import lombok.*;

import javax.persistence.*;

@Entity
@EqualsAndHashCode
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

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "lastLocation")
    User user;
}
