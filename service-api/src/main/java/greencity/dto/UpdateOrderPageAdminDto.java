package greencity.dto;

import lombok.*;

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
    private EcoNumberDto ecoNumberFromShop;
    private ExportDetailsDtoUpdate exportDetailsDto;
    private UpdateOrderDetailDto orderDetailDto;
}
