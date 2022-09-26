package greencity.entity.order;

import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"user", "order"})
@Table(name = "change_of_points")
public class ChangeOfPoints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private Integer amount;

    @Column
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
