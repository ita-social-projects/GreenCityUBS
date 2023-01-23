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
public class SaveEmployeeDtoMapperTest {

    @InjectMocks
    private SaveEmployeeDtoMapper mapper;

    @Test
    public void convertSaveEmployeeDtoToEmployeeTest() {
        Employee employee = ModelUtils.getFullEmployee();
        SaveEmployeeDto dto = ModelUtils.getSaveEmployeeDto();
        assertEquals(mapper.convert(dto).getFirstName(), employee.getFirstName());
    }
}
