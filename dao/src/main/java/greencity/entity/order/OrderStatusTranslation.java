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

    @Column(name = "name_eng", length = 30)
    private String nameEng;

    @Column(name = "status_id")
    private Long statusId;
}