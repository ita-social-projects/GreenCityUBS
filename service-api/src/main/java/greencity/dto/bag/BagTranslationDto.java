package greencity.dto.bag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import javax.validation.constraints.Min;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
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
