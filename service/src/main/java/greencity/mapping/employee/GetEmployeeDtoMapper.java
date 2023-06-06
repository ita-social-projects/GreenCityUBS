package greencity.mapping.employee;

import greencity.dto.employee.GetEmployeeDto;
import greencity.entity.user.employee.EmployeeFilterView;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class GetEmployeeDtoMapper extends AbstractConverter<EmployeeFilterView, GetEmployeeDto> {
    @Override
    protected GetEmployeeDto convert(EmployeeFilterView source) {
        return GetEmployeeDto.builder()
            .id(source.getEmployeeId())
            .firstName(source.getFirstName())
            .lastName(source.getLastName())
            .phoneNumber(source.getPhoneNumber())
            .email(source.getEmail())
            .image(source.getImage())
            .build();
    }
}
