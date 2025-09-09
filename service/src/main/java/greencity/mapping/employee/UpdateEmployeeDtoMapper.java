package greencity.mapping.employee;

import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.entity.user.employee.Employee;
import greencity.repository.PositionRepository;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
public class UpdateEmployeeDtoMapper extends AbstractConverter<EmployeeWithTariffsIdDto, Employee> {
    private final PositionRepository positionRepository;

    public UpdateEmployeeDtoMapper(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Override
    protected Employee convert(EmployeeWithTariffsIdDto employeeWithTariffsIdDto) {
        return Employee.builder()
            .id(employeeWithTariffsIdDto.getEmployeeDto().getId())
            .firstName(employeeWithTariffsIdDto.getEmployeeDto().getFirstName())
            .lastName(employeeWithTariffsIdDto.getEmployeeDto().getLastName())
            .email(employeeWithTariffsIdDto.getEmployeeDto().getEmail())
            .tariffsInfoReceivingEmployees(new ArrayList<>())
            .phoneNumber(employeeWithTariffsIdDto.getEmployeeDto().getPhoneNumber())
            .employeePosition(
                positionRepository.findByIdIn(employeeWithTariffsIdDto.getEmployeeDto().getEmployeePositionIds()))
            .build();
    }
}
