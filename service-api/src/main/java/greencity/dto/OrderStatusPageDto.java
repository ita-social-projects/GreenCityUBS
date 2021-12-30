package greencity.dto;

import lombok.*;
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
    private GeneralOrderInfo generalOrderInfo;
    private UserInfoDto userInfoDto;
    private AddressExportDetailsDto addressExportDetailsDto;
    private String addressComment;
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
    private Integer courierPricePerPackage;
    private CourierInfoDto courierInfo;
}
