package greencity.mapping.employee;

import greencity.dto.employee.AddEmployeeDto;
import greencity.enums.EmployeeStatus;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link AddEmployeeDto} into
 * {@link Employee}.
 */
@Component
public class AddEmployeeDtoMapper extends AbstractConverter<AddEmployeeDto, Employee> {
    /**
     * Method convert {@link AddEmployeeDto} to {@link Employee}.
     *
     * @return {@link Employee}
     */
    @Override
    protected Employee convert(AddEmployeeDto dto) {
        return Employee.builder()
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .phoneNumber(dto.getPhoneNumber())
            .email(dto.getEmail())
            .employeeStatus(EmployeeStatus.ACTIVE)
            .employeePosition(dto.getEmployeePositions().stream()
                .map(p -> Position.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .nameEN(p.getNameEN())
                    .build())
                .collect(Collectors.toSet()))
            .build();
    }
}
