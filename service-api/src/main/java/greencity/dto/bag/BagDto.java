package greencity.dto.bag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BagDto implements Serializable {
    @NotNull
    @Min(1)
    private Integer id;
    @NotNull
    @Range(min = 1, max = 999)
    private Integer amount;
}
