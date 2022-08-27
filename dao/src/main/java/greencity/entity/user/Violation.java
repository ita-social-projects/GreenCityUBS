package greencity.entity.user;

import greencity.entity.enums.ViolationLevel;
import greencity.entity.order.Order;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "violations_description_mapping")
@EqualsAndHashCode(exclude = {"description", "violationLevel", "violationDate",})
@Entity
public class Violation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "description")
    private String description;

    @Column(name = "violation_date")
    private ZonedDateTime violationDate;

    @ElementCollection
    @CollectionTable(name = "violation_images",
        joinColumns = {@JoinColumn(name = "violation_id", referencedColumnName = "id")})
    @Column(name = "image")
    private List<String> images;

    @Column(nullable = false, name = "violation_level", length = 15)
    @Enumerated(EnumType.STRING)
    private ViolationLevel violationLevel;

    @ManyToOne
    @JoinColumn(name = "added_by_user_id")
    private User addedByUser;
}
