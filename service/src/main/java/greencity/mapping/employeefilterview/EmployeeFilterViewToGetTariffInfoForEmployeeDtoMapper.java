package greencity.mapping.employeefilterview;

import greencity.dto.RegionDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.user.employee.EmployeeFilterView;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EmployeeFilterViewToGetTariffInfoForEmployeeDtoMapper
    extends AbstractConverter<EmployeeFilterView, GetTariffInfoForEmployeeDto> {

    @Override
    protected GetTariffInfoForEmployeeDto convert(EmployeeFilterView employeeFilterView) {
        return GetTariffInfoForEmployeeDto.builder()
            .id(employeeFilterView.getTariffsInfoId())
            .region(RegionDto.builder()
                .regionId(employeeFilterView.getRegionId())
                .nameEn(employeeFilterView.getRegionNameEn())
                .nameUk(employeeFilterView.getRegionNameUk())
                .build())
            .courier(CourierTranslationDto.builder()
                .id(employeeFilterView.getCourierId())
                .nameUk(employeeFilterView.getCourierNameUk())
                .nameEn(employeeFilterView.getCourierNameEn())
                .build())
            .build();
    }
}
