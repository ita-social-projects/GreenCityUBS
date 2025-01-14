package greencity.mapping.employeefilterview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static greencity.ModelUtils.getEmployeeFilterView;
import static greencity.ModelUtils.getLocationsDtos;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeFilterViewToLocationsDtosMapperTest {

    private EmployeeFilterViewToLocationsDtosMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new EmployeeFilterViewToLocationsDtosMapper();
    }

    @Test
    void shouldConvertEmployeeFilterViewToLocationsDtos() {
        var employeeFilterView = getEmployeeFilterView();
        var locationsDtosExpected = getLocationsDtos(30L);
        assertEquals(locationsDtosExpected, mapper.convert(employeeFilterView));
    }
}