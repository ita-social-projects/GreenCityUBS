package greencity.mapping.employee;

import greencity.dto.employee.UpdateEmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.EmployeeStatus;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UpdateEmployeeDtoMapper extends AbstractConverter<UpdateEmployeeDto, Employee> {
    @Override
    protected Employee convert(UpdateEmployeeDto updateEmployeeDto) {
        return Employee.builder()
            .id(updateEmployeeDto.getId())
            .firstName(updateEmployeeDto.getFirstName())
            .lastName(updateEmployeeDto.getLastName())
            .email(updateEmployeeDto.getEmail())
            .phoneNumber(updateEmployeeDto.getPhoneNumber())
            .employeeStatus(EmployeeStatus.ACTIVE)
            .employeePosition(updateEmployeeDto.getEmployeePositions().stream().map(
                positionDto -> Position.builder()
                    .id(positionDto.getId())
                    .name(positionDto.getName())
                    .build())
                .collect(Collectors.toSet()))
            .build();
    }
}
