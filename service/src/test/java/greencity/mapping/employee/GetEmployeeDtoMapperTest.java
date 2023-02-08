package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.GetEmployeeDto;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class GetEmployeeDtoMapperTest {

    @InjectMocks
    private GetEmployeeDtoMapper mapper;

    @Test
    void convertEmployeeToGetEmployeeDtoTest() {
        Employee employee = ModelUtils.getFullEmployee();
        GetEmployeeDto dto = ModelUtils.getGetEmployeeDto();
        assertEquals(mapper.convert(employee), dto);
    }
}
