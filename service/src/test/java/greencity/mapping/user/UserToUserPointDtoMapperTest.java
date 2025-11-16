package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.UserPointDto;
import greencity.entity.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserToUserPointDtoMapperTest {
    @InjectMocks
    private UserToUserPointDtoMapper userToUserPointDtoMapper;

    @Test
    void convert() {
        User user = ModelUtils.getUser();
        UserPointDto expected = UserPointDto.builder().points(100).build();
        UserPointDto actual = userToUserPointDtoMapper.convert(user);
        assertEquals(actual.getPoints(), expected.getPoints());
    }
}
