package greencity.entity.user;


import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.ViolationLevel;
import greencity.entity.order.Order;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "violations_description_mapping")
@EqualsAndHashCode(exclude = {"description", "violation_level", "violation_date",})
@Entity
public class Violation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "description")
    private String description;

    @Column(name = "violation_date")
    private LocalDateTime violationDate;

    @Column(name = "image_path")
    private String image;

    @Column(nullable = false, name = "violation_level", length = 15)
    @Enumerated(EnumType.STRING)
    private ViolationLevel violationLevel;

}
