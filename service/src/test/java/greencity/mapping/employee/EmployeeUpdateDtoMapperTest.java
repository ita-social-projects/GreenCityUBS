package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeDto;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EmployeeUpdateDtoMapperTest {

    @InjectMocks
    private EmployeeUpdateDtoMapper mapper;

    @Test
    void convertEmployeeToEmployeeDtoTest() {
        Employee employee = ModelUtils.getEmployee();
        EmployeeDto dto = ModelUtils.getEmployeeDto();
        assertEquals(mapper.convert(employee), dto);
    }
}
