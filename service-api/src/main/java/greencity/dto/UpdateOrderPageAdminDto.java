package greencity.dto;

import lombok.*;

import java.util.List;

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
