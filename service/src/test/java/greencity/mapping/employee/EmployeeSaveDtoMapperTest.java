package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.SaveEmployeeDto;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EmployeeSaveDtoMapperTest {

    @InjectMocks
    private EmployeeSaveDtoMapper mapper;

    @Test
    void convertEmployeeToSaveEmployeeDtoTest() {
        Employee employee = ModelUtils.getEmployee();
        SaveEmployeeDto dto = ModelUtils.getSaveEmployeeDto();
        assertEquals(mapper.convert(employee), dto);
    }
}
