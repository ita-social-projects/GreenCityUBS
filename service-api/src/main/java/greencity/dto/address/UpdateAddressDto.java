package greencity.dto.address;

import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UpdateAddressDto {
    @NotNull
    private OrderAddressExportDetailsDtoUpdate orderAddressExportDetails;
    @NotNull
    @Min(1)
    private Long orderId;
}
