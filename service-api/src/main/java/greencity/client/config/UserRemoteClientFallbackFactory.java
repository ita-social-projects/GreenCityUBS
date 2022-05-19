package greencity.client.config;

import feign.hystrix.FallbackFactory;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.UserVO;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.RemoteServerUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class UserRemoteClientFallbackFactory implements FallbackFactory<UserRemoteClient> {
    @Override
    public UserRemoteClient create(Throwable throwable) {
        return new UserRemoteClient() {
            @Override
            public String findUuidByEmail(String email) {
                throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_USER_DATA, throwable);
            }

            @Override
            public Optional<UserVO> findNotDeactivatedByEmail(String email) {
                log.error(ErrorMessage.USER_WITH_THIS_EMAIL_DOES_NOT_EXIST + email, throwable);
                return Optional.empty();
            }

            @Override
            public Optional<UbsCustomersDto> findByUuid(String uuid) {
                log.error(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST, throwable);
                return Optional.empty();
            }

            @Override
            public void markUserDeactivated(String uuid) {
                throw new RemoteServerUnavailableException(ErrorMessage.USER_HAS_NOT_BEEN_DEACTIVATED, throwable);
            }

            @Override
            public PasswordStatusDto getPasswordStatus() {
                throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_PASSWORD_STATUS, throwable);
            }

            @Override
            public void sendEmailNotification(NotificationDto notification, String email) {
                log.error(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT, throwable);
            }
        };
    }
}
