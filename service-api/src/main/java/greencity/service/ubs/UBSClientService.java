package greencity.service.ubs;

import greencity.dto.CreateAddressRequestDto;
import greencity.dto.LocationsDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.address.AddressDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.order.EventDto;
import greencity.dto.order.WayForPayOrderResponse;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.user.User;
import greencity.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

public interface UBSClientService {
    /**
     * Validates the payment response received from the payment gateway.
     *
     * @param response The payment response data.
     * @return An object representing the validated payment response.
     */
    @Transactional
    PaymentResponseWayForPay validatePayment(PaymentResponseDto response);

    /**
     * Methods returns all available for order bags and current user's bonus points.
     *
     * @param tariffId   {@link Long} tariff id.
     * @param locationId {@link Long} location id.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Safarov Renat
     */
    UserPointsAndAllBagsDto getFirstPageDataByTariffAndLocationId(Long tariffId, Long locationId);

    /**
     * Methods returns all available for order bags and current user's bonus points.
     *
     * @param uuid    current {@link User}'s uuid.
     * @param orderId {@link Long} id of existing order.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Safarov Renat
     */
    UserPointsAndAllBagsDto getFirstPageDataByOrderId(String uuid, Long orderId);

    /**
     * Methods returns all saved user data.
     *
     * @param uuid current {@link User}'s uuid.
     * @return instance of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    PersonalDataDto getSecondPageData(String uuid);

    /**
     * Methods return status of entered certificate, empty string if absent.
     *
     * @param code {@link String} code of certificate.
     * @return {@link CertificateDto} which contains status.
     * @author Oleh Bilonizhka
     */
    CertificateDto checkCertificate(String code, String userUuid);

    /**
     * Methods saves all entered by user data to database.
     *
     * @param dto     {@link OrderResponseDto} user entered data;
     * @param uuid    current {@link User}'s uuid;
     * @param orderId {@link Long} order id;
     * @return {@link PaymentRequestDto} which contains data to pay order out.
     * @author Oleh Bilonizhka
     */
    WayForPayOrderResponse saveFullOrderToDB(OrderResponseDto dto, String uuid, Long orderId);

    /**
     * Method get status of order from db by id.
     *
     * @param orderId {@link Long} order id;
     * @param uuid    current {@link User}'s uuid;
     * @return - payment status
     * @author Vadym Makitra
     */
    FondyPaymentResponse getPaymentResponseFromFondy(Long orderId, String uuid);

    /**
     * Methods return list of all user addresses.
     *
     * @param uuid current {@link User}'s uuid;
     * @return {@link OrderWithAddressesResponseDto} that contains address list
     * @author Veremchuk Zakhar
     */
    OrderWithAddressesResponseDto findAllAddressesForCurrentOrder(String uuid);

    /**
     * Method that save address for current user.
     *
     * @param requestDto {@link CreateAddressRequestDto} information about address;
     * @param uuid       current {@link User}'s uuid;
     * @return {@link OrderAddressDtoRequest} contains all information needed for
     *         save address;
     * @author Veremchuk Zakhar
     */
    OrderWithAddressesResponseDto saveCurrentAddressForOrder(CreateAddressRequestDto requestDto, String uuid);

    /**
     * Method that update address for current user (if placeId is null updates only
     * addressComment).
     *
     * @param requestDto {@link OrderAddressDtoRequest} information about address;
     * @param uuid       current {@link User}'s uuid;
     * @return {@link OrderAddressDtoRequest} contains all information needed for
     *         update address;
     * @author Oleg Postolovskyi
     */
    OrderWithAddressesResponseDto updateCurrentAddressForOrder(OrderAddressDtoRequest requestDto, String uuid);

    /**
     * Method that delete user address.
     *
     * @param addressId of {@link Long} address id;
     * @param uuid      current {@link User}'s uuid;
     * @return {@link OrderWithAddressesResponseDto} that contains address list;
     * @author Veremchuk Zakhar
     */
    OrderWithAddressesResponseDto deleteCurrentAddressForOrder(Long addressId, String uuid);

    /**
     * Method that returns info about all orders for specified userID.
     *
     * @param uuid current {@link User}'s uuid;
     * @author Oleksandr Khomiakov
     */
    PageableDto<OrdersDataForUserDto> getOrdersForUser(String uuid, Pageable page, List<OrderStatus> statuses);

    /**
     * Method that returns info about order for specified userID.
     *
     * @param uuid current {@link User}'s uuid;
     * @author Oleg Postolovskyi
     */
    OrdersDataForUserDto getOrderForUser(String uuid, Long id);

    /**
     * Method returns list all bonuses of user.
     *
     * @param uuid of {@link User}'s uuid;
     * @return {@link AllPointsUserDto} that contains all client's bonuses;
     * @author Liubomyr Bratakh
     */
    AllPointsUserDto findAllCurrentPointsForUser(String uuid);

