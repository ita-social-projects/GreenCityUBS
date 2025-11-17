package greencity.mapping.user;

import greencity.dto.user.UserPointDto;
import greencity.entity.user.User;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserPointDtoMapper extends AbstractConverter<User, UserPointDto> {
    @Override
    protected UserPointDto convert(User user) {
        return UserPointDto.builder()
            .points(user.getCurrentPoints())
            .build();
    }
}
