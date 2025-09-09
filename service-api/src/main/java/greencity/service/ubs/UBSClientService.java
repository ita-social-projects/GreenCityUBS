package greencity.service.ubs;

import greencity.dto.LocationsDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.order.EventDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.user.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UBSClientService {
    //TODO Move to CertificateService
    /**
     * Methods return status of entered certificate, empty string if absent.
     *
     * @param code {@link String} code of certificate.
     * @return {@link CertificateDto} which contains status.
     * @author Oleh Bilonizhka
     */
    CertificateDto checkCertificate(String code, String userUuid);

    //TODO Move to PointService
    /**
     * Method returns list all bonuses of user.
     *
     * @param uuid of {@link User}'s uuid;
     * @return {@link AllPointsUserDto} that contains all client's bonuses;
     * @author Liubomyr Bratakh
     */
    AllPointsUserDto findAllCurrentPointsForUser(String uuid);


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

    //TODO Move to EventService
    /**
     * Methods for finding all events for Order.
     *
     * @param orderId  {@link Long} id.
     * @param email    {@link String};
     * @param language {@link String};
     * @return {@link List} that contains list of EventsDTOS.
     * @author Yuriy Bahlay.
     */
    List<EventDto> getAllEventsForOrder(Long orderId, String email, String language);

    //TODO Move to UserService
    /**
     * Methods returns current user's bonus points.
     *
     * @param uuid current {@link User}'s uuid.
     * @return {@link UserPointDto}.
     * @author Max Boiarchuk
     */
    UserPointDto getUserPoint(String uuid);

    //TODO Move to CourierService
    /**
     * Method for getting info about all active locations by courier ID or if user
     * has made an order before to get info about tariff.
     *
     * @param uuid      - user's uuid
     * @param changeLoc - optional param. If it's present provide info about
     *                  locations
     * @param courierId - id of courier
     * @return {@link OrderCourierPopUpDto}
     * @author Anton Bondar
     */
    OrderCourierPopUpDto getInfoForCourierOrderingByCourierId(String uuid, Optional<String> changeLoc, Long courierId);

    //TODO Move to CouriersService
    /**
     * Method for getting all active couriers.
     *
     * @return list of {@link CourierDto}
     * @author Anton Bondar
     */
    List<CourierDto> getAllActiveCouriers();

    //TODO Move to TariffsService
    /**
     * Method for getting info about tariff by courier ID and location ID.
     *
     * @param courierId  - id of courier
     * @param locationId - id of location
     * @return {@link TariffInfoByLocationDto}
     * @author Anton Bondar
     */
    TariffInfoByLocationDto getTariffInfoForLocation(Long courierId, Long locationId);

    //TODO Move to TariffsService
    /**
     * Method for getting info about tariff by order's id.
     *
     * @param id - id of order
     * @return {@link TariffsForLocationDto}
     */
    TariffsForLocationDto getTariffForOrder(Long id);

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

    //TODO Move to TariffsService
    /**
     * Checks if a tariff exists by its ID.
     *
     * @param tariffInfoId The ID of the tariff to check.
     * @return {@code true} if the tariff exists, {@code false} otherwise.
     */
    boolean checkIfTariffExistsById(Long tariffInfoId);

    //TODO Move to AddressService
    /**
     * Retrieves all locations.
     *
     * @return List of all locations.
     */
    List<LocationsDto> getAllLocations();

    //TODO Move to TariffsService
    /**
     * Retrieves the tariff ID associated with the specified location ID.
     *
     * @param locationId The ID of the location for which to retrieve the tariff ID.
     * @return The tariff ID associated with the specified location ID.
     */
    List<Long> getTariffIdByLocationId(Long locationId);

    //TODO Move to AddressService
    /**
     * Retrieves all active locations by courier id.
     *
     * @param courierId The ID of the courier for which to retrieve all active the
     *                  locations.
     * @return List of all locations.
     */
    List<LocationsDto> getAllLocationsByCourierId(Long courierId);
}
