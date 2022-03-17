package greencity.dto;

import greencity.entity.order.Certificate;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Builder

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderStatusForUserDto {
    private Long id;
    private LocalDateTime dateForm;
    private LocalDateTime datePaid;
    private String orderStatus;
    private String paymentStatus;
    private Double paidAmount;
    private Double orderFullPrice;
    private Double amountBeforePayment;
    private List<BagForUserDto> bags;
    private String orderComment;
    private Double bonuses;
    private Set<Certificate> certificate;
    private Set<String> additionalOrders;
    private SenderInfoDto sender;
    private AddressInfoDto address;
}
