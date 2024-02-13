package greencity.dto.order;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("clientName")
    private String clientName;
    @JsonProperty("recipientPhone")
    private String phone;
    @JsonProperty("recipientEmail")
    private String email;
    @JsonProperty("dateOfRegistration")
    private String registrationDate;
    @JsonProperty("orderDate")
    private String lastOrderDate;
    @JsonProperty("number_of_orders")
    private int numberOfOrders;
    @JsonProperty("violations")
    private int violation;
    @JsonProperty("currentPoints")
    private String userBonuses;
}
