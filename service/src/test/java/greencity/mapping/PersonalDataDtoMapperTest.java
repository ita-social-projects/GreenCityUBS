package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.PersonalDataDto;
import greencity.entity.coords.Coordinates;
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
        PersonalDataDto expected = ModelUtils.getOrderResponceDto().getPersonalData();
        ubSuser.getUserAddress().setCoordinates(Coordinates.builder().longitude(0).latitude(0).build());
        ubSuser.setId(13L);
        ubSuser.getUserAddress().setId(null);

        assertEquals(expected, personalDataDtoMapper.convert(ubSuser));
    }
}