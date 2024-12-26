package greencity.dto.order;

import greencity.dto.bag.BagInfoDto;
import greencity.dto.bag.BagMappingDto;
import greencity.dto.bag.BagTransDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
