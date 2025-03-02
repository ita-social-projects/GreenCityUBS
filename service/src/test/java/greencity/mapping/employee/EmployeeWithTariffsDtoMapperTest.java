package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EmployeeWithTariffsDtoMapperTest {

    @InjectMocks
    private EmployeeWithTariffsDtoMapper mapper;

    @Test
    void convertEmployeeToEmployeeWithTariffsDtoTest() {
        Employee employee = ModelUtils.getEmployeeWithTariffs();
        EmployeeWithTariffsDto dto = ModelUtils.getEmployeeWithTariffsDto();
        assertEquals(mapper.convert(employee), dto);
    }
}
