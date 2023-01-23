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
public class UpdateEmployeeDtoMapperTest {

    @InjectMocks
    private UpdateEmployeeDtoMapper mapper;

    @Test
    public void convertUpdateEmployeeDtoToEmployeeTest() {
        Employee employee = ModelUtils.getFullEmployee();
        UpdateEmployeeDto dto = ModelUtils.getUpdateEmployeeDto();
        assertEquals(mapper.convert(dto).getFirstName(), employee.getFirstName());
    }
}