package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.PersonalDataDto;
import greencity.entity.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserToPersonalDataDtoMapperTest {

    @InjectMocks
    UserToPersonalDataDtoMapper mapper;

    @Test
    void convert() {
        PersonalDataDto expected = ModelUtils.getPersonalDataDto();
        User user = ModelUtils.getUserPersonalData();

        assertEquals(expected, mapper.convert(user));
    }

}
