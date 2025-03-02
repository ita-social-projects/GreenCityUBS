package greencity.mapping.employeefilterview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static greencity.ModelUtils.getEmployeeDto;
import static greencity.ModelUtils.getEmployeeFilterView;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeFilterViewToGetEmployeeDtoMapperTest {

    private EmployeeFilterViewToGetEmployeeDtoMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new EmployeeFilterViewToGetEmployeeDtoMapper();
    }

    @Test
    void shouldConvertEmployeeFilterViewToGetEmployeeDto() {
        var employeeFilterView = getEmployeeFilterView();
        var getEmployeeDtoExpected = getEmployeeDto();
        assertEquals(getEmployeeDtoExpected, mapper.convert(employeeFilterView));
    }
}