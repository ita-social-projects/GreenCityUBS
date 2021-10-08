package greencity.mapping;

import greencity.dto.OptionForColumnDTO;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Employee} into
 * {@link OptionForColumnDTO}.
 */
@Component
public class EmployeeToOptionForColumnDtoMapper extends AbstractConverter<Employee, OptionForColumnDTO> {
    /**
     * Method convert {@link Employee} to {@link OptionForColumnDTO}.
     *
     * @return {@link OptionForColumnDTO}
     */
    @Override
    protected OptionForColumnDTO convert(Employee employee) {
        return OptionForColumnDTO.builder()
            .key(employee.getId().toString())
            .ua(String.format("%s %s", employee.getFirstName(), employee.getLastName()))
            .en(String.format("%s %s", employee.getFirstName(), employee.getLastName()))
            .build();
    }
}
