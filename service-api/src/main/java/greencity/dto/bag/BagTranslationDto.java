package greencity.dto.bag;

import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.NonNull;
import javax.validation.constraints.Min;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class BagTranslationDto implements Serializable {
    @Min(1)
    private Integer id;
    @NonNull
    private String name;
    @NonNull
    private Integer capacity;
    @NonNull
    private Double price;
    @NonNull
    private String nameEng;
    @NonNull
    private Boolean limitedIncluded;
    private Integer quantity;
}
