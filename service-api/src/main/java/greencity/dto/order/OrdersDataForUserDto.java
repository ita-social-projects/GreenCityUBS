package greencity.dto.order;

import greencity.dto.address.AddressInfoDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.notification.SenderInfoDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class OrdersDataForUserDto {
    private Long id;
    private LocalDateTime dateForm;
    private LocalDateTime datePaid;
    private String orderStatusUk;
    private String orderStatusEn;
    private String paymentStatusUk;
    private String paymentStatusEn;
    private String paymentLink;
    private LocalDateTime paymentLinkExpiry;
    private Double paidAmount;
    private Double orderFullPrice;
    private Double amountBeforePayment;
    private Double refundedBonuses;
    private Double refundedMoney;
    private List<BagForUserDto> bags;
    private String orderComment;
    private Double bonuses;
    private List<CertificateDto> certificate;
    private Set<String> additionalOrders;
    private SenderInfoDto sender;
    private AddressInfoDto address;
    private Long completedOrdersCount;
}