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
    @JsonProperty("order_id")
    private Long orderId;
    @JsonProperty("order_status")
    private String orderStatus;
    @JsonProperty("payment_status")
    private String paymentStatus;
    @JsonProperty("order_date")
    private String orderDate;
    @JsonProperty("payment_date")
    private String paymentDate;
    @JsonProperty("client_name")
    private String clientName;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("email")
    private String email;
    @JsonProperty("sender_name")
    private String senderName;
    @JsonProperty("sender_phone")
    private String senderPhone;
    @JsonProperty("sender_email")
    private String senderEmail;
    @JsonProperty("violations")
    private Integer violationsAmount;
    @JsonProperty("location")
    private String location;
    @JsonProperty("district")
    private String district;
    @JsonProperty("address")
    private String address;
    @JsonProperty("comment_to_address_for_client")
    private String commentToAddressForClient;
    @JsonProperty("bags_amount")
    private Integer bagsAmount;
    @JsonProperty("total_order_sum")
    private Long totalOrderSum;
    @JsonProperty("order_certificate_code")
    private String orderCertificateCode;
    @JsonProperty("order_certificate_points")
    private String orderCertificatePoints;
    @JsonProperty("amount_due")
    private Long amountDue;
    @JsonProperty("comment_for_order_by_client")
    private String commentForOrderByClient;
    @JsonProperty("payment")
    private String payment;
    @JsonProperty("date_of_export")
    private String dateOfExport;
    @JsonProperty("time_of_export")
    private String timeOfExport;
    @JsonProperty("id_order_from_shop")
    private String idOrderFromShop;
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
