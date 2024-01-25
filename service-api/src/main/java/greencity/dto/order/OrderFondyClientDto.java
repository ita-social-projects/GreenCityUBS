package greencity.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class OrderFondyClientDto {
    private Long orderId;
    @NotNull
    @Min(0)
    private Integer pointsToUse;
    private Set<@Pattern(regexp = "(\\d{4}-\\d{4})|(^$)",
        message = "This certificate code is not valid") String> certificates;
}
