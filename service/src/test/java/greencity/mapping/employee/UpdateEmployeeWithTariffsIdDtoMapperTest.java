package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.entity.user.employee.Employee;
import greencity.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateEmployeeWithTariffsIdDtoMapperTest {

    @InjectMocks
    private UpdateEmployeeDtoMapper mapper;

    @Mock
    private PositionRepository positionRepository;

    @Test
    void convertEmployeeDtoToEmployeeTest() {
        Employee employee = ModelUtils.getFullEmployee();
        EmployeeWithTariffsIdDto dto = ModelUtils.getEmployeeWithTariffsIdDto();
        when(positionRepository.findByIdIn(any())).thenReturn(any());
        assertEquals(mapper.convert(dto).getFirstName(), employee.getFirstName());
    }
}
