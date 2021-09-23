package greencity.entity.order;
import lombok.*;

import javax.persistence.*;
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "order_status_translations")
public class OrderStatusTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "language_id")
    private Long languageId;
}