package greencity.dto.order;

import greencity.annotations.TimeDeliveryOrder;
import greencity.constant.ErrorMessage;
import greencity.constant.ValidationConstant;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@TimeDeliveryOrder
public class ExportDetailsDtoUpdate {
    @NotNull(message = "Export date cannot be null")
    private String dateExport;

    @NotNull(message = "Time delivery from cannot be null")
    @Pattern(regexp = ValidationConstant.DELIVERY_TIME_REGEX, message = ErrorMessage.INVALID_DELIVERY_TIME_FORMAT)
    private String timeDeliveryFrom;

    @NotNull(message = "Time delivery to cannot be null")
    @Pattern(regexp = ValidationConstant.DELIVERY_TIME_REGEX, message = ErrorMessage.INVALID_DELIVERY_TIME_FORMAT)
    private String timeDeliveryTo;

    private Long receivingStationId;
}
