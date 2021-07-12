package greencity.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class DetailsOrderInfoDto {
    private String service;
    private String capacity;
    private String cost;
    private String bagAmount;
    private String sum;
}
