package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.UpdateEmployeeDto;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class EmployeeUpdateDtoMapperTest {

    @InjectMocks
    private EmployeeUpdateDtoMapper mapper;

    @Test
    public void convertEmployeeToUpdateEmployeeDtoTest() {
        Employee employee = ModelUtils.getEmployee();
        UpdateEmployeeDto dto = ModelUtils.getUpdateEmployeeDto();
        assertEquals(mapper.convert(employee), dto);
    }
}
