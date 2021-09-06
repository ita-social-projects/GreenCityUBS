package greencity.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class OrderDetailDto {
    @NotNull
    @Min(1)
    private Long orderId;
    List<BagInfoDto> capacityAndPrice;
    List<BagMappingDto> amount;
    List<BagTransDto> name;
}
