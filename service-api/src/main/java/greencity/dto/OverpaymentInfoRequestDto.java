package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OverpaymentInfoRequestDto {
    Long bonuses;
    Long overpayment;
    String comment;
}
