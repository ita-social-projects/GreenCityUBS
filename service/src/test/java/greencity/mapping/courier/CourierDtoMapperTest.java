package greencity.mapping.courier;

import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.entity.order.Courier;
import greencity.entity.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static greencity.ModelUtils.getCourierTranslationDto;
import static greencity.ModelUtils.getUser;
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
        User user = getUser();
        Courier expected = Courier.builder()
            .id(22L)
            .courierStatus(ACTIVE)
            .nameUk("УБС")
            .nameEn("UBS")
            .createDate(LocalDate.of(2022, 8, 2))
            .createdBy(user)
            .build();

        CourierDto actual = courierDtoMapper.convert(expected);

        assertEquals(expected.getId(), actual.getCourierId());
        assertEquals(expected.getCourierStatus().toString(), actual.getCourierStatus());
        assertEquals(expected.getNameUk(), actual.getNameUk());
        assertEquals(expected.getNameEn(), actual.getNameEn());
        assertEquals(expected.getCreateDate(), actual.getCreateDate());
        assertEquals(user.getRecipientName() + " " + user.getRecipientSurname(), actual.getCreatedBy());
    }
}