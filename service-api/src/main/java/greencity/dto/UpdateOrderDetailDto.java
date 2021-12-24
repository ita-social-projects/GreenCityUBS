package greencity.dto;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class UpdateOrderDetailDto {
    private Map<Integer, Integer> amountOfBagsExported;
    private Map<Integer, Integer> amountOfBagsConfirmed;
}
