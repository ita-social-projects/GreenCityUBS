package greencity.dto.bag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class BagOrderDto {
    private Integer bagId;
    private Integer capacity;
    private Double price;
    private String name;
    private String nameEng;
    private Integer bagAmount;
}
