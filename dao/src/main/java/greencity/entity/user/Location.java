package greencity.entity.user;

import greencity.entity.enums.LocationStatus;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.Courier;
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
    private Long id;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "min_amount_of_big_bags")
    private Long minAmountOfBigBags;

    @Column(name = "location_status")
    @Enumerated(EnumType.STRING)
    private LocationStatus locationStatus;

    @ManyToOne
    private Language language;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lastLocation")
    private List<User> user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    List<Service> service;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    List<Courier> courier;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    List<Bag> bags;
}
