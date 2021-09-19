package greencity.service.ubs;

import greencity.dto.*;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;

import java.util.List;
import java.util.Locale;

public interface UBSClientService {
    /**
     * Method validates received payment response.
     *
     * @param dto {@link PaymentResponseDto} - response order data.
     */
    void validatePayment(PaymentResponseDto dto);

    /**
     * Methods returns all available for order bags and current user's bonus points.
     *
     * @param uuid current {@link User}'s uuid.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Oleh Bilonizhka
     */
    UserPointsAndAllBagsDto getFirstPageData(String uuid);

    /**
     * test method for frontend.
     *
     * @author Denys Kisliak
     */
    UserPointsAndAllBagsDtoTest getFirstPageDataTest(String uuid) throws InterruptedException;

    /**
     * Methods returns all saved user data.
     *
     * @param uuid current {@link User}'s uuid.
     * @return list of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    List<PersonalDataDto> getSecondPageData(String uuid);

    /**
     * Methods return status of entered certificate, empty string if absent.
     *
     * @param code {@link String} code of certificate.
     * @return {@link CertificateDto} which contains status.
     * @author Oleh Bilonizhka
     */
    CertificateDto checkCertificate(String code);

    /**
     * Methods saves all entered by user data to database.
     *
     * @param dto  {@link OrderResponseDto} user entered data;
     * @param uuid current {@link User}'s uuid;
     * @return {@link PaymentRequestDto} which contains data to pay order out.
     * @author Oleh Bilonizhka
     */
    String saveFullOrderToDB(OrderResponseDto dto, String uuid);

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
     * @param dtoRequest {@link OrderAddressDtoRequest} information about address;
     * @param uuid       current {@link User}'s uuid;
     * @return {@link OrderAddressDtoRequest} contains all information needed for
     *         save address;
     * @author Veremchuk Zakhar
     */
    OrderWithAddressesResponseDto saveCurrentAddressForOrder(OrderAddressDtoRequest dtoRequest, String uuid);

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
     * Method returns list of all orders done by user.
     *
     * @param uuid current {@link User}'s uuid;
     * @return {@link OrderClientDto} that contains client's orders.
     * @author Danylko Mykola
     */
    List<OrderClientDto> getAllOrdersDoneByUser(String uuid);

    /**
     * Method cancels order with status FORMED.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link OrderClientDto} that contains client's order;
     * @author Danylko Mykola
     */
    OrderClientDto cancelFormedOrder(Long orderId);

    /**
     * Method creates the same order again if order's status is ON_THE_ROUTE,
     * CONFIRMED or DONE.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link OrderClientDto} that contains client's order;
     * @author Danylko Mykola
     */
    MakeOrderAgainDto makeOrderAgain(Locale locale, Long orderId);

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
     * @return {@link UserInfoDto};
     * @author Rusanovscaia Nadejda
     */
    UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId);

    /**
     * Method updates ubs_user information order in order.
     *
     * @param dtoUpdate of {@link UbsCustomersDtoUpdate} ubs_user_id;
     * @return {@link UbsCustomersDto};
     * @author Rusanovscaia Nadejda
     */
    UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate);

    /**
     * Method that save user for current user.
     *
     * @param uuid current {@link String} user`s uuid;
     * @param dto  user`s date {@link UserProfileDto} user;
     * @return {@link UserProfileDto} contains all information needed save user;
     * @author Liubomyr Bratakh
     */
    UserProfileDto saveProfileData(String uuid, UserProfileDto dto);

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
     * @param id {@link Long}
     *
     * @author Liubomyr Bratakh
     */
    void markUserAsDeactivated(Long id);

    /**
     * Method returns cancellation reason and comment.
     *
     * @param orderId {@link Long};
     * @return {@link OrderCancellationReasonDto} dto that contains cancellation
     *         reason and comment;
     *
     * @author Oleksandr Khomiakov
     */
    OrderCancellationReasonDto getOrderCancellationReason(Long orderId);

    /**
     * Method updates cancellation reason and comment.
     *
     * @param id  {@link Long};
     * @param dto {@link OrderCancellationReasonDto};
     * @return {@link OrderCancellationReasonDto} dto that contains cancellation
     *         reason and comment;
     * @author Oleksandr Khomiakov
     */
    OrderCancellationReasonDto updateOrderCancellationReason(long id, OrderCancellationReasonDto dto);

    /**
     * Method gets list of locations.
     *
     * @param userUuid {@link UserVO} id.
     * @return {@link LocationResponseDto} dto that contains location id and name;
     * @author Denys Kisliak
     */
    List<LocationResponseDto> getAllLocations(String userUuid);

    /**
     * Method updates last order location.
     *
     * @param userUuid   {@link UserVO} id.
     * @param locationId {@link LocationIdDto} id.
     * @author Denys Kisliak
     */
    void setNewLastOrderLocation(String userUuid, LocationIdDto locationId);

    /**
     * Methods for finding all events for Order.
     *
     * @param orderId {@link Long} id.
     * @return {@link List} that contains list of EventsDTOS.
     * @author Yuriy Bahly.
     */
    List<EventDto> getAllEventsForOrderById(Long orderId);

    /**
     * Methods for converting UserProfileDTO to PersonalDataDTO.
     *
     * @param userProfileDto {@link UserProfileDto}.
     * @return {@link PersonalDataDto}.
     * @author Liyubomy Pater.
     */
    PersonalDataDto convertUserProfileDtoToPersonalDataDto(UserProfileDto userProfileDto);

    /**
     * Methods for saving UbsUser when User is saving profile data.
     *
     * @param userProfileDto {@link UserProfileDto}.
     * @param savedUser      {@link User}.
     * @param savedAddress   {@link Address}.
     * @author Liyubomy Pater.
     */
    UBSuser createUbsUserBasedUserProfileData(UserProfileDto userProfileDto, User savedUser, Address savedAddress);

    /**
     * Methods saves all entered by user data to database.
     * 
     * @param dto  {@link OrderResponseDto} user entered data;
     * @param uuid current {@link User}'s uuid;
     * @return {@link PaymentRequestDto} which contains data to pay order out.
     * @author Vadym Makitra
     */
    String saveFullOrderToDBFromLiqPay(OrderResponseDto dto, String uuid);

    /**
     * Method validates received payment response.
     * 
     * @param dto       {@link PaymentResponseDtoLiqPay}
     * @param signature {@link String} signature that we get from LiqPay
     * @author Vadym Makitra
     */
    void validateLiqPayPayment(PaymentResponseDtoLiqPay dto, String signature);
}