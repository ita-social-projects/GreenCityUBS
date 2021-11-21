package greencity.dto;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class OrderStatusPageDto {
    private Long id;
    private LocalDateTime dateFormed;
    private List<OrderStatusesTranslationDto> orderStatusesDto;
    private List<OrderPaymentStatusesTranslationDto> orderPaymentStatusesDto;
    private UserInfoDto userInfoDto;
    private String addressCity;
    private String addressStreet;
    private String addressDistrict;
    private Long addressHouseNumber;
    private Long addressHouseCorpus;
    private Long addressEntranceNumber;
    private String addressRegion;
    private String addressComment;
    private OrderStatus orderStatus;
    private String orderStatusName;
    private OrderPaymentStatus orderPaymentStatus;
    private String orderPaymentStatusName;
    private double orderFullPrice;
    private double orderDiscountedPrice;
    private double orderCertificateTotalDiscount;
    private double orderBonusDiscount;
    private double orderExportedPrice;
    private double orderExportedDiscountedPrice;
    private Map<Integer, Integer> amountOfBagsOrdered;
    private List<BagInfoDto> bags;
    private Map<Integer, Integer> amountOfBagsExported;
    private Map<Integer, Integer> amountOfBagsConfirmed;
    private Set<String> numbersFromShop;
    private List<String> certificates;
    private PaymentTableInfoDto paymentTableInfoDto;
    private ExportDetailsDto exportDetailsDto;
    private EmployeePositionDtoRequest employeePositionDtoRequest;
    private String comment;
}
