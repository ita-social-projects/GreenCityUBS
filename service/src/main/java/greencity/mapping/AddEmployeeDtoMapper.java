package greencity.mapping;

import greencity.dto.AddEmployeeDto;
import greencity.dto.EmployeeDto;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

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
                .build();
    }
}
