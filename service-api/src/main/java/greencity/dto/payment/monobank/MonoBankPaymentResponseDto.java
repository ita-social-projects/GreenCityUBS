package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MonoBankPaymentResponseDto {
    @NotBlank
    private String invoiceId;
    @NotBlank
    private String status;
    private String failureReason;
    @JsonProperty("errCode")
    private String errorCode;
    private Integer amount;
    @JsonProperty("ccy")
    private Integer currency;
    private String createdDate;
    private String modifiedDate;
    @JsonProperty("reference")
    private String orderReference;
    private PaymentInfo paymentInfo;
}
