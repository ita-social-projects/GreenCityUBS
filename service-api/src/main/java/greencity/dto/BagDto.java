package greencity.dto;

import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
