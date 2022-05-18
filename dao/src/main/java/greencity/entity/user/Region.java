package greencity.entity.user;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "regions")
@EqualsAndHashCode(exclude = {"locations"})
@ToString(exclude = {"locations"})
@Entity
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "region")
    private List<Location> locations;

    @Column(name = "name_uk")
    private String ukrName;

    @Column(name = "name_en")
    private String enName;
}
