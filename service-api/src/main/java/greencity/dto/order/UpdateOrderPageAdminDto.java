package greencity.dto.order;

import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UpdateResponsibleEmployeeDto;
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
    private EcoNumberDto ecoNumberFromShop;
    private ExportDetailsDtoUpdate exportDetailsDto;
    private UpdateOrderDetailDto orderDetailDto;
    private List<UpdateResponsibleEmployeeDto> updateResponsibleEmployeeDto;
    private Long writeOffStationSum;

}
