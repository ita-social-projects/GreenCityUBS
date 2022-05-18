package greencity.dto.order;

import greencity.entity.enums.OrderStatus;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class OrderClientDto {
    @NotNull
    @Length(min = 1)
    private Long id;
    private OrderStatus orderStatus;
    private Long amount;
}