package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailsOrderInfoDto {
    private String service;
    private String capacity;
    private String cost;
    private String bagAmount;
    private String sum;
}
