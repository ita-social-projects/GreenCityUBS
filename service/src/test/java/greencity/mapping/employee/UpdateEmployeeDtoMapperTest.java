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
class UpdateEmployeeDtoMapperTest {

    @InjectMocks
    private UpdateEmployeeDtoMapper mapper;

    @Test
    void convertEmployeeDtoToEmployeeTest() {
        Employee employee = ModelUtils.getFullEmployee();
        EmployeeDto dto = ModelUtils.getEmployeeDto();
        assertEquals(mapper.convert(dto).getFirstName(), employee.getFirstName());
    }
}
