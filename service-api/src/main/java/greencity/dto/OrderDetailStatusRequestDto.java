package greencity.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailStatusRequestDto {
    String orderStatus;
    String paymentStatus;
    String orderComment;
}