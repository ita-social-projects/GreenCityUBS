package greencity.dto.order;

import greencity.entity.enums.OrderStatus;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class OrderClientForDto {
    @NotNull
    @Length(min = 1)
    private Long id;
    private OrderStatus orderStatus;
    private Long counter;
    private List<Integer> amount;
}