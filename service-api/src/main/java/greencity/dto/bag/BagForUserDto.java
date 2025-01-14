package greencity.dto.bag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BagForUserDto {
    private String service;
    private String serviceEng;
    private Integer capacity;
    private Double fullPrice;
    private Integer count;
    private Double totalPrice;
}