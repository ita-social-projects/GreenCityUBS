package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.bag.AdditionalBagInfoDto;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.employee.EmployeePositionDtoRequest;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.BigOrderTableDTO;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.order.DetailsOrderInfoDto;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.ExportDetailsDtoUpdate;
import greencity.dto.order.GroupedOrderDto;
import greencity.dto.order.NotTakenOrderReasonDto;
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
import greencity.dto.position.PositionDto;
import greencity.dto.table.CustomTableViewDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.violation.AddingViolationsToUserDto;
import greencity.dto.violation.UpdateViolationToUserDto;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.dto.violation.ViolationsInfoDto;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.user.User;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.service.ubs.CertificateService;
import greencity.service.ubs.CoordinateService;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementService;
import greencity.service.ubs.ViolationService;
import greencity.service.ubs.manager.BigOrderTableServiceView;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/ubs/management")
@RequiredArgsConstructor
public class ManagementOrderController {
    private final UBSManagementService ubsManagementService;
    private final UBSClientService ubsClientService;
    private final CertificateService certificateService;
    private final CoordinateService coordinateService;
    private final ViolationService violationService;
    private final BigOrderTableServiceView bigOrderTableService;

    /**
     * Controller getting all certificates with sorting possibility.
     *
     * @return list of all certificates.
     * @author Nazar Struk
     */
    @ApiOperation(value = "Get all certificates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_CERTIFICATES', authentication)")
    @GetMapping("/getAllCertificates")
    public ResponseEntity<PageableDto<CertificateDtoForSearching>> allCertificates(
        CertificatePage certificatePage,
        CertificateFilterCriteria certificateFilterCriteria) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(certificateService.getCertificatesWithFilter(certificatePage, certificateFilterCriteria));
    }

    /**
     * Controller add certificate.
     *
     * @return httpStatus.
     * @author Nazar Struk
     */

    @ApiOperation("Add Certificate")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@preAuthorizer.hasAuthority('CREATE_NEW_CERTIFICATE', authentication)")
    @PostMapping("/addCertificate")
    public ResponseEntity<HttpStatus> addCertificate(
        @Valid @RequestBody CertificateDtoForAdding certificateDtoForAdding) {
        certificateService.addCertificate(certificateDtoForAdding);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Controller delete certificate.
     *
     * @param code {@link String}.
     * @return {@link HttpStatus} - http status.
     * @author Hlazova Nataliia
     */

    @ApiOperation("Delete Certificate")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_CERTIFICATE', authentication)")
    @DeleteMapping("/deleteCertificate/{code}")
    public ResponseEntity<HttpStatus> deleteCertificate(
        @Valid @PathVariable String code) {
        certificateService.deleteCertificate(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller finds undelivered orders.
     *
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get all undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })

    @GetMapping("/all-undelivered")
    public ResponseEntity<List<GroupedOrderDto>> allUndeliveredCoords(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(coordinateService.getAllUndeliveredOrdersWithLiters());
    }

    /**
     * Controller groups undelivered orders.
     *
     * @param radius {@link Double} preferred searching radius.
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get grouped undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedOrderDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/group-undelivered")
    public ResponseEntity<List<GroupedOrderDto>> groupCoords(@RequestParam Double radius,
        @RequestParam(required = false, defaultValue = "3000") Integer litres) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(coordinateService.getClusteredCoords(radius, litres));
    }

    /**
     * Controller groups orders along with specified.
     */
    @ApiOperation(value = "Get grouped orders along with specified.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedOrderDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/group-undelivered-with-specified")
    public ResponseEntity<List<GroupedOrderDto>> groupCoordsWithSpecifiedOnes(
        @Valid @RequestBody Set<CoordinatesDto> specified,
        @RequestParam(required = false, defaultValue = "3000") Integer litres,
        @RequestParam(required = false, defaultValue = "0") Double additionalDistance) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(coordinateService.getClusteredCoordsAlongWithSpecified(specified, litres, additionalDistance));
    }

    /**
     * Controller adding points to user by Email.
     *
     * @return httpStatus.
     * @author Nazar Struk
     */
    @ApiOperation("Add Points to User")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping(value = "/addPointsToUser")
    public ResponseEntity<HttpStatus> addPointsToUser(
        @Valid @RequestBody AddingPointsToUserDto addingPointsToUserDto) {
        ubsManagementService.addPointsToUser(addingPointsToUserDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller for getting User violations.
     *
     * @return {@link ViolationsInfoDto} count of Users violations with order id
     *         descriptions
     * @author Nazar Struk
     */
    @ApiOperation("Get User violations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/getUsersViolations")
    public ResponseEntity<ViolationsInfoDto> getUserViolations(@Valid @Email @RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getAllUserViolations(email));
    }

    /**
     * Controller for adding User violation.
     *
     * @return {@link AddingViolationsToUserDto} count of Users violations with
     *         order id descriptions.
     * @author Bohdan Melnyk
     */
    @ApiOperation("Add Violation to User")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = AddingViolationsToUserDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping(value = "/addViolationToUser",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<HttpStatus> addUsersViolation(@Valid @RequestPart AddingViolationsToUserDto add,
        @RequestPart(required = false) @Nullable MultipartFile[] files,
        Principal principal) {
        violationService.addUserViolation(add, files, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * The method returns page with all order's data from big order table.
     *
     * @return {@link Page} with order's information
     * @author Ihor Volianskyi
     */
    @ApiOperation("Get all order's data from big order table")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @GetMapping("/bigOrderTable")
    public ResponseEntity<Page<BigOrderTableDTO>> getOrders(OrderPage page,
        OrderSearchCriteria criteria,
        Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(bigOrderTableService.getOrders(page, criteria, principal.getName()));
    }

    /**
     * The method save or update view of order table. This method only save Uuid of
     * user and titles of columns in DataBase. All changes actually take place at
     * the Front-end side.
     *
     * @author Sikhovskiy Rostyslav
     */
    @ApiOperation("Save or update Parameters for custom orders table view")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @PutMapping("/changeOrdersTableView")
    public ResponseEntity<CustomTableView> setCustomTable(@ApiIgnore @CurrentUserUuid String uuid,
        String titles) {
        bigOrderTableService.changeOrderTableView(uuid, titles);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * The method return parameters for custom orders table view. This method only
     * gets parameters of user and sends it to Front-end.
     *
     * @author Sikhovskiy Rostyslav
     */
    @ApiOperation("Get parameters for custom orders table view")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @GetMapping("/getOrdersViewParameters")
    public ResponseEntity<CustomTableViewDto> getCustomTableParameters(@ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(bigOrderTableService.getCustomTableParameters(uuid));
    }

    /**
     * Controller read address by order id.
     *
     * @param id {@link Long}.
     * @return {@link HttpStatus} - http status.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Get address by order id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ReadAddressByOrderDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/read-address-order/{id}")
    public ResponseEntity<ReadAddressByOrderDto> getAddressByOrderId(
        @Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getAddressByOrderId(id));
    }

    /**
     * Controller returns information about order payments.
     *
     * @return list of {@link PaymentTableInfoDto}.
     * @author Nazar Struk
     */
    @ApiOperation(value = "Get information about order payments.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PaymentTableInfoDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/getPaymentInfo")
    public ResponseEntity<PaymentTableInfoDto> paymentInfo(@RequestParam long orderId,
        @RequestParam Double sumToPay) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getPaymentInfo(orderId, sumToPay));
    }

    /**
     * Controller for get order info.
     *
     * @return {@link List OrderDetailInfoDto}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Get order detail info")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderDetailInfoDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/read-order-info/{id}")
    public ResponseEntity<List<OrderDetailInfoDto>> getOrderInfo(
        @Valid @PathVariable("id") Long id, @RequestParam String language) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getOrderDetails(id, language));
    }

    /**
     * Controller for calculate order sum.
     *
     * @return {@link CounterOrderDetailsDto}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Get order sum details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = CounterOrderDetailsDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-order-sum-detail/{id}")
    public ResponseEntity<CounterOrderDetailsDto> getOrderSumDetails(
        @Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getOrderSumDetails(id));
    }

    /**
     * Controller for getting bags information.
     *
     * @author Nazar Struk
     */
    @ApiOperation(value = "Get bags info")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/getOrderBagsInfo/{id}")
    public ResponseEntity<List<DetailsOrderInfoDto>> getOrderBagsInfo(
        @Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getOrderBagsDetails(id));
    }

    /**
     * Controller gets details of user rule violation added to the current order.
     *
     * @param orderId {@link Long}.
     * @return {@link ViolationDetailInfoDto} gets details of user rule violation
     *         added to the current order
     */
    @ApiOperation("Get details of user violation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ViolationDetailInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/violation-details/{orderId}")
    public ResponseEntity<ViolationDetailInfoDto> getViolationDetailsForCurrentOrder(
        @Valid @PathVariable("orderId") Long orderId) {
        Optional<ViolationDetailInfoDto> violationDetailsByOrderId =
            violationService.getViolationDetailsByOrderId(orderId);
        if (violationDetailsByOrderId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Violation not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                .body(violationDetailsByOrderId.get());
        }
    }

    /**
     * Controller for get order detail status.
     *
     * @return {@link OrderDetailStatusDto}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Get order detail status")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderDetailStatusDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/read-order-detail-status/{id}")
    public ResponseEntity<OrderDetailStatusDto> getOrderDetailStatus(
        @Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getOrderDetailStatus(id));
    }

    /**
     * Controller for update order and payment status by id.
     *
     * @return {@link OrderDetailStatusDto}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Update order detail status")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.CREATED, response = OrderDetailStatusDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_ORDER', authentication)")
    @PutMapping("/update-order-detail-status/{id}")
    public ResponseEntity<OrderDetailStatusDto> updateOrderDetailStatus(
        @Valid @PathVariable("id") Long id, @RequestBody OrderDetailStatusRequestDto dto,
        Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.updateOrderDetailStatusById(id, dto, principal.getName()));
    }

    /**
     * Controller for get export details by order id.
     *
     * @return {@link ExportDetailsDto}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Get export details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ExportDetailsDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-order-export-details/{id}")
    public ResponseEntity<ExportDetailsDto> getOrderExportInfo(
        @Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getOrderExportDetails(id));
    }

    /**
     * Controller for getting all user orders.
     *
     * @return {@link List OrderInfoDto}.
     * @author Oleksandr Khomiakov
     */
    @ApiOperation(value = "returns all user orders for specified uuid")
    @ApiResponses({
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderInfoDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-all-orders/{uuid}")
    public ResponseEntity<List<OrderInfoDto>> getAllDataForOrder(
        @PathVariable("uuid") String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getOrdersForUser(uuid));
    }

    /**
     * Controller for getting order related data.
     *
     * @return {@link OrderStatusPageDto}.
     * @author Oleksandr Khomiakov
     */
    @ApiOperation(value = "Controller for getting order related data")
    @ApiResponses({
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderStatusPageDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-data-for-order/{id}")
    public ResponseEntity<OrderStatusPageDto> getDataForOrderStatusPage(
        @PathVariable(name = "id") Long orderId,
        Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getOrderStatusData(orderId, principal.getName()));
    }

    /**
     * Controller to check current employee for order.
     *
     * @return {@link Boolean}.
     * @author Hlazova Nataliia
     */
    @ApiOperation(value = "Controller to check current employee for order")
    @ApiResponses({
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderStatusPageDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/check-employee-for-order/{id}")
    public ResponseEntity<Boolean> checkEmployeeForOrderPage(
        @PathVariable(name = "id") Long orderId,
        Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.checkEmployeeForOrder(orderId, principal.getName()));
    }

    /**
     * Controller for update export details.
     *
     * @return {@link ExportDetailsDto}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Update export details")
    @ApiResponses({
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ExportDetailsDtoUpdate.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_ORDER', authentication)")
    @PutMapping("/update-order-export-details/{id}")
    public ResponseEntity<ExportDetailsDto> updateOrderExportInfo(
        @Valid @PathVariable("id") Long id, @RequestBody ExportDetailsDtoUpdate dto,
        Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.updateOrderExportDetailsById(id, dto, principal.getName()));
    }

    /**
     * Controller for getting bags additional information.
     *
     * @author Nazar Struk
     */
    @ApiOperation(value = "Get bags additional info")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/getAdditionalOrderBagsInfo/{id}")
    public ResponseEntity<List<AdditionalBagInfoDto>> getAdditionalOrderBagsInfo(
        @Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getAdditionalBagsInfo(id));
    }

    /**
     * Controller deletes violation from order.
     *
     * @return {@link HttpStatus}
     * @author Nadia Rusanovscaia.
     */
    @ApiOperation(value = "Delete violation from order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ViolationDetailInfoDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete-violation-from-order/{orderId}")
    public ResponseEntity<HttpStatus> deleteViolationFromOrder(@PathVariable Long orderId,
        @ApiIgnore @CurrentUserUuid String uuid) {
        violationService.deleteViolation(orderId, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller returns overpayment as bonuses information.
     *
     * @param orderId  {@link Long}.
     * @param sumToPay {@link Long}.
     * @return list of {@link PaymentTableInfoDto}.
     * @author Ostap Mykhailivskyi
     */
    @ApiOperation(value = "Return overpayment as bonuses information")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/return-overpayment-as-bonuses-info")
    public ResponseEntity<PaymentTableInfoDto> returnOverpaymentAsBonusesInfo(@RequestParam Long orderId,
        @RequestParam Double sumToPay) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.returnOverpaymentInfo(orderId, sumToPay, 2L));
    }

    /**
     * Controller returns overpayment as money information.
     *
     * @param orderId  {@link Long}.
     * @param sumToPay {@link Long}.
     * @return list of {@link PaymentTableInfoDto}.
     * @author Ostap Mykhailivskyi
     */
    @ApiOperation(value = "Return overpayment as Money information")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/return-overpayment-as-money-info")
    public ResponseEntity<PaymentTableInfoDto> returnOverpaymentAsMoneyInfo(@RequestParam Long orderId,
        @RequestParam Double sumToPay) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.returnOverpaymentInfo(orderId, sumToPay, 1L));
    }

    /**
     * Controller saves manual payment.
     *
     * @param orderId          {@link Long}.
     * @param manualPaymentDto {@link ManualPaymentRequestDto}
     * @return {@link ManualPaymentResponseDto}
     * @author Denys Kisliak.
     */
    @ApiOperation(value = "Save manual payment")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ManualPaymentResponseDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PostMapping(value = "/add-manual-payment/{id}",
        consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ManualPaymentResponseDto> addManualPayment(@PathVariable(name = "id") Long orderId,
        @Valid @RequestPart ManualPaymentRequestDto manualPaymentDto,
        @RequestPart(required = false) MultipartFile image, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.saveNewManualPayment(orderId, manualPaymentDto, image, principal.getName()));
    }

    /**
     * Controller deletes manual payment.
     *
     * @param paymentId {@link Long}.
     * @return {@link HttpStatus}
     * @author Denys Kisliak.
     */
    @ApiOperation(value = "Delete manual payment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete-manual-payment/{id}")
    public ResponseEntity<ResponseStatus> deleteManualPayment(@PathVariable(name = "id") Long paymentId,
        @ApiIgnore @CurrentUserUuid String uuid) {
        ubsManagementService.deleteManualPayment(paymentId, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller updates manual payment.
     *
     * @param paymentId        {@link Long}.
     * @param manualPaymentDto {@link ManualPaymentRequestDto}
     * @return {@link ManualPaymentResponseDto}
     * @author Denys Kisliak.
     */
    @ApiOperation(value = "Update manual payment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ManualPaymentResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping(value = "/update-manual-payment/{id}",
        consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ManualPaymentResponseDto> updateManualPayment(@PathVariable(name = "id") Long paymentId,
        @Valid @RequestPart ManualPaymentRequestDto manualPaymentDto,
        @RequestPart(required = false) MultipartFile image, @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.updateManualPayment(paymentId, manualPaymentDto, image, uuid));
    }

    /**
     * Controller for get employees depends on position.
     *
     * @return {EmployeePositionDtoRequest}.
     * @author Bohdan Fedorkiv
     */

    @ApiOperation(value = "Get all employee by positions")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PositionDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-all-employee-by-position/{id}")
    public ResponseEntity<EmployeePositionDtoRequest> getAllEmployeeByPosition(@Valid @PathVariable("id") Long orderId,
        Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getAllEmployeesByPosition(orderId, principal.getName()));
    }

    /**
     * Controller for updating User violation.
     *
     * @author Bohdan Melnyk
     */
    @ApiOperation("Update Violation to User")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = UpdateViolationToUserDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @ResponseStatus(value = HttpStatus.CREATED)
    @PutMapping(value = "/updateViolationToUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpStatus> updateUsersViolation(@Valid @RequestPart UpdateViolationToUserDto add,
        @Nullable @RequestPart(required = false) MultipartFile[] multipartFiles,
        @ApiIgnore @CurrentUserUuid String uuid) {
        violationService.updateUserViolation(add, multipartFiles, uuid);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Controller for saving Admin comment.
     *
     * @param adminCommentDto {@link AdminCommentDto}.
     * @author Bahlay Yuriy.
     */
    @ApiOperation(value = "Save admin comment")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PostMapping("/save-admin-comment")
    public ResponseEntity<HttpStatus> saveAdminCommentToOrder(
        @RequestBody @Valid AdminCommentDto adminCommentDto,
        Principal principal) {
        ubsManagementService.saveAdminCommentToOrder(adminCommentDto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for updating Id From eco-store for order.
     * 
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @author Bahlay Yuriy.
     */
    @ApiOperation(value = "update eco-store id for order")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/update-eco-store{id}")
    public ResponseEntity<HttpStatus> updateEcoStoreIdToOrder(
        @RequestBody @Valid EcoNumberDto ecoNumberDto, @PathVariable(name = "id") Long orderId,
        Principal principal) {
        ubsManagementService.updateEcoNumberForOrderById(ecoNumberDto, orderId, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for updating order admin page info and save reason if needed.
     *
     * @param orderId                 {@link Long}.
     * @param updateOrderPageAdminDto {@link UpdateOrderPageAdminDto}.
     * @param language                {@link String}.
     * @param principal               {@link Principal}.
     * @param description             {@link String}.
     * @param images                  {@link MultipartFile}.
     *
     * @author Bahlay Yuriy.
     * @author Anton Bondar.
     */

    @ApiOperation(value = "update order admin page info and save reason if needed")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_ORDER', authentication)")
    @PatchMapping(value = "/update-order-page-admin-info/{id}",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<HttpStatus> updatePageAdminInfo(@PathVariable(name = "id") Long orderId,
        @Valid @RequestPart UpdateOrderPageAdminDto updateOrderPageAdminDto,
        @RequestParam String language,
        @ApiIgnore Principal principal,
        @RequestParam String description,
        @RequestPart(required = false) @Nullable MultipartFile[] images) {
        ubsManagementService.updateOrderAdminPageInfoAndSaveReason(orderId, updateOrderPageAdminDto, language,
            principal.getName(), description, images);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for updating all order admin page info.
     *
     * @param updateAllOrderPageDto {@link UpdateAllOrderPageDto}.
     * @param lang                  {@link String} language
     * @author Max Boiarchuk.
     */
    @ApiOperation(value = "update all order admin page info")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/all-order-page-admin-info")
    public ResponseEntity<HttpStatus> updateAllOrderPageAdminInfo(
        @RequestBody @Valid UpdateAllOrderPageDto updateAllOrderPageDto, Principal principal,
        @RequestParam String lang) {
        ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, principal.getName(), lang);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for adding bonuses to user.
     *
     * @param orderId             {@link Long}.
     * @param addBonusesToUserDto {@link AddBonusesToUserDto}.
     *
     * @author Pavlo Hural.
     */
    @ApiOperation(value = "add bonuses to user")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping(value = "/add-bonuses-user/{id}")
    public ResponseEntity<AddBonusesToUserDto> addBonusesToUser(@PathVariable(name = "id") Long orderId,
        @RequestBody @Valid AddBonusesToUserDto addBonusesToUserDto, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.addBonusesToUser(addBonusesToUserDto, orderId, principal.getName()));
    }

    /**
     * Controller for get order cancellation reason.
     *
     * @return {@link OrderCancellationReasonDto}.
     * @author Kharchenko Volodymyr
     */
    @ApiOperation(value = "Get order cancellation reason")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderCancellationReasonDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-order-cancellation-reason/{id}")
    public ResponseEntity<OrderCancellationReasonDto> getOrderCancellationReason(
        @Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getOrderCancellationReason(id));
    }

    /**
     * Controller for get not taken order reason.
     *
     * @param orderId {@link Long}.
     * @return {@link NotTakenOrderReasonDto}.
     *
     * @author Kharchenko Volodymyr
     */
    @ApiOperation(value = "Get not taken order reason")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = NotTakenOrderReasonDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-not-taken-order-reason/{id}")
    public ResponseEntity<NotTakenOrderReasonDto> getNotTakenOrderReason(
        @Valid @PathVariable("id") Long orderId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getNotTakenOrderReason(orderId));
    }

    /**
     * Controller updates info about order cancellation reason.
     *
     * @param id   {@link Long}.
     * @param dto  {@link OrderCancellationReasonDto}
     * @param uuid current {@link User}'s uuid.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "updates info about order cancellation reason ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderCancellationReasonDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/order/{id}/cancellation")
    public ResponseEntity<OrderCancellationReasonDto> updateCancellationReason(
        @RequestBody final OrderCancellationReasonDto dto,
        @PathVariable("id") final Long id,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.updateOrderCancellationReason(id, dto, uuid));
    }

    /**
     * Controller saves order ID of order for which we need to make a refund.
     *
     * @param orderId {@link Long}.
     * @return {@link HttpStatus} - http status.
     *
     * @author Anton Bondar
     */
    @ApiOperation(value = "saves order ID of order for which we need to make a refund")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/save-order-for-refund/{orderId}")
    public ResponseEntity<HttpStatus> saveOrderIdForRefund(
        @Valid @PathVariable("orderId") Long orderId) {
        ubsManagementService.saveOrderIdForRefund(orderId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
