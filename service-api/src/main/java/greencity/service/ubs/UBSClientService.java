package greencity.service.ubs;

import greencity.dto.CreateAddressRequestDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.location.LocationSummaryDto;
import greencity.dto.order.*;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.user.*;
import greencity.entity.user.User;
import greencity.enums.OrderStatus;
import greencity.exceptions.payment.PaymentLinkException;
import org.springframework.data.domain.Pageable;

import java.util.*;

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
     * @param uuid       current {@link User}'s uuid.
     * @param tariffId   {@link Long} tariff id.
     * @param locationId {@ling Long} location id.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Safarov Renat
     */
    UserPointsAndAllBagsDto getFirstPageDataByTariffAndLocationId(String uuid, Long tariffId, Long locationId);

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
    CertificateDto checkCertificate(String code);

    /**
     * Methods saves all entered by user data to database.
     *
     * @param dto     {@link OrderResponseDto} user entered data;
     * @param uuid    current {@link User}'s uuid;
     * @param orderId {@link Long} order id;
     * @return {@link PaymentRequestDto} which contains data to pay order out.
     * @author Oleh Bilonizhka
     */
    FondyOrderResponse saveFullOrderToDB(OrderResponseDto dto, String uuid, Long orderId);

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
     * Method that update address for current user.
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
    UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String email);

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
     * @param email   {@link String};
     * @return {@link List} that contains list of EventsDTOS.
     * @author Yuriy Bahlay.
     */
    List<EventDto> getAllEventsForOrder(Long orderId, String email);

    /**
     * Method that returns order info for surcharge.
     *
     * @return {@link OrderStatusPageDto}.
     * @author Igor Boykov
     */
    OrderStatusPageDto getOrderInfoForSurcharge(Long orderId, String uuid);

    /**
     * Method for delete user order.
     *
     * @param id - current order id.
     * @author Max Boyarchuk
     */
    void deleteOrder(String uuid, Long id);

    /**
     * Method return link with Fondy payment .
     * 
     * @param dto - current OrderFondyClientDto dto.
     * @author Max Boiarchuk
     */
    FondyOrderResponse processOrderFondyClient(OrderFondyClientDto dto, String uuid) throws PaymentLinkException;

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

    /**
     * Method for getting info about all active locations or if user has made an
     * order before to get info about tariff.
     *
     * @param uuid      - user's uuid
     * @param changeLoc - optional param. If it's present provide info about
     *                  locations
     * @return {@link OrderCourierPopUpDto}
     */
    OrderCourierPopUpDto getInfoForCourierOrdering(String uuid, Optional<String> changeLoc);

    /**
     * Method for getting info about tariff.
     *
     * @param locationId - id of location
     * @return {@link OrderCourierPopUpDto}
     */
    OrderCourierPopUpDto getTariffInfoForLocation(Long locationId);

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
     * Method updates Authority for {@link User}.
     *
     * @param dto - instance of {@link UserEmployeeAuthorityDto}.
     */
    void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto);

    /**
     * Methods returns all locations.
     *
     * @return {@link LocationSummaryDto}.
     * @author Max Nazaruk
     */
    List<LocationSummaryDto> getLocationSummary();
}
