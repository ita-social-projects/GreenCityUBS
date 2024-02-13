package greencity.dto.order;

import greencity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotNull;
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