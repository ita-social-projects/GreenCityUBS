package greencity.client;

import feign.Headers;
import greencity.client.config.UserRemoteClientInterceptor;
import greencity.client.config.UserRemoteClientFallbackFactory;
import greencity.client.config.UserRemoteClientInterceptor;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.UserVO;
import greencity.entity.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Client for getting info about {@link User} from GreenCityUser server.
 *
 * @author Andrii Yezenitskyi
 */
@FeignClient(name = "user-remote-client",
    url = "${greencity.redirect.user-server-address}",
    configuration = UserRemoteClientInterceptor.class,
    fallbackFactory = UserRemoteClientFallbackFactory.class)
public interface UserRemoteClient {
    String EMAIL = "email";
    String UUID = "uuid";

    /**
     * Finds {@link User}'s UUID by {@link User}'s Email.
     *
     * @param email {@link User}'s Email.
     * @return {@link String} - UUID.
     */
    @GetMapping("/user/findUuidByEmail")
    String findUuidByEmail(@RequestParam(EMAIL) String email);

    /**
     * Finds {@link UserVO} that is not 'DEACTIVATED' by {@link UserVO}'s Email.
     *
     * @param email {@link UserVO}'s Email.
     * @return {@link Optional} of {@link UserVO}.
     */
    @GetMapping("/user/findNotDeactivatedByEmail")
    Optional<UserVO> findNotDeactivatedByEmail(@RequestParam(EMAIL) String email);

    /**
     * Finds {@link UbsCustomersDto} by {@link User}'s UUID.
     *
     * @param uuid {@link User}'s UUID.
     * @return {@link Optional} of {@link UbsCustomersDto}.
     */
    @GetMapping("/user/findByUuId")
    Optional<UbsCustomersDto> findByUuid(@RequestParam(UUID) String uuid);

    /**
     * Changes userStatus to "DEACTIVATED" by UUID.
     *
     * @param uuid {@link User}'s UUID.
     */
    @PutMapping("/user/markUserAsDeactivated")
    void markUserDeactivated(@RequestParam(UUID) String uuid);

    /**
     * Gets current user's password status.
     *
     * @return {@link PasswordStatusDto}.
     */
    @GetMapping("/ownSecurity/password-status")
    PasswordStatusDto getPasswordStatus();

    /**
     * Sends an email notification for user.
     *
     * @param notification {@link NotificationDto} - notification details.
     * @param email        {@link String} user's email.
     */
    @PostMapping("/email/notification")
    void sendEmailNotification(NotificationDto notification, @RequestParam(EMAIL) String email);
}
