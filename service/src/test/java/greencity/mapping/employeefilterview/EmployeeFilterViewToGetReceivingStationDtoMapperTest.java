package greencity.mapping.employeefilterview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static greencity.ModelUtils.getEmployeeFilterView;
import static greencity.ModelUtils.getReceivingStationDto2;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeFilterViewToGetReceivingStationDtoMapperTest {

    private EmployeeFilterViewToGetReceivingStationDtoMapper mapper;

    @BeforeEach
    public void setUp() {
        this.mapper = new EmployeeFilterViewToGetReceivingStationDtoMapper();
    }

    @Test
    void shouldConvertEmployeeFilterViewToGetReceivingStationDto() {
        var employeeFilterView = getEmployeeFilterView();
        var getReceivingStationDtoExpected = getReceivingStationDto2();
        assertEquals(getReceivingStationDtoExpected, mapper.convert(employeeFilterView));
    }
}