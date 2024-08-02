package greencity.client;

import greencity.client.config.UserRemoteClientInterceptor;
import greencity.client.config.UserRemoteClientFallbackFactory;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.employee.EmployeeSignUpDto;
import greencity.dto.employee.EmployeePositionsDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.notification.EmailNotificationDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.PasswordStatusDto;
import greencity.dto.user.UserVO;
import greencity.entity.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
     * Method checks the existence of the user by uuid.
     *
     * @param uuid {@link User}'s UUID.
     * @return {@link Boolean}.
     */
    @GetMapping("/user/checkByUuid")
    boolean checkIfUserExistsByUuid(@RequestParam(UUID) String uuid);

    /**
     * Gets user's positions and all possible related authorities to these positions
     * by user's email.
     *
     * @param email {@link String} - user's email.
     * @return {@link PositionAuthoritiesDto}.
     * @author Anton Bondar
     */
    @GetMapping("/user/get-positions-authorities")
    PositionAuthoritiesDto getPositionsAndRelatedAuthorities(@RequestParam String email);

    /**
     * Changes userStatus to "DEACTIVATED" by UUID.
     *
     * @param uuid {@link User}'s uuid.
     */
    @PutMapping("/user/deactivate")
    void markUserDeactivated(@RequestParam(UUID) String uuid, @RequestBody DeactivateUserRequestDto request);

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
     * @param notification {@link EmailNotificationDto} - notification details.
     */
    @PostMapping("/email/general/notification")
    void sendEmailNotification(@RequestBody EmailNotificationDto notification);

    /**
     * Get information about all employee's authorities.
     *
     * @param email {@link String} user's email.
     * @return Set of {@link String} employee's authorities.
     */
    @GetMapping("/user/get-all-authorities")
    Set<String> getAllAuthorities(@RequestParam(EMAIL) String email);

    /**
     * Edit an employee's authorities.
     *
     * @param dto {@link UserEmployeeAuthorityDto}
     */
    @PutMapping("/user/edit-authorities")
    void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto);

    /**
     * Save an employee to users table in GreenCityUser.
     *
     * @param dto {@link EmployeeSignUpDto}
     */
    @PostMapping("/ownSecurity/sign-up-employee")
    void signUpEmployee(@RequestBody EmployeeSignUpDto dto);

    /**
     * Update employee email.
     *
     * @param newEmployeeEmail - new email of employee.
     * @param uuid             - uuid of current employee.
     */
    @PutMapping("/user/employee-email")
    void updateEmployeeEmail(@RequestParam String newEmployeeEmail, @RequestParam String uuid);

    /**
     * Update an employee`s authorities to related positions in chosen employee.
     *
     * @param dto {@link EmployeePositionsDto} contains email and list of positions.
     */
    @PutMapping("/user/authorities")
    void updateAuthoritiesToRelatedPositions(@RequestBody EmployeePositionsDto dto);

    /**
     * Deactivate employee by uuid.
     *
     * @param uuid - uuid of employee.
     */
    @PutMapping("/user/deactivate-employee")
    void deactivateEmployee(@RequestParam String uuid);

    /**
     * Activate employee by uuid.
     *
     * @param uuid - uuid of employee.
     */
    @PutMapping("/user/markUserAsActivated")
    void activateEmployee(@RequestParam String uuid);
}
