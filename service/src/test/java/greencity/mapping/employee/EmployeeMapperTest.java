package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.mapping.employee.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EmployeeMapperTest {

    @InjectMocks
    private EmployeeMapper mapper;

    @Test
    void convert() {
        EmployeeDto dto = ModelUtils.getEmployeeDto();
        dto.setTariffs(Set.of(ModelUtils.getTariffsInfoDto()));
        Employee employee = ModelUtils.getEmployee();
        employee.setTariffInfos(Set.of(ModelUtils.getTariffsInfo()));

        assertEquals(dto.getId(), mapper.convert(employee).getId());
        assertEquals(dto.getEmail(), mapper.convert(employee).getEmail());
        assertEquals(dto.getEmployeePositions(), mapper.convert(employee).getEmployeePositions());
    }
}