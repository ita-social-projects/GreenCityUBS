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
class UpdateEmployeeWithTariffsIdDtoMapperTest {

    @InjectMocks
    private UpdateEmployeeDtoMapper mapper;

    @Test
    void convertEmployeeDtoToEmployeeTest() {
        Employee employee = ModelUtils.getFullEmployee();
        EmployeeWithTariffsIdDto dto = ModelUtils.getEmployeeWithTariffsIdDto();
        assertEquals(mapper.convert(dto).getFirstName(), employee.getFirstName());
    }
}
