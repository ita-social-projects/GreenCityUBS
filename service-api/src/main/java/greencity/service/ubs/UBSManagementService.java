package greencity.service.ubs;

import greencity.dto.bag.AdditionalBagInfoDto;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.employee.EmployeePositionDtoRequest;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.order.DetailsOrderInfoDto;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.ExportDetailsDtoUpdate;
import greencity.dto.order.NotTakenOrderReasonDto;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailInfoDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderDetailStatusRequestDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.OrderStatusPageDto;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.order.UpdateOrderPageAdminDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.ManualPaymentResponseDto;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.violation.ViolationsInfoDto;
import greencity.entity.order.Order;
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
    PaymentTableInfoDto getPaymentInfo(long orderId, Double sumToPay);

    /**
     * Method returns overpayment to user.
     *
     * @return {@link PaymentTableInfoDto};
     * @author Ostap Mykhailivskyi
     */
    PaymentTableInfoDto returnOverpaymentInfo(Long orderId, Double sumToPay, Long marker);

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
     * @param dtoUpdate of {@link OrderAddressExportDetailsDtoUpdate} order id.
     * @param order     {@link Order}.
     * @param email     {@link String}.
     * @return {@link OrderAddressDtoResponse} that contains address.
     * @author Mahdziak Orest
     */
    Optional<OrderAddressDtoResponse> updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Order order,
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
    void setOrderDetail(Order order,
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
     * Method that update order and payment status by id.
     *
     * @author Mahdziak Orest
     */
    OrderDetailStatusDto updateOrderDetailStatusById(Long id, OrderDetailStatusRequestDto dto, String email);

    /**
     * Method that update order and payment status.
     *
     * @author Mahdziak Orest
     */
    OrderDetailStatusDto updateOrderDetailStatus(Order order, OrderDetailStatusRequestDto dto, String email);

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
    ExportDetailsDto updateOrderExportDetailsById(Long id, ExportDetailsDtoUpdate dto, String uuid);

    /**
     * Method that update export details by order.
     *
     * @author Mahdziak Orest
     */
    ExportDetailsDto updateOrderExportDetails(Order order, ExportDetailsDtoUpdate dto, String uuid);

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
    void saveReason(Order order, String description, MultipartFile[] images);

    /**
     * This is method which save Admin comment.
     *
     * @param adminCommentDto {@link AdminCommentDto}.
     * @param email           {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void saveAdminCommentToOrder(AdminCommentDto adminCommentDto, String email);

    /**
     * This is method updates eco id from the shop for order by id.
     *
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @param orderId      {@link Long}.
     * @param email        {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void updateEcoNumberForOrderById(EcoNumberDto ecoNumberDto, Long orderId, String email);

    /**
     * This is method updates eco id from the shop for order.
     *
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @param order        {@link Order}.
     * @param email        {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void updateEcoNumberForOrder(EcoNumberDto ecoNumberDto, Order order, String email);

    /**
     * This is method which is updates admin page info for order and save reason.
     *
     * @param orderId                 {@link Long}.
     * @param updateOrderPageAdminDto {@link UpdateOrderPageAdminDto}.
     * @param language                {@link String}.
     * @param email                   {@link String}.
     *
     * @author Anton Bondar.
     */
    void updateOrderAdminPageInfoAndSaveReason(Long orderId, UpdateOrderPageAdminDto updateOrderPageAdminDto,
        String language, String email);

    /**
     * This is method which is updates admin page info for order.
     * 
     * @param updateOrderPageAdminDto {@link UpdateOrderPageAdminDto}.
     * @param order                   {@link Order}.
     * @param email                   {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void updateOrderAdminPageInfo(UpdateOrderPageAdminDto updateOrderPageAdminDto, Order order, String lang,
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
     * Method returns cancellation reason and comment to order.
     *
     * @param orderId {@link Long}.
     * @return {@link OrderCancellationReasonDto}
     *
     * @author Kharchenko Volodymyr.
     */
    OrderCancellationReasonDto getOrderCancellationReason(Long orderId);

    /**
     * Method returns not taken order reason.
     *
     * @param orderId {@link Long}.
     * @return {@link NotTakenOrderReasonDto}.
     *
     * @author Kharchenko Volodymyr.
     */
    NotTakenOrderReasonDto getNotTakenOrderReason(Long orderId);

    /**
     * Method saves order ID of order for which we need to make a refund.
     *
     * @param orderId {@link Long}.
     *
     * @author Anton Bondar.
     */
    void saveOrderIdForRefund(Long orderId);
}
