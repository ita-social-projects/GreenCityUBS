package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.OptionForColumnDTO;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class EmployeeToOptionForColumnDtoMapperTest {

    @InjectMocks
    EmployeeToOptionForColumnDtoMapper employeeToOptionForColumnDtoMapper;

    @Test
    @Transactional
    void convert() {

        Employee employee = ModelUtils.getEmployee();

        OptionForColumnDTO expected = OptionForColumnDTO.builder()
            .key("1")
            .ua("Петро Петренко")
            .en("Петро Петренко")
            .build();

        OptionForColumnDTO actual = employeeToOptionForColumnDtoMapper.convert(employee);

        Assertions.assertEquals(actual, expected);

    }
}
