package greencity.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailStatusDto {
    String orderStatus;
    String paymentStatus;
    String date;
}
