package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.UserProfileDto;
import greencity.entity.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserToUserProfileDtoMapperTest {

    @InjectMocks
    private UserToUserProfileDtoMapper mapper;

    @Test
    void convert() {
        UserProfileDto expected = ModelUtils.userProfileDto();
        User user = ModelUtils.getUserProfile();

        assertEquals(expected, mapper.convert(user));
    }
}
