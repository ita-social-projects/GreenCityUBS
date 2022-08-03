package greencity.mapping.courier;

import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.entity.language.Language;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierTranslation;
import greencity.entity.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static greencity.entity.enums.CourierStatus.ACTIVE;
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
        CourierTranslation courierTranslation = CourierTranslation.builder()
                .id(2L)
                .name("Ukrainian")
                .courier(new Courier())
                .language(new Language())
                .build();

        CourierTranslationDto courierTranslationDto = CourierTranslationDto.builder()
                .name("Ukrainian")
                .languageCode("null")
                .build();

        User user = User.builder()
                .recipientName("Ivan")
                .recipientSurname("Boiko")
                .build();

        Courier expected = Courier.builder()
                .id(22L)
                .courierStatus(ACTIVE)
                .courierTranslationList(List.of(courierTranslation))
                .createDate(LocalDate.of(2022, 8, 2))
                .createdBy(user)
                .build();

        CourierDto actual = courierDtoMapper.convert(expected);

        assertEquals(expected.getId(), actual.getCourierId());
        assertEquals(expected.getCourierStatus().toString(), actual.getCourierStatus());
        assertEquals(courierTranslationDto.getName(), actual.getCourierTranslationDtos().get(0).getName());
        assertEquals(expected.getCreateDate(), actual.getCreateDate());
        assertEquals(user.getRecipientName() + " " + user.getRecipientSurname(), actual.getCreatedBy());
    }
}
