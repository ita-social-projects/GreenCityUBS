package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BagForUserDto {
    private String service;
    private Integer capacity;
    private Integer price;
    private Integer count;
    private Integer totalPrice;
}
