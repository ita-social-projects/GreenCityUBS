/*
package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.PersonalDataDto;
import greencity.entity.user.ubs.UBSuser;
import greencity.mapping.user.PersonalDataDtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PersonalDataDtoMapperTest {
    @InjectMocks
    PersonalDataDtoMapper personalDataDtoMapper;

    @Test
    void convert() {
        UBSuser ubSuser = ModelUtils.getUBSuser();
        PersonalDataDto expected = ModelUtils.getOrderResponseDto().getPersonalData();
        ubSuser.setId(13L);
        expected.setUbsUserId(null);

        assertEquals(expected, personalDataDtoMapper.convert(ubSuser));
    }
}
*/
