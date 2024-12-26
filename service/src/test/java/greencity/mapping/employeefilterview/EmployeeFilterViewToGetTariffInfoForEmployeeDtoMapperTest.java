package greencity.mapping.employeefilterview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static greencity.ModelUtils.getEmployeeFilterView;
import static greencity.ModelUtils.getTariffInfoForEmployeeDto2;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeFilterViewToGetTariffInfoForEmployeeDtoMapperTest {

    private EmployeeFilterViewToGetTariffInfoForEmployeeDtoMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new EmployeeFilterViewToGetTariffInfoForEmployeeDtoMapper();
    }

    @Test
    void shouldConvertEmployeeFilterViewToGetTariffInfoForEmployeeDto() {
        var employeeFilterView = getEmployeeFilterView();
        var tariffInfoForEmployeeDtoExpected = getTariffInfoForEmployeeDto2();
        assertEquals(tariffInfoForEmployeeDtoExpected, mapper.convert(employeeFilterView));
    }
}