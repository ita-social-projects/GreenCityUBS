package greencity.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonSerialize
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllFieldsFromTableDto {
    @JsonProperty("orderid")
    private Long orderId;
    @JsonProperty("order_status")
    private String orderStatus;
    @JsonProperty("order_date")
    private String orderDate;
    @JsonProperty("clientname")
    private String clientName;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("email")
    private String email;
    @JsonProperty("violations")
    private Integer violationsAmount;
    @JsonProperty("district")
    private String district;
    @JsonProperty("address")
    private String address;
    @JsonProperty("recipient_name")
    private String recipientName;
    @JsonProperty("recipient_phone")
    private String phoneNumberRecipient;
    @JsonProperty("recipient_email")
    private String emailRecipient;
    @JsonProperty("comment_to_address_for_client")
    private String commentToAddressForClient;
    @JsonProperty("garbage_bags_120_amount")
    private Integer garbageBags120Amount;
    @JsonProperty("bo_bags_120_amount")
    private Integer boBags120Amount;
    @JsonProperty("bo_bags_20_amount")
    private Integer boBags20Amount;
    @JsonProperty("total_order_sum")
    private Long totalSumOrder;
    @JsonProperty("code")
    private String certificateNumber;
    @JsonProperty("points")
    private Integer discount;
    @JsonProperty("amount_due")
    private Long amountDue;
    @JsonProperty("comment_for_order_by_client")
    private String commentForOrderByClient;
    @JsonProperty("payment_system")
    private String payment;
    @JsonProperty("date_of_export")
    private String dateOfExport;
    @JsonProperty("time_of_export")
    private String timeOfExport;
    @JsonProperty("id_order_from_shop")
    private Long idOrderFromShop;
    @JsonProperty("receiving_station")
    private String receivingStation;
    @JsonProperty("responsible_manager")
    private String responsibleManager;
    @JsonProperty("responsible_logic_man")
    private String responsibleLogicMan;
    @JsonProperty("responsible_driver")
    private String responsibleDriver;
    @JsonProperty("responsible_navigator")
    private String responsibleNavigator;
    @JsonProperty("comments_for_order")
    private String commentsForOrder;
}
