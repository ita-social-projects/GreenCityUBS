package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AdditionalPaymentInfoDto {
    // CHECKSTYLE:OFF
    private String bank_name;
    private String bank_country;
    private String bank_response_code;
    private String card_product;
    private String card_category;
    private Double settlement_fee;
    private String capture_status;
    private Double client_fee;
    private String ipaddress_v4;
    private Double capture_amount;
    private String card_type;
    private String reservation_data;
    private String bank_response_description;
    private Integer transaction_id;
    private String timeend;
    private String card_number;
}
