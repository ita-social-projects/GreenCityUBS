package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BagForUserDto {
    private String service;
    private String serviceEng;
    private Integer capacity;
    private Integer fullPrice;
    private Integer count;
    private Integer totalPrice;
}