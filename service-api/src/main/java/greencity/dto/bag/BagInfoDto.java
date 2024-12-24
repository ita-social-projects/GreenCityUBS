package greencity.dto.bag;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class BagInfoDto {
    Integer capacity;
    Double price;
    Integer id;
    String name;
    String nameEng;
}