    /**
     * Method returns info about user, ubsUser and user violations by order orderId.
     *
     * @param orderId of {@link Long} order id;
     * @param uuid    current {@link User}'s uuid;
     * @return {@link UserInfoDto};
     * @author Rusanovscaia Nadejda
     */
    UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId, String uuid);

    /**
     * Method updates ubs_user information order in order.
     *
     * @param dtoUpdate of {@link UbsCustomersDtoUpdate} ubs_user_id;
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
     * Method that update user.
     *
     * @param uuid current {@link String} user`s uuid;
     * @param dto  user`s date {@link UserProfileDto} user;
     * @return {@link UserProfileDto} contains all information needed for updating
     *         user;
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
     * Method returns information about order payment by orderId.
     *
     * @param orderId {@link Long}
     * @return {@link OrderPaymentDetailDto} dto that contain information about
     *         order payment.
     * @author Mykola Danylko
     */
    OrderPaymentDetailDto getOrderPaymentDetail(Long orderId);

    /**
     * Method that mark user as DEACTIVATED.
     *
     * @param uuid    {@link String} current user uuid.
     * @param request {@link DeactivateUserRequestDto} information for deactivation.
     *
     * @author Liubomyr Bratakh
     */
    void markUserAsDeactivated(String uuid, DeactivateUserRequestDto request);

    /**
     * Method returns cancellation reason and comment.
     *
     * @param orderId {@link Long};
     * @param uuid    current {@link User}'s uuid;
     * @return {@link OrderCancellationReasonDto} dto that contains cancellation
     *         reason and comment;
     *
     * @author Oleksandr Khomiakov
     */
    OrderCancellationReasonDto getOrderCancellationReason(Long orderId, String uuid);

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

    /**
     * Method for delete user order.
     *
     * @param id - current order id.
     * @author Max Boyarchuk
     */
    void deleteOrder(String uuid, Long id);

    /**
     * Methods returns current user's bonus points.
     *
     * @param uuid current {@link User}'s uuid.
     * @return {@link UserPointDto}.
     * @author Max Boiarchuk
     */
    UserPointDto getUserPoint(String uuid);

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

    /**
     * Method for getting all active couriers.
     *
     * @return list of {@link CourierDto}
     *
     * @author Anton Bondar
     */
    List<CourierDto> getAllActiveCouriers();

    /**
     * Method for getting info about tariff by courier ID and location ID.
     *
     * @param courierId  - id of courier
     * @param locationId - id of location
     * @return {@link OrderCourierPopUpDto}
     * @author Anton Bondar
     */
    OrderCourierPopUpDto getTariffInfoForLocation(Long courierId, Long locationId);

    /**
     * Method for getting info about tariff by order's id.
     *
     * @param id - id of order
     * @return {@link TariffsForLocationDto}
     */
    TariffsForLocationDto getTariffForOrder(Long id);

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
     * Method that gets information about login employee`s positions.
     *
     * @param email {@link String} - employee email.
     * @return List of {@link String} - list of employee`s positions.
     * @author Anton Bondar
     */
    List<String> getEmployeeLoginPositionNames(String email);

    /**
     * Method updates Authority for {@link User}.
     *
     * @param dto - instance of {@link UserEmployeeAuthorityDto}.
     */
    void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto);

    /**
     * Makes an address actual (default) for a given user, identified by his UUID.
     *
     * @param addressId - the ID of the address to make the default
     * @param uuid      - the UUID of the user whose address is being updated
     *
     * @return an {@link AddressDto} object representing the updated address
     */
    AddressDto makeAddressActual(Long addressId, String uuid);

    /**
     * Method gets all districts in city.
     *
     * @param region - name of region
     * @param city   - name of city
     *
     * @return {@link DistrictDto}
     */

    List<DistrictDto> getAllDistricts(String region, String city);

    /**
     * Method to generate payment link.
     *
     * @param userUuid current {@link User} uuid.
     * @param dto      order information.
     * @return {@link WayForPayOrderResponse} payment link and order id.
     */
    WayForPayOrderResponse processOrder(String userUuid, OrderWayForPayClientDto dto);

    /**
     * Checks if a tariff exists by its ID.
     *
     * @param tariffInfoId The ID of the tariff to check.
     * @return {@code true} if the tariff exists, {@code false} otherwise.
     */
    boolean checkIfTariffExistsById(Long tariffInfoId);

    /**
     * Retrieves all locations.
     *
     * @return List of all locations.
     */
    List<LocationsDto> getAllLocations();

    /**
     * Retrieves the tariff ID associated with the specified location ID.
     *
     * @param locationId The ID of the location for which to retrieve the tariff ID.
     * @return The tariff ID associated with the specified location ID.
     */
    Long getTariffIdByLocationId(Long locationId);

    /**
     * Retrieves all active locations by courier id.
     *
     * @param courierId The ID of the courier for which to retrieve all active the
     *                  locations.
     * @return List of all locations.
     */
    List<LocationsDto> getAllLocationsByCourierId(Long courierId);
}
