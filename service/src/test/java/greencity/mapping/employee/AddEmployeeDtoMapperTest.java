package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.AddEmployeeDto;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AddEmployeeDtoMapperTest {

    @InjectMocks
    private AddEmployeeDtoMapper mapper;

    @Test
    void convert() {
        Employee expected = ModelUtils.getEmployee();
        AddEmployeeDto dto = ModelUtils.getAddEmployeeDto();

        assertEquals(expected.getPhoneNumber(), mapper.convert(dto).getPhoneNumber());
        assertEquals(expected.getEmail(), mapper.convert(dto).getEmail());
    }
}