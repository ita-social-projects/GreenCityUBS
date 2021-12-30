package greencity.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
    @NotNull
    private Long locationId;
}
