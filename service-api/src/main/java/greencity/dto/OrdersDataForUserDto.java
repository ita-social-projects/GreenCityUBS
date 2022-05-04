package greencity.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrdersDataForUserDto {
    private Long id;
    private LocalDateTime dateForm;
    private LocalDateTime datePaid;
    private String orderStatus;
    private String orderStatusEng;
    private String paymentStatus;
    private String paymentStatusEng;
    private Double orderFullPrice;
    private Double orderDiscountedPrice;
    private Double orderCertificateDiscount;
    private Double orderBonusDiscount;
    private Double paidAmount;
    private Double amountToPay;
    private List<BagForUserDto> bags;
    private String orderComment;
    private List<CertificateDto> certificate;
    private Set<String> additionalOrders;
    private SenderInfoDto sender;
    private AddressInfoDto address;
}