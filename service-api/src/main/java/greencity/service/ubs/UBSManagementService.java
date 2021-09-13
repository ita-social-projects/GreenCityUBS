package greencity.service.ubs;

import greencity.dto.*;
import greencity.filters.SearchCriteria;
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
    void returnOverpayment(Long orderId,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto);

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
    PageableDto<CertificateDtoForSearching> getAllCertificates(Pageable page);

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
     *
     * @param multipartFiles {@link MultipartFile}
     *
     * @author Nazar Struk
     */
    void addUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles);

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
    PageableDto<AllFieldsFromTableDto> getAllValuesFromTable(SearchCriteria searchCriteria, int pages, int size);

    /**
     * Method for getting all sorted values from table .
     *
     * @author Nazar Struk
     */
    PageableDto<AllFieldsFromTableDto> getAllSortedValuesFromTable(String column, String sortingType, int pages,
        int size);

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
     * @return {@link OrderAddressDtoResponse} that contains address;
     * @author Mahdziak Orest
     */
    OrderAddressDtoResponse updateAddress(OrderAddressDtoUpdate dtoUpdate);

    /**
     * Method that gets all order payments.
     *
     * @return {@link PaymentTableInfoDto};
     * @author Struk Nazar
     */

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
    List<OrderDetailInfoDto> setOrderDetail(List<UpdateOrderDetailDto> request, String language);

    /**
     * Method that count sum order.
     *
     * @author Mahdziak Orest
     */
    CounterOrderDetailsDto getOrderSumDetails(Long id);

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
    OrderDetailStatusDto updateOrderDetailStatus(Long id, OrderDetailStatusRequestDto dto);

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
    ExportDetailsDto updateOrderExportDetails(Long id, ExportDetailsDtoRequest dto);

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
     * @author Nadia Rusanovscaia
     */
    void deleteViolation(Long orderId);

    /**
     * Method that saves manual payment and returns response with required fields.
     *
     * @param orderId           of {@link Long} order id;
     * @param paymentRequestDto of {@link ManualPaymentRequestDto} manual payment
     *                          request dto;
     * @param image             {@link MultipartFile} image of receipt.
     * @return {@link ManualPaymentResponseDto }
     * @author Denys Kisliak
     */
    ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image);

    /**
     * Method that deletes manual payment.
     *
     * @param paymentId of {@link Long} payment id;
     * @author Denys Kisliak
     */
    void deleteManualPayment(Long paymentId);

    /**
     * Method that updates manual payment and returns response with required fields.
     *
     * @param paymentId         of {@link Long} payment id;
     * @param paymentRequestDto of {@link ManualPaymentRequestDto} manual payment
     *                          request dto;
     * @param image             {@link MultipartFile} image of receipt.
     * @return {@link ManualPaymentResponseDto }
     * @author Denys Kisliak
     */
    ManualPaymentResponseDto updateManualPayment(Long paymentId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image);

    /**
     * Method that return all employees by position.
     *
     * @author Bohdan Fedorkiv
     */
    EmployeePositionDtoRequest getAllEmployeesByPosition(Long id);

    /**
     * Method that update EmployeePositionDtoResponse.
     */
    void updatePositions(EmployeePositionDtoResponse dto);

    /**
     * Method for adding violation for user.
     *
     * @param add            {@link AddingViolationsToUserDto}
     *
     * @param multipartFiles {@link MultipartFile}
     *
     * @author Bohdan Melnyk
     */
    void updateUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles);

    /**
     * Method that return parameters for building table on admin's page.
     *
     * @param userId of {@link Long} administrator's user id;
     * @author Liubomyr Pater
     */
    TableParamsDTO getParametersForOrdersTable(Long userId);

    /**
     * Method that return orders table on admin's page after saving changes.
     *
     * @param userUuid of {@link String} manager's user uuid;
     * @param requestToChangeOrdersDataDTO of {@link RequestToChangeOrdersDataDTO} column & value that need to update;
     * @author Liubomyr Pater
     */
    PageableDto<AllFieldsFromTableDto> changeOrdersDataSwitcher(String userUuid, RequestToChangeOrdersDataDTO requestToChangeOrdersDataDTO);
}
