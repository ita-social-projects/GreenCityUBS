package greencity.service.ubs;

import greencity.dto.*;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    CertificateDto checkCertificate(String code);

    /**
     * Methods saves all entered by user data to database.
     *
     * @param dto  {@link OrderResponseDto} user entered data;
     * @param uuid current {@link User}'s uuid;
     * @return {@link PaymentRequestDto} which contains data to pay order out.
     * @author Oleh Bilonizhka
     */
    FondyOrderResponse saveFullOrderToDB(OrderResponseDto dto, String uuid);

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
     * Method creates the same order again if order's status is ON_THE_ROUTE,
     * CONFIRMED or DONE.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link OrderClientDto} that contains client's order;
     * @author Danylko Mykola
     */
    MakeOrderAgainDto makeOrderAgain(Locale locale, Long orderId);

    /**
     * Method that returns info about all orders for specified userID.
     *
     * @param uuid current {@link User}'s uuid;
     * @author Oleksandr Khomiakov
     */
    PageableDto<OrdersDataForUserDto> getOrdersForUser(String uuid, Pageable page);

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
    UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String uuid);

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
     * @param id {@link Long}
     *
     * @author Liubomyr Bratakh
     */
    void markUserAsDeactivated(Long id);

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
     * Method updates cancellation reason and comment.
     *
     * @param id   {@link Long};
     * @param dto  {@link OrderCancellationReasonDto};
     * @param uuid current {@link User}'s uuid;
     * @return {@link OrderCancellationReasonDto} dto that contains cancellation
     *         reason and comment;
     * @author Oleksandr Khomiakov
     */
    OrderCancellationReasonDto updateOrderCancellationReason(long id, OrderCancellationReasonDto dto, String uuid);

    /**
     * Methods for finding all events for Order.
     *
     * @param orderId {@link Long} id.
     * @param uuid    current {@link User}'s uuid;
     * @return {@link List} that contains list of EventsDTOS.
     * @author Yuriy Bahlay.
     */
    List<EventDto> getAllEventsForOrder(Long orderId, String uuid);

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
     * @return {@link LiqPayOrderResponse} order id and liqpay payment button.
     * @author Vadym Makitra
     */
    LiqPayOrderResponse saveFullOrderToDBFromLiqPay(OrderResponseDto dto, String uuid);

    /**
     * Method validates received payment response.
     * 
     * @param dto {@link PaymentResponseDtoLiqPay}
     * @author Vadym Makitra
     */
    void validateLiqPayPayment(PaymentResponseDtoLiqPay dto);

    /**
     * Method that returns order info for surcharge.
     *
     * @return {@link OrderStatusPageDto}.
     * @author Igor Boykov
     */
    OrderStatusPageDto getOrderInfoForSurcharge(Long orderId);

    /**
     * Method for get info about payment status from LiqPay.
     * 
     * @param orderId - current order id.
     * @param uuid    current {@link User}'s uuid;
     * @return {@link Map}
     * @author Vadym Makitra
     */
    Map<String, Object> getLiqPayStatus(Long orderId, String uuid) throws Exception;

    /**
     * Method for delete user order.
     *
     * @param id - current order id.
     * @author Max Boyarchuk
     */
    void deleteOrder(Long id);

    /**
     * Method return link with Fondy payment .
     * 
     * @param dto - current OrderFondyClientDto dto.
     * @author Max Boiarchuk
     */
    FondyOrderResponse processOrderFondyClient(OrderFondyClientDto dto, String uuid) throws Exception;

    /**
     * Method return link with liqpay payment .
     *
     * @param dto - current OrderLiqpayClientDto dto.
     * @author Max Boiarchuk
     */
    LiqPayOrderResponse proccessOrderLiqpayClient(OrderFondyClientDto dto, String uuid) throws Exception;

    /**
     * Method for getting info about courier,location,and courier limits.
     *
     * @param courierId - id of current courier;
     * @return {@link GetCourierLocationDto}
     * @author Vadym Makitra
     */
    List<GetCourierLocationDto> getCourierLocationByCourierIdAndLanguageCode(Long courierId);

    /**
     * Method validates received payment client response.
     *
     * @param dto {@link PaymentResponseDto} - response order data.
     */
    void validatePaymentClient(PaymentResponseDto dto);

    /**
     * Methods returns current user's bonus points.
     *
     * @param uuid current {@link User}'s uuid.
     * @return {@link UserPointDto}.
     * @author Max Boiarchuk
     */
    UserPointDto getUserPoint(String uuid);
}
