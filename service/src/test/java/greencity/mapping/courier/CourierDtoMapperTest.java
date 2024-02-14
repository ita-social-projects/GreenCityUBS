package greencity.mapping.courier;

import greencity.dto.courier.CourierDto;
import greencity.entity.order.Courier;
import greencity.entity.user.employee.Employee;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import static greencity.ModelUtils.getEmployee;
import static greencity.enums.CourierStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CourierDtoMapperTest {

    @InjectMocks
    private CourierDtoMapper courierDtoMapper;

    @Before
    public void setup() {
        courierDtoMapper = new CourierDtoMapper();
    }

    @Test
    public void convert() {
        Employee employee = getEmployee();
        Courier expected = Courier.builder()
            .id(22L)
            .courierStatus(ACTIVE)
            .nameUk("УБС")
            .nameEn("UBS")
            .createDate(LocalDate.of(2022, 8, 2))
            .createdBy(employee)
            .build();

        CourierDto actual = courierDtoMapper.convert(expected);

        assertEquals(expected.getId(), actual.getCourierId());
        assertEquals(expected.getCourierStatus().toString(), actual.getCourierStatus());
        assertEquals(expected.getNameUk(), actual.getNameUk());
        assertEquals(expected.getNameEn(), actual.getNameEn());
        assertEquals(expected.getCreateDate(), actual.getCreateDate());
        assertEquals(employee.getFirstName() + " " + employee.getLastName(), actual.getCreatedBy());
    }
}