package greencity.dto.bag;

import lombok.*;

import javax.validation.constraints.Min;
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
    @NonNull
    private String nameEng;
    @NonNull
    private Long locationId;
}
