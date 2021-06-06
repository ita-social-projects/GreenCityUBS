package greencity.dto;

import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class BagTranslationDto implements Serializable {
    @Min(1)
    private Integer id;
    @NonNull
    private String name;
    @NonNull
    private Integer capacity;
    @NonNull
    private Integer price;
    @NotNull
    private String code;
}
