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
    private OrderDetailStatusRequestDto orderDetailStatusRequestDto;
    private UbsCustomersDtoUpdate ubsCustomersDtoUpdate;
    private OrderAddressExportDetailsDtoUpdate orderAddressExportDetailsDtoUpdate;
    private List<EcoNumberDto> ecoNumberFromShop;
    private ExportDetailsDtoUpdate exportDetailsDtoUpdate;
    private List<UpdateOrderDetailDto> updateOrderDetailDto;
}
