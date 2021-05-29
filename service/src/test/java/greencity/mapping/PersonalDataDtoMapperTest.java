package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.PersonalDataDto;
import greencity.entity.user.ubs.UBSuser;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalDataDtoMapperTest {
    @InjectMocks
    PersonalDataDtoMapper personalDataDtoMapper;

    @Test
    void convert() {
        UBSuser ubSuser = ModelUtils.getUBSuser();
        PersonalDataDto expected = ModelUtils.getOrderResponseDto().getPersonalData();
        ubSuser.setId(13L);

        assertEquals(expected, personalDataDtoMapper.convert(ubSuser));
    }
}
