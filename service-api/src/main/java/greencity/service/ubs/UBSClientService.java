package greencity.service.ubs;

import greencity.dto.*;
import greencity.entity.user.User;

import java.util.List;

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
    UserPointsAndAllBagsDto getFirstPageData(String uuid, String language);

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
    List<OrderBagDto> makeOrderAgain(Long orderId);

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
     * @author Mykhailo Berezhinskiy
     */
    UserProfileDto saveProfileData(String uuid, UserProfileDto dto);
}
