package greencity.dto.bag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
