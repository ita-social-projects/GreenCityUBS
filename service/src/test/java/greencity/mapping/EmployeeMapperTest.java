package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeDto;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EmployeeMapperTest {

    @InjectMocks
    private EmployeeMapper mapper;

    @Test
    void convert() {
        EmployeeDto dto = ModelUtils.getEmployeeDto();
        Employee employee = ModelUtils.getEmployee();

        assertEquals(dto, mapper.convert(employee));
    }
}