package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Map;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class UpdateOrderDetailDto {
    private Map<Integer, Integer> amountOfBagsExported;
    private Map<Integer, Integer> amountOfBagsConfirmed;
    @Length(min = 10, max = 255)
    private String userComment;
}
