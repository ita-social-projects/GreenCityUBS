package greencity.service.ubs;

import greencity.dto.*;
import greencity.entity.enums.SortingOrder;
import greencity.filters.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UBSManagementService {
    /**
     * Method to group orders into clusters including summary litres and specified
     * coordinates.
     *
     * @param specified          - list of {@link CoordinatesDto}.
     * @param litres             - preferred amount of litres.
     * @param additionalDistance - additional km to radius.
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getClusteredCoordsAlongWithSpecified(Set<CoordinatesDto> specified,
        int litres, double additionalDistance);

    /**
     * Method to group orders into clusters including summary litres.
     *
     * @param distance - preferred distance for clusterization.
     * @param litres   - preferred amount of litres.
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getClusteredCoords(double distance, int litres);

    /**
     * Method returns all undelivered orders including litres.
     *
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getAllUndeliveredOrdersWithLiters();

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
     * @author Ostap Mykhailivskyi
     */
    void returnOverpayment(Long orderId, OverpaymentInfoRequestDto overpaymentInfoRequestDto, String uuid);

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
     * Method returns all certificates with filtering and sorting data.
     *
     * @return List of {@link greencity.entity.order.Certificate} lists.
     * @author Sikhovskiy Rostyslav
     */
    PageableDto<CertificateDtoForSearching> getCertificatesWithFilter(CertificatePage certificatePage,
        CertificateFilterCriteria certificateFilterCriteria);

    /**
     * Method add a certificates.
     *
     * @author Nazar Struk
     */
    void addCertificate(CertificateDtoForAdding add);

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
     * Method for adding violation for user.
     *
     * @param add            {@link AddingViolationsToUserDto}
     * @param multipartFiles {@link MultipartFile}
     * @param uuid           {@link String}.
     * @author Nazar Struk
     */
    void addUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles, String uuid);

    /**
     * Method for send email with description to user.
     *
     * @param dto {@link AddingViolationsToUserDto } order id with description.
     * @author Veremchuk Zakhar.
     */
    void sendNotificationAboutViolation(AddingViolationsToUserDto dto, String language);

    /**
     * Method for getting all values from order table .
     *
     * @author Nazar Struk
     */
    PageableDto<AllFieldsFromTableDto> getAllValuesFromTable(SearchCriteria searchCriteria, int pages, int size,
        String column, String sortingType);

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
     * @param dtoUpdate of {@link OrderAddressDtoUpdate} order id;
     * @param uuid      {@link String}.
     * @return {@link OrderAddressDtoResponse} that contains address;
     * @author Mahdziak Orest
     */
    Optional<OrderAddressDtoResponse> updateAddress(OrderAddressDtoUpdate dtoUpdate, String uuid);

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
    List<OrderDetailInfoDto> setOrderDetail(List<UpdateOrderDetailDto> request, String language, String uuid);

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
    OrderStatusPageDto getOrderStatusData(Long orderId, Long languageId);

    /**
     * Method that gets bags information.
     *
     * @author Nazar Struk
     */
    List<DetailsOrderInfoDto> getOrderBagsDetails(Long orderId);

    /**
     * Method returns detailed information about user violation by order id.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link ViolationDetailInfoDto};
     * @author Rusanovscaia Nadejda
     */
    Optional<ViolationDetailInfoDto> getViolationDetailsByOrderId(Long orderId);

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
    OrderDetailStatusDto updateOrderDetailStatus(Long id, OrderDetailStatusRequestDto dto, String uuid);

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
    ExportDetailsDto updateOrderExportDetails(Long id, ExportDetailsDtoRequest dto, String uuid);

    /**
     * Method that gets bags additional information.
     *
     * @author Nazar Struk
     */
    List<AdditionalBagInfoDto> getAdditionalBagsInfo(Long orderId);

    /**
     * Method deletes violation from database by orderId.
     *
     * @param orderId {@link Long}
     * @param uuid    {@link String}.
     * @author Nadia Rusanovscaia
     */
    void deleteViolation(Long orderId, String uuid);

    /**
     * Method that saves manual payment and returns response with required fields.
     *
     * @param orderId           of {@link Long} order id;
     * @param paymentRequestDto of {@link ManualPaymentRequestDto} manual payment
     *                          request dto;
     * @param image             {@link MultipartFile} image of receipt.
     * @param uuid              {@link String}.
     * @return {@link ManualPaymentResponseDto }
     * @author Denys Kisliak
     */
    ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String uuid);

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
    EmployeePositionDtoRequest getAllEmployeesByPosition(Long id);

    /**
     * Method that update EmployeePositionDtoResponse.
     */
    void updatePositions(EmployeePositionDtoResponse dto, String uuid);

    /**
     * Method for adding violation for user.
     *
     * @param add            {@link AddingViolationsToUserDto}
     * @param multipartFiles {@link MultipartFile}
     * @author Bohdan Melnyk
     */
    void updateUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles, String uuid);

    /**
     * Method that save ReasonNotTakeBagDto.
     */
    ReasonNotTakeBagDto saveReason(Long orderId, String description, List<MultipartFile> images);

    /**
     * This method assign Employee with it's position for current order.
     *
     * @param dto {@link AssignEmployeesForOrderDto}.
     * @author Yuriy Bahlay.
     */
    void assignEmployeesWithThePositionsToTheOrder(AssignEmployeesForOrderDto dto, String uuid);

    /**
     * Method returns all order's data from big order table.
     *
     * @author Ihor Volianskyi
     */
    Page<BigOrderTableDTO> getOrders(OrderPage orderPage, OrderSearchCriteria searchCriteria);

    /**
     * This is method which is save Admin comment.
     *
     * @param adminCommentDto {@link AdminCommentDto}.
     * @param uuid            {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void saveAdminCommentToOrder(AdminCommentDto adminCommentDto, String uuid);

    /**
     * This is method updates eco id from the shop for order.
     *
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @param orderId      {@link Long}.
     * @param uuid         {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    void updateEcoNumberForOrder(List<EcoNumberDto> ecoNumberDto, Long orderId, String uuid);
}
