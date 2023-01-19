package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.mapping.employee.EmployeeDtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EmployeeDtoMapperTest {

    @InjectMocks
    EmployeeDtoMapper mapper;

    @Test
    void convert() {
        EmployeeDto dto = ModelUtils.getEmployeeDto();
        dto.setTariffs(Set.of(ModelUtils.getTariffsInfoDto()));
        Employee employee = ModelUtils.getEmployee();
        employee.setTariffInfos(Set.of(ModelUtils.getTariffsInfo()));

        assertEquals(employee.getId(), mapper.convert(dto).getId());
        assertEquals(employee.getFirstName(), mapper.convert(dto).getFirstName());
        assertEquals(employee.getLastName(), mapper.convert(dto).getLastName());
        assertEquals(employee.getPhoneNumber(), mapper.convert(dto).getPhoneNumber());
    }
}