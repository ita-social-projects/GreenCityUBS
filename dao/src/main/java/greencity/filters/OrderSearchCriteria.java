package greencity.filters;

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
    private DateFilter orderDate;
    private DateFilter deliveryDate;
    private DateFilter paymentDate;
    private String[] region;
    private String[] cities;
    private String[] districts;
    private String[] regionEn;
    private String[] citiesEn;
    private String[] districtsEn;
    private String[] search;
    private Long[] regionId;
    private Long[] citiesId;
    private Long[] districtsId;
}
