package greencity.service.ubs;

import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.user.User;
import java.util.Set;

public interface UBSClientService {
    //TODO Move to UserService
    /**
     * Method returns info about user, ubsUser and user violations by order orderId.
     *
     * @param orderId of {@link Long} order id;
     * @param uuid    current {@link User}'s uuid;
     * @return {@link UserInfoDto};
     * @author Rusanovscaia Nadejda
     */
    UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId, String uuid);

    //TODO Move to UserService
    /**
     * Method updates ubs_user information order in order.
     *
     * @param dtoUpdate of {@link UbsCustomersDtoUpdate} ubs_user_id;
     * @return {@link UbsCustomersDto};
     * @author Rusanovscaia Nadejda
     */
    UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String userUuid);


    //TODO Move to UserService
    /**
     * Method creates ubs user profile if it does not exist.
     *
     * @param userProfileCreateDto of {@link UserProfileCreateDto} with profile
     *                             data;
     * @return id {@link Long} of ubs user profile;
     * @author Maksym Golik
     */
    Long createUserProfile(UserProfileCreateDto userProfileCreateDto);

    //TODO Move to UserService
    /**
     * Method that update user.
     *
     * @param uuid current {@link String} user`s uuid;
     * @param dto  user`s date {@link UserProfileDto} user;
     * @return {@link UserProfileDto} contains all information needed for updating
     *         user;
     * @author Liubomyr Bratakh.
     */
    UserProfileUpdateDto updateProfileData(String uuid, UserProfileUpdateDto dto);

    //TODO Move to UserService
    /**
     * Method that get user profile for current user.
     *
     * @param uuid current {@link String} user`s uuid;
     * @return {@link UserProfileDto} contains information about user;
     * @author Liubomyr Bratkh
     */
    UserProfileDto getProfileData(String uuid);

    //TODO Move to UserService prolly will be removed by Rostyslav
    /**
     * Method that mark user as DEACTIVATED.
     *
     * @param uuid    {@link String} current user uuid.
     * @param request {@link DeactivateUserRequestDto} information for deactivation.
     * @author Liubomyr Bratakh
     */
    void markUserAsDeactivated(String uuid, DeactivateUserRequestDto request);

    //TODO Move to UserService
    /**
     * Methods returns current user's bonus points.
     *
     * @param uuid current {@link User}'s uuid.
     * @return {@link UserPointDto}.
     * @author Max Boiarchuk
     */
    UserPointDto getUserPoint(String uuid);

    //TODO Move to UserService
    /**
     * Get information about all employee's authorities.
     *
     * @param email {@link String} user's email.
     * @return Set of {@link String} employee's authorities.
     */
    Set<String> getAllAuthorities(String email);

    //TODO Move to UserService
    /**
     * Method that gets an employee`s positions and all possible related authorities
     * to these positions.
     *
     * @param email {@link String} - employee email.
     * @return {@link PositionAuthoritiesDto}.
     * @author Anton Bondar
     */
    PositionAuthoritiesDto getPositionsAndRelatedAuthorities(String email);

    //TODO Move to UserService
    /**
     * Method updates Authority for {@link User}.
     *
     * @param dto - instance of {@link UserEmployeeAuthorityDto}.
     */
    void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto);
}
