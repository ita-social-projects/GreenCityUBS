package greencity.dto.bag;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BagForUserDto {
    private String service;
    private String serviceEng;
    private Integer capacity;
    private Integer fullPrice;
    private Integer count;
    private Integer totalPrice;
}