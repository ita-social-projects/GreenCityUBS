package greencity.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrderPageAdminDto {
    private OrderDetailStatusRequestDto generalOrderInfo;
    private UbsCustomersDtoUpdate userInfoDto;
    private OrderAddressExportDetailsDtoUpdate addressExportDetailsDto;
    private List<EcoNumberDto> ecoNumberFromShop;
    private ExportDetailsDtoUpdate exportDetailsDto;
    private UpdateOrderDetailDto orderDetailDto;
}
