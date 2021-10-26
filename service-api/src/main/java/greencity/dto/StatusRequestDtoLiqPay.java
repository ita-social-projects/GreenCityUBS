package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class StatusRequestDtoLiqPay {
    private Integer version;
    private String publicKey;
    private String action;
    private String orderId;
}
