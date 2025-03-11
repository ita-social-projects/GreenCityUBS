package greencity.entity.user.ubs;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.locations.City;
import greencity.entity.user.locations.District;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
@Getter
@Setter
@Builder
@Table(name = "address")
@ToString(exclude = {"user"})
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @Embedded
    private Coordinates coordinates;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region regionId;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City cityId;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District districtId;

    @Embedded
    private BaseAddress baseAddress;
}
