package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.GetEmployeeDto;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;

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

    @Test
    void convertEmployeeWithEmptyTariffLocationToGetEmployeeDtoTest() {
        Employee employee = ModelUtils.getFullEmployee();
        employee.getTariffInfos().forEach(tariffsInfo -> tariffsInfo.setTariffLocations(Collections.emptySet()));

        GetEmployeeDto dto = ModelUtils.getGetEmployeeDto();
        dto.getTariffs().forEach(tariffs -> {
            tariffs.setRegion(null);
            tariffs.setLocationsDtos(Collections.emptyList());
        });

        assertEquals(dto, mapper.convert(employee));
    }
}
