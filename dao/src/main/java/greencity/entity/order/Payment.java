package greencity.entity.order;

import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payment")
@EqualsAndHashCode(exclude = {"order"})
@ToString
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 3, nullable = false)
    private String currency;
    @Column(length = 12, nullable = false)
    private Long amount;
    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    @Column(length = 50)
    private String responseStatus;
    @Column(length = 16)
    private String senderCellPhone;
    @Column(length = 50)
    private String senderAccount;
    @Column(length = 19)
    private String maskedCard;
    @Column(length = 50)
    private String cardType;
    @Column(length = 4)
    private Integer responseCode;
    @Column(length = 1024)
    private String responseDescription;
    @Column(length = 19)
    private String orderTime;
    @Column(length = 10)
    private String settlementDate;
    @Column(length = 12)
    private Long fee;
    @Column(length = 50)
    private String paymentSystem;
    @Column(length = 254)
    private String senderEmail;
    @Column
    private String receiptLink;
    @Column
    private String imagePath;
    @Column
    private String paymentId;
    @ManyToOne
    private Order order;
    @Column
    private String comment;
    @Column(name = "payment_type")
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    @Column(name = "payment_status", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}
