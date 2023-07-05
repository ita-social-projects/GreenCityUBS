package greencity.mapping.employeefilterview;

import greencity.dto.employee.GetEmployeeDto;
import greencity.entity.user.employee.EmployeeFilterView;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EmployeeFilterViewToGetEmployeeDtoMapper extends AbstractConverter<EmployeeFilterView, GetEmployeeDto> {
    @Override
    protected GetEmployeeDto convert(EmployeeFilterView employeeFilterView) {
        return GetEmployeeDto.builder()
            .id(employeeFilterView.getEmployeeId())
            .firstName(employeeFilterView.getFirstName())
            .lastName(employeeFilterView.getLastName())
            .phoneNumber(employeeFilterView.getPhoneNumber())
            .email(employeeFilterView.getEmail())
            .image(employeeFilterView.getImage())
            .employeeStatus(employeeFilterView.getEmployeeStatus())
            .build();
    }
}
