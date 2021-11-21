package greencity.entity.order;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "order_payment_status_translations")
public class OrderPaymentStatusTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "translation_value", length = 30)
    private String translationValue;

    @Column(name = "order_payment_status_id")
    private Long orderPaymentStatusId;

    @Column(name = "language_id")
    private Long languageId;
}
