package greencity.dto.bag;

import lombok.*;

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
    private Integer price;
    private String name;
    private String nameEng;
    private Integer bagAmount;
}
