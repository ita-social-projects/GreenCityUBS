package greencity.service.ubs;

import greencity.dto.bag.AdditionalBagInfoDto;
import greencity.dto.bag.ReasonNotTakeBagDto;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.employee.EmployeePositionDtoRequest;
import greencity.dto.order.*;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.ManualPaymentResponseDto;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.violation.ViolationsInfoDto;
import greencity.enums.SortingOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UBSManagementService {
    /**
     * Method returns payment info.
     *
     * @return {@link PaymentTableInfoDto};
     * @author Struk Nazar
     */
    PaymentTableInfoDto getPaymentInfo(long orderId, Long sumToPay);

    /**
     * Method returns overpayment to user.
     *
     * @return {@link PaymentTableInfoDto};
     * @author Ostap Mykhailivskyi
     */
    PaymentTableInfoDto returnOverpaymentInfo(Long orderId, Long sumToPay, Long marker);

    /**
     * Method returns all certificates.
     *
     * @return List of {@link greencity.entity.order.Certificate} lists.
     * @author Nazar Struk
     */
    PageableDto<CertificateDtoForSearching> getAllCertificates(Pageable page, String columnName,
        SortingOrder sortingOrder);

    /**
     * Method add some points to UserUBS by email.
     *
     * @author Nazar Struk
     */
    void addPointsToUser(AddingPointsToUserDto addingPointsToUserDto);

    /**
     * Method returns all users violations.
     *
     * @return {@link ViolationsInfoDto} count of Users violations with order id
     *         descriptions.
     * @author Nazar Struk
     */
    ViolationsInfoDto getAllUserViolations(String email);

    /**
     * Method that read user address by order id.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link ReadAddressByOrderDto} that contains one address;
     * @author Mahdziak Orest
     */
    ReadAddressByOrderDto getAddressByOrderId(Long orderId);

    /**
     * Method that update address.
     *
     * @param dtoUpdate of {@link OrderAddressExportDetailsDtoUpdate} order id;
     * @param email     {@link String}.
     * @return {@link OrderAddressDtoResponse} that contains address;
     * @author Mahdziak Orest
     */
    Optional<OrderAddressDtoResponse> updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Long orderId,
        String email);

    /**
     * Method for getting order detail by language and order id.
     *
     * @author Mahdziak Orest
     */
    List<OrderDetailInfoDto> getOrderDetails(Long orderId, String language);

    /**
     * Method for updating order details.
     *
     * @author Mahdziak Orest
     */
    void setOrderDetail(Long orderId,
        Map<Integer, Integer> confirmed, Map<Integer, Integer> exported, String email);

    /**
     * Method that count sum order.
     *
     * @author Mahdziak Orest
     */
    CounterOrderDetailsDto getOrderSumDetails(Long id);

    /**
     * Method that returns some info about all orders for specified userID.
     *
     * @author Oleksandr Khomiakov
     */
    List<OrderInfoDto> getOrdersForUser(String uuid);

    /**
     * Method that returns order related data.
     *
     * @return {@link OrderStatusPageDto}.
     * @author Oleksandr Khomiakov
     */
    OrderStatusPageDto getOrderStatusData(Long orderId, String email);

    /**
     * Method that gets bags information.
     *
     * @author Nazar Struk
     */
    List<DetailsOrderInfoDto> getOrderBagsDetails(Long orderId);

    /**
     * Method that get order and payment status.
     *
     * @author Mahdziak Orest
     */
    OrderDetailStatusDto getOrderDetailStatus(Long id);

    /**
     * Method that update order and payment status.
     *
     * @author Mahdziak Orest
     */
    OrderDetailStatusDto updateOrderDetailStatus(Long id, OrderDetailStatusRequestDto dto, String email);

    /**
     * Method that get export details by order id.
     *
     * @author Mahdziak Orest
     */
    ExportDetailsDto getOrderExportDetails(Long id);

    /**
     * Method that update export details by order id.
     *
     * @author Mahdziak Orest
     */
    ExportDetailsDto updateOrderExportDetails(Long id, ExportDetailsDtoUpdate dto, String uuid);

    /**
     * Method that gets bags additional information.
     *
     * @author Nazar Struk
     */
    List<AdditionalBagInfoDto> getAdditionalBagsInfo(Long orderId);

    /**
     * Method that saves manual payment and returns response with required fields.
     *
     * @param orderId           of {@link Long} order id;
     * @param paymentRequestDto of {@link ManualPaymentRequestDto} manual payment
     *                          request dto;
     * @param image             {@link MultipartFile} image of receipt.
     * @param email             {@link String}.
     * @return {@link ManualPaymentResponseDto }
     * @author Denys Kisliak
     */
    ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String email);

    /**
     * Method that deletes manual payment.
     *
     * @param paymentId of {@link Long} payment id;
     * @param uuid      {@link String}.
     * @author Denys Kisliak
     */
    void deleteManualPayment(Long paymentId, String uuid);

    /**
     * Method that updates manual payment and returns response with required fields.
     *
     * @param paymentId         of {@link Long} payment id;
     * @param paymentRequestDto of {@link ManualPaymentRequestDto} manual payment
     *                          request dto;
     * @param image             {@link MultipartFile} image of receipt.
     * @param uuid              {@link String}.
     * @return {@link ManualPaymentResponseDto }
     * @author Denys Kisliak
     */
    ManualPaymentResponseDto updateManualPayment(Long paymentId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String uuid);

    /**
     * Method that return all employees by position.
     *
     * @author Bohdan Fedorkiv
     */
    EmployeePositionDtoRequest getAllEmployeesByPosition(Long id, String email);

    /**
     * Method that save ReasonNotTakeBagDto.
     */
    ReasonNotTakeBagDto saveReason(Long orderId, String description, MultipartFile[] images);

    /**
     * This is method which is save Admin comment.
     *
     * @param adminCommentDto {@link AdminCommentDto}.
     * @param email           {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void saveAdminCommentToOrder(AdminCommentDto adminCommentDto, String email);

    /**
     * This is method updates eco id from the shop for order.
     *
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @param orderId      {@link Long}.
     * @param email        {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void updateEcoNumberForOrder(EcoNumberDto ecoNumberDto, Long orderId, String email);

    /**
     * This is method which is updates admin page info for order.
     * 
     * @param updateOrderPageAdminDto {@link UpdateOrderPageAdminDto}.
     * @param orderId                 {@link Long}.
     * @param email                   {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void updateOrderAdminPageInfo(UpdateOrderPageAdminDto updateOrderPageAdminDto, Long orderId, String lang,
        String email);

    /**
     * This is method which is updates admin page info for all order.
     *
     * @param updateAllOrderPageDto {@link UpdateOrderPageAdminDto}.
     * @param email                 {@link String} currentUser.
     *
     * @author Max Boiarchuk.
     */
    void updateAllOrderAdminPageInfo(UpdateAllOrderPageDto updateAllOrderPageDto, String email, String lang);

    /**
     * Method that add bonuses to user.
     *
     * @param addBonusesToUserDto {@link AddBonusesToUserDto}.
     * @param orderId             {@link Long}.
     * @param email               {@link String}.
     *
     * @author Pavlo Hural.
     */
    AddBonusesToUserDto addBonusesToUser(AddBonusesToUserDto addBonusesToUserDto, Long orderId, String email);

    /**
     * Method returns employee's access status to order.
     *
     * @param orderId {@link Long}.
     * @param email   {@link String}.
     *
     * @author Hlazova Nataliia.
     */
    Boolean checkEmployeeForOrder(Long orderId, String email);

    /**
     * This is method which is updates orders status at 00:00 everyday where date of
     * export equals current date.
     *
     * @author Anatolii Shapiro.
     */
    void updateOrderStatusToExpected();

    /**
     * Method returns reason not taking bag to order.
     *
     * @param orderId {@link Long}.
     * @return {@link ReasonNotTakingBagDto}.
     *
     * @author Kharchenko Volodymyr.
     */
    ReasonNotTakingBagDto getReasonNotTakingBag(Long orderId);
}
