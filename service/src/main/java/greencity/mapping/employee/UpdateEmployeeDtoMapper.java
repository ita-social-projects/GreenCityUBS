package greencity.mapping.employee;

import greencity.dto.employee.EmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UpdateEmployeeDtoMapper extends AbstractConverter<EmployeeDto, Employee> {
    @Override
    protected Employee convert(EmployeeDto employeeDto) {
        return Employee.builder()
            .id(employeeDto.getId())
            .firstName(employeeDto.getFirstName())
            .lastName(employeeDto.getLastName())
            .email(employeeDto.getEmail())
            .phoneNumber(employeeDto.getPhoneNumber())
            .employeePosition(employeeDto.getEmployeePositions().stream().map(
                positionDto -> Position.builder()
                    .id(positionDto.getId())
                    .name(positionDto.getName())
                    .build())
                .collect(Collectors.toSet()))
            .build();
    }
}
