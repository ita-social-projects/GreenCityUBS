package greencity.service.ubs.user;

import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.UserDeletionReasonDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import java.util.Set;
import greencity.enums.UserStatus;
import java.util.List;

public interface UserService {
    /**
     * Method returns info about user, ubsUser and user violations by order orderId.
     *
     * @param orderId of {@link Long} order id;
     * @param uuid    current user's uuid;
     * @return {@link UserInfoDto};
     * @author Rusanovscaia Nadejda
     */
    UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId, String uuid);

    /**
     * Method updates ubs_user information order in order.
     *
     * @param dtoUpdate {@link UbsCustomersDtoUpdate} update payload;
     * @param userUuid  current user's uuid;
     * @return {@link UbsCustomersDto};
     * @author Rusanovscaia Nadejda
     */
    UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String userUuid);

    /**
     * Method creates ubs user profile if it does not exist.
     *
     * @param userProfileCreateDto of {@link UserProfileCreateDto} with profile
     *                             data;
     * @return id {@link Long} of ubs user profile;
     * @author Maksym Golik
     */
    Long createUserProfile(UserProfileCreateDto userProfileCreateDto);

    /**
     * Method that updates user profile data.
     *
     * @param uuid current user’s uuid;
     * @param dto  {@link UserProfileUpdateDto} payload;
     * @return {@link UserProfileUpdateDto} updated profile data;
     * @author Liubomyr Bratakh.
     */
    UserProfileUpdateDto updateProfileData(String uuid, UserProfileUpdateDto dto);

    /**
     * Method that get user profile for current user.
     *
     * @param uuid current {@link String} user`s uuid;
     * @return {@link UserProfileDto} contains information about user;
     * @author Liubomyr Bratkh
     */
    UserProfileDto getProfileData(String uuid);

    /**
     * Methods returns current user's bonus points.
     *
     * @param uuid current user's uuid.
     * @return {@link UserPointDto}.
     * @author Max Boiarchuk
     */
    UserPointDto getUserPoint(String uuid);

    /**
     * Get information about all employee's authorities.
     *
     * @param email {@link String} user's email.
     * @return Set of {@link String} employee's authorities.
     */
    Set<String> getAllAuthorities(String email);

    /**
     * Method that gets an employee`s positions and all possible related authorities
     * to these positions.
     *
     * @param email {@link String} - employee email.
     * @return {@link PositionAuthoritiesDto}.
     * @author Anton Bondar
     */
    PositionAuthoritiesDto getPositionsAndRelatedAuthorities(String email);

    /**
     * Method updates Authority for user.
     *
     * @param dto - instance of {@link UserEmployeeAuthorityDto}.
     */
    void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto);

    /**
     * Method to find user's status by uuid.
     *
     * @param uuid user's uuid.
     * @return user's status.
     */
    UserStatus getUserStatusByUuid(String uuid);

    /**
     * Method to find user's status by email.
     *
     * @param email user's email.
     * @return user's status.
     */
    UserStatus getUserStatusByEmail(String email);

    /**
     * Method to delete a user by uuid, setting their status to DELETED.
     *
     * @param uuid   {@link String} user's uuid.
     * @param reason {@link UserDeletionReasonDto} reason for deletion
     */
    void deleteUserByUuid(String uuid, UserDeletionReasonDto reason);

    /**
     * Method that change user status.
     *
     * @param currentUserUuid {@link String} current user uuid.
     * @param targetUserId    {@link Long} user uuid that is deactivated.
     * @param status          {@link UserStatus} user status.
     */
    void updateUserStatusById(String currentUserUuid, Long targetUserId, UserStatus status);

    /**
     * Method for getting a {@link List} of {@link String} - reasons for
     * deactivation of the current user.
     *
     * @param id              {@link Long} - user's id.
     * @param currentUserUuid {@link String} - user's uuid.
     * @return {@link List} of {@link String}.
     */
    List<String> getDeactivationReasons(Long id, String currentUserUuid);

    /**
     * Counts all users by user {@link UserStatus} ACTIVATED.
     *
     * @return amount of users with {@link UserStatus} ACTIVATED.
     */
    long getActivatedUsersAmount();
}
