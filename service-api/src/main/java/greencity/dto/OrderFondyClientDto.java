package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class OrderFondyClientDto {
    private Long orderId;
    private Integer sum;
}
