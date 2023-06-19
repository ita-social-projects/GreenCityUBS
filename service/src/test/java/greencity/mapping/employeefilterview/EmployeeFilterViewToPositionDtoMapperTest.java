package greencity.mapping.employeefilterview;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static greencity.ModelUtils.getEmployeeFilterView;
import static greencity.ModelUtils.getPositionDto;

class EmployeeFilterViewToPositionDtoMapperTest {

    private EmployeeFilterViewToPositionDtoMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new EmployeeFilterViewToPositionDtoMapper();
    }

    @Test
    void shouldConvertEmployeeFilterViewToPositionDto() {
        var employeeFilterView = getEmployeeFilterView();
        var positionDtoExpected = getPositionDto(5L);
        Assertions.assertEquals(positionDtoExpected, mapper.convert(employeeFilterView));
    }
}