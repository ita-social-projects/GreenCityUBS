package greencity.mapping;

import greencity.dto.UserProfileDto;
import greencity.entity.user.User;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserProfileDtoMapper extends AbstractConverter<User, UserProfileDto> {
    @Override
    protected UserProfileDto convert(User user) {
        return UserProfileDto.builder()
            .recipientName(user.getRecipientName())
            .recipientSurname(user.getRecipientSurname())
            .recipientEmail(user.getRecipientEmail())
            .recipientPhone(user.getRecipientPhone())
            .build();
    }
}