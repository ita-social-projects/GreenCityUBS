package greencity.mapping;

import greencity.dto.TitleDto;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Employee} into
 * {@link TitleDto}.
 */
@Component
public class EmployeeToTitleDtoMapper extends AbstractConverter<Employee, TitleDto> {
    /**
     * Method convert {@link Employee} to {@link TitleDto}.
     *
     * @return {@link TitleDto}
     */
    @Override
    protected TitleDto convert(Employee employee) {
        return TitleDto.builder()
            .key(employee.getId().toString())
            .ua(String.format("%s %s", employee.getFirstName(), employee.getLastName()))
            .en(String.format("%s %s", employee.getFirstName(), employee.getLastName()))
            .build();
    }
}
