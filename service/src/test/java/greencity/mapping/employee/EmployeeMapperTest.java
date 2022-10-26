package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.mapping.employee.EmployeeMapper;
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

        assertEquals(dto.getId(), mapper.convert(employee).getId());
        assertEquals(dto.getEmail(), mapper.convert(employee).getEmail());
        assertEquals(dto.getEmployeePositions(), mapper.convert(employee).getEmployeePositions());
    }
}