package greencity.entity.order;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_payment_status_translations")
public class OrderPaymentStatusTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "translation_value", length = 30)
    private String translationValue;

    @Column(name = "translation_value_eng", length = 30)
    private String translationsValueEng;

    @Column(name = "order_payment_status_id")
    private Long orderPaymentStatusId;
}
