package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class BlockedOrderDTO {
    private Long orderId;
    private String userName;
}
