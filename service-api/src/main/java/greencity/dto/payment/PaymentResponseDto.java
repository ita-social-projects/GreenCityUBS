package greencity.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentResponseDto {
    // CHECKSTYLE:OFF
    @Length(max = 1024)
    private String order_id;
    private Integer merchant_id;
    private Integer amount;
    @Length(max = 3)
    private String currency;
    @Length(max = 50)
    private String order_status;
    @Length(max = 50)
    private String response_status;
    @Length(max = 40)
    private String signature;
    @Length(max = 50)
    private String tran_type;
    @Length(max = 16)
    private String sender_cell_phone;
    @Length(max = 50)
    private String sender_account;
    @Length(max = 19)
    private String masked_card;
    private Integer card_bin;
    @Length(max = 50)
    private String card_type;
    @Length(max = 50)
    private String rrn;
    @Length(max = 6)
    private String approval_code;
    private Integer response_code;
    @Length(max = 1024)
    private String response_description;
    private Integer reversal_amount;
    private Integer settlement_amount;
    @Length(max = 3)
    private String settlement_currency;
    @Length(max = 19)
    private String order_time;
    @Length(max = 10)
    private String settlement_date;
    private Integer eci;
    private Integer fee;
    @Length(max = 50)
    private String payment_system;
    @Length(max = 254)
    private String sender_email;
    private Integer payment_id;
    private Integer actual_amount;
    @Length(max = 3)
    private String actual_currency;
    @Length(max = 1024)
    private String product_id;
    @Length(max = 2048)
    private String merchant_data;
    @Length(max = 50)
    private String verification_status;
    @Length(max = 40)
    private String rectoken;
    @Length(max = 19)
    private String rectoken_lifetime;
    private Integer parent_order_id;
}
