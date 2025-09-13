package greencity.mapping.user;

import greencity.dto.user.UserProfileDto;
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
            .alternateEmail(user.getAlternateEmail())
            .recipientPhone(user.getRecipientPhone())
            .telegramIsNotify(user.getTelegramBot() != null && user.getTelegramBot().getIsNotify())
            .viberIsNotify(user.getViberBot() != null && user.getViberBot().getIsNotify())
            .build();
    }
}