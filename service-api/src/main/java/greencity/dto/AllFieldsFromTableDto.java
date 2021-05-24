package greencity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.json.JSONPropertyName;
import org.springframework.boot.jackson.JsonComponent;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AllFieldsFromTableDto {
    @JsonProperty("orderid")
    private Long orderId;
    @JsonProperty("order_status")
    private String orderStatus;
    @JsonProperty("order_date")
    private String orderDate;
    private String clientName;
    private String phoneNumber;
    private String email;
    private Integer violationsAmount;
    private String district;
    private String address;
    private String recipientName;
    private String phoneNumberRecipient;
    private String emailRecipient;
    private String commentToAddressForClient;
    private Integer garbageBags120Amount;
    private Integer boBags120Amount;
    private Integer boBags20Amount;
    private Long totalSumOrder;
    private String certificateNumber;
    private Integer discount;
    private Long amountDue;
    private String commentForOrderByClient;
    private String payment;
    private String dateOfExport;
    private String timeOfExport;
    private Long idOrderFromShop;
    private String receivingStation;
    private String responsibleManager;
    private String responsibleLogicMan;
    private String responsibleDriver;
    private String responsibleNavigator;
    private String commentsForOrder;
}
