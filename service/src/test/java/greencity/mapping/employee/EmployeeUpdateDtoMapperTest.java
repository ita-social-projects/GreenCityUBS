package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
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
        EmployeeWithTariffsIdDto dto = ModelUtils.getEmployeeWithTariffsIdDto();
        assertEquals(mapper.convert(employee), dto);
    }
}
