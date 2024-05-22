package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.position.PositionDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.EmployeeStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

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
