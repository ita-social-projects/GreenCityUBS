package greencity.entity.user;

import greencity.entity.order.Service;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "location_name")
    String locationName;

    @Column(name = "min_amount_of_big_bags")
    Long minAmountOfBigBags;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lastLocation")
    List<User> user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    List<Service> service;
}
