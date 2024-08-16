package greencity.dto.order;

import greencity.dto.address.AddressInfoDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.notification.SenderInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
}