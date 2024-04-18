package greencity.mapping.employee;

import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.order.TariffsInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class UpdateEmployeeDtoMapper extends AbstractConverter<EmployeeWithTariffsIdDto, Employee> {
    @Override
    protected Employee convert(EmployeeWithTariffsIdDto employeeWithTariffsIdDto) {
        return Employee.builder()
            .id(employeeWithTariffsIdDto.getEmployeeDto().getId())
            .firstName(employeeWithTariffsIdDto.getEmployeeDto().getFirstName())
            .lastName(employeeWithTariffsIdDto.getEmployeeDto().getLastName())
            .email(employeeWithTariffsIdDto.getEmployeeDto().getEmail())
            .tariffsInfoReceivingEmployees(new ArrayList<>())
            .phoneNumber(employeeWithTariffsIdDto.getEmployeeDto().getPhoneNumber())
            .employeePosition(employeeWithTariffsIdDto.getEmployeeDto().getEmployeePositions().stream()
                .map(positionDto -> Position.builder()
                    .id(positionDto.getId())
                    .name(positionDto.getName())
                    .nameEn(positionDto.getNameEn())
                    .build())
                .collect(Collectors.toSet()))
            .build();
    }
}
