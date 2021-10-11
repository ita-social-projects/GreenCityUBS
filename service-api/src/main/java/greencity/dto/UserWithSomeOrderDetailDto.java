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
@ToString
@EqualsAndHashCode
@JsonSerialize
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserWithSomeOrderDetailDto {
    @JsonProperty("client_name")
    private String clientName;
    @JsonProperty("phone_number")
    private String phone;
    @JsonProperty("email")
    private String email;
    @JsonProperty("registration_date")
    private String registrationDate;
    @JsonProperty("last_order_date")
    private String lastOrderDate;
    @JsonProperty("number_of_orders")
    private int numberOfOrders;
    @JsonProperty("user_violation")
    private int violation;
    @JsonProperty("user_bonus_count")
    private String userBonuses;
}
