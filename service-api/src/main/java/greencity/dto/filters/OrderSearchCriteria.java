package greencity.dto.filters;

import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import lombok.Data;

@Data
public class OrderSearchCriteria {
    private OrderStatus[] orderStatus;
    private OrderPaymentStatus[] orderPaymentStatus;
    private Long[] receivingStation;
    private Long[] responsibleCallerId;
    private Long[] responsibleDriverId;
    private Long[] responsibleNavigatorId;
    private Long[] responsibleLogicManId;
    private Long[] regionId;
    private Long[] cityId;
    private Long[] districtId;
    private DateFilter orderDate;
    private DateFilter deliveryDate;
    private DateFilter paymentDate;
    private String[] regionUk;
    private String[] citiesUk;
    private String[] districtsUk;
    private String[] regionEn;
    private String[] citiesEn;
    private String[] districtsEn;
    private String[] search;
}
