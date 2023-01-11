package greencity.mapping.employee;

import greencity.dto.employee.SaveEmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.EmployeeStatus;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SaveEmployeeDtoMapper extends AbstractConverter<SaveEmployeeDto, Employee> {
    @Override
    protected Employee convert(SaveEmployeeDto dto) {
        return Employee.builder()
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .phoneNumber(dto.getPhoneNumber())
            .email(dto.getEmail())
            .imagePath(dto.getImage())
            .employeeStatus(EmployeeStatus.ACTIVE)
            .employeePosition(dto.getEmployeePositions().stream()
                .map(p -> Position.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .build())
                .collect(Collectors.toSet()))
            .build();
    }
}
