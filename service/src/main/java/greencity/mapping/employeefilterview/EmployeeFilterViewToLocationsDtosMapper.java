package greencity.mapping.employeefilterview;

import greencity.dto.LocationsDtos;
import greencity.entity.user.employee.EmployeeFilterView;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EmployeeFilterViewToLocationsDtosMapper extends AbstractConverter<EmployeeFilterView, LocationsDtos> {
    @Override
    protected LocationsDtos convert(EmployeeFilterView employeeFilterView) {
        return LocationsDtos.builder()
            .locationId(employeeFilterView.getLocationId())
            .nameEn(employeeFilterView.getLocationNameEn())
            .nameUk(employeeFilterView.getLocationNameUk())
            .build();
    }
}
