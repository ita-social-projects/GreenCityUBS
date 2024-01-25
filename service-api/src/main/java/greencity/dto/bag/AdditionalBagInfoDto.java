package greencity.dto.bag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class AdditionalBagInfoDto {
    @JsonProperty("recipient_name")
    private String recipientName;
    @JsonProperty("recipient_phone")
    private String recipientPhone;
    @JsonProperty("recipient_email")
    private String recipientEmail;
    @JsonProperty("city")
    private String city;
    @JsonProperty("street")
    private String street;
    @JsonProperty("house_number")
    private String houseNumber;
    @JsonProperty("district")
    private String district;
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("pay_id")
    private String paymentId;
}
