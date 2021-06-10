package greencity.mapping;

import greencity.dto.EmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.ReceivingStation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link EmployeeDto} into
 * {@link Employee}.
 */
@Component
public class EmployeeDtoMapping extends AbstractConverter<EmployeeDto, Employee> {
    /**
     * Method convert {@link EmployeeDto} to {@link Employee}.
     *
     * @return {@link Employee}
     */
    @Override
    protected Employee convert(EmployeeDto employeeDto) {
        return Employee.builder()
                .firstName(employeeDto.getFirstName())
                .lastName(employeeDto.getLastName())
                .phoneNumber(employeeDto.getPhoneNumber())
                .email(employeeDto.getEmail())
                .build();
    }
}
