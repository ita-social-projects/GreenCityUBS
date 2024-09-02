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
import greencity.dto.table.CustomTableViewDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.violation.AddingViolationsToUserDto;
import greencity.dto.violation.UpdateViolationToUserDto;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.dto.violation.ViolationsInfoDto;
import greencity.entity.parameters.CustomTableView;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.service.ubs.CertificateService;
import greencity.service.ubs.CoordinateService;
import greencity.service.ubs.PaymentService;
import greencity.service.ubs.UBSManagementService;
import greencity.service.ubs.ViolationService;
import greencity.service.ubs.manager.BigOrderTableServiceView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/ubs/management")
@RequiredArgsConstructor
public class ManagementOrderController {
    private final UBSManagementService ubsManagementService;
    private final CertificateService certificateService;
    private final CoordinateService coordinateService;
    private final ViolationService violationService;
    private final BigOrderTableServiceView bigOrderTableService;
    private final PaymentService paymentService;

    /**
     * Controller getting all certificates with sorting possibility.
     *
     * @return list of all certificates.
     * @author Nazar Struk
     */
    @Operation(summary = "Get all certificates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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

    @Operation(summary = "Add Certificate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content)
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
     * @param responseCode {@link String}.
     * @return {@link HttpStatus} - http status.
     * @author Hlazova Nataliia
     */

    @Operation(summary = "Delete Certificate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_CERTIFICATE', authentication)")
    @DeleteMapping("/deleteCertificate/{responseCode}")
    public ResponseEntity<HttpStatus> deleteCertificate(
        @Valid @PathVariable String responseCode) {
        certificateService.deleteCertificate(responseCode);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller finds undelivered orders.
     *
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @Operation(summary = "Get all undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })

    @GetMapping("/all-undelivered")
    public ResponseEntity<List<GroupedOrderDto>> allUndeliveredCoords() {
        return ResponseEntity.status(HttpStatus.OK).body(coordinateService.getAllUndeliveredOrdersWithLiters());
    }

    /**
     * Controller groups undelivered orders.
     *
     * @param radius {@link Double} preferred searching radius.
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @Operation(summary = "Get grouped undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupedOrderDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
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
    @Operation(summary = "Get grouped orders along with specified.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupedOrderDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Add Points to User")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
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
    @Operation(summary = "Get User violations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Add Violation to User")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PostMapping(value = "/addViolationToUser",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
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
    @Operation(summary = "Get all order's data from big order table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
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
    @Operation(summary = "Save or update Parameters for custom orders table view")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @PutMapping("/changeOrdersTableView")
    public ResponseEntity<CustomTableView> setCustomTable(@Parameter(hidden = true) @CurrentUserUuid String uuid,
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
    @Operation(summary = "Get parameters for custom orders table view")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @GetMapping("/getOrdersViewParameters")
    public ResponseEntity<CustomTableViewDto> getCustomTableParameters(
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
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
    @Operation(summary = "Get address by order id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ReadAddressByOrderDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Get information about order payments.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PaymentTableInfoDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/getPaymentInfo")
    public ResponseEntity<PaymentTableInfoDto> paymentInfo(@RequestParam long orderId,
        @RequestParam Double sumToPay) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(paymentService.getPaymentInfo(orderId, sumToPay));
    }

    /**
     * Controller for get order info.
     *
     * @return {@link List OrderDetailInfoDto}.
     * @author Orest Mahdziak
     */
    @Operation(summary = "Get order detail info")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDetailInfoDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Get order sum details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = CounterOrderDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Get bags info")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Get details of user violation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ViolationDetailInfoDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Get order detail status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderDetailStatusDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Update order detail status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = OrderDetailStatusDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Get export details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ExportDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "returns all user orders for specified uuid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderInfoDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Controller for getting order related data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderStatusPageDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Controller to check current employee for order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderStatusPageDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Update export details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = ExportDetailsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Get bags additional info")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Delete violation from order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @DeleteMapping("/delete-violation-from-order/{orderId}")
    public ResponseEntity<HttpStatus> deleteViolationFromOrder(@PathVariable Long orderId,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        violationService.deleteViolation(orderId, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller saves manual payment.
     *
     * @param orderId          {@link Long}.
     * @param manualPaymentDto {@link ManualPaymentRequestDto}
     * @return {@link ManualPaymentResponseDto}
     * @author Denys Kisliak.
     */
    @Operation(summary = "Save manual payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = ManualPaymentResponseDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY, content = @Content)
    })
    @PostMapping(value = "/add-manual-payment/{id}",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ManualPaymentResponseDto> addManualPayment(@PathVariable(name = "id") Long orderId,
        @Valid @RequestPart ManualPaymentRequestDto manualPaymentDto,
        @RequestPart(required = false) MultipartFile image, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentService.saveNewManualPayment(orderId, manualPaymentDto, image, principal.getName()));
    }

    /**
     * Controller deletes manual payment.
     *
     * @param paymentId {@link Long}.
     * @return {@link HttpStatus}
     * @author Denys Kisliak.
     */
    @Operation(summary = "Delete manual payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @DeleteMapping("/delete-manual-payment/{id}")
    public ResponseEntity<ResponseStatus> deleteManualPayment(@PathVariable(name = "id") Long paymentId,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        paymentService.deleteManualPayment(paymentId, uuid);
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
    @Operation(summary = "Update manual payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ManualPaymentResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PutMapping(value = "/update-manual-payment/{id}",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ManualPaymentResponseDto> updateManualPayment(@PathVariable(name = "id") Long paymentId,
        @Valid @RequestPart ManualPaymentRequestDto manualPaymentDto,
        @RequestPart(required = false) MultipartFile image, @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(paymentService.updateManualPayment(paymentId, manualPaymentDto, image, uuid));
    }

    /**
     * Controller for get employees depends on position.
     *
     * @return {EmployeePositionDtoRequest}.
     * @author Bohdan Fedorkiv
     */

    @Operation(summary = "Get all employee by positions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EmployeePositionDtoRequest.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Update Violation to User")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @ApiLocale
    @ResponseStatus(value = HttpStatus.CREATED)
    @PutMapping(value = "/updateViolationToUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpStatus> updateUsersViolation(@Valid @RequestPart UpdateViolationToUserDto add,
        @Nullable @RequestPart(required = false) MultipartFile[] multipartFiles,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        violationService.updateUserViolation(add, multipartFiles, uuid);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Controller for saving Admin comment.
     *
     * @param adminCommentDto {@link AdminCommentDto}.
     * @author Bahlay Yuriy.
     */
    @Operation(summary = "Save admin comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY, content = @Content)
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
    @Operation(summary = "update eco-store id for order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY, content = @Content)
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
     * @param images                  {@link MultipartFile}.
     *
     * @author Bahlay Yuriy.
     * @author Anton Bondar.
     */

    @Operation(summary = "update order admin page info and save reason if needed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_ORDER', authentication)")
    @PatchMapping(value = "/update-order-page-admin-info/{id}",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HttpStatus> updatePageAdminInfo(@PathVariable(name = "id") Long orderId,
        @Valid @RequestPart UpdateOrderPageAdminDto updateOrderPageAdminDto,
        @RequestParam String language,
        @Parameter(hidden = true) Principal principal,
        @RequestPart(required = false) @Nullable MultipartFile[] images) {
        ubsManagementService.updateOrderAdminPageInfoAndSaveReason(orderId, updateOrderPageAdminDto, language,
            principal.getName(), images);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for updating all order admin page info.
     *
     * @param updateAllOrderPageDto {@link UpdateAllOrderPageDto}.
     * @param lang                  {@link String} language
     * @author Max Boiarchuk.
     */
    @Operation(summary = "update all order admin page info")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY, content = @Content)
    })
    @PutMapping("/all-order-page-admin-info")
    public ResponseEntity<HttpStatus> updateAllOrderPageAdminInfo(
        @RequestBody @Valid UpdateAllOrderPageDto updateAllOrderPageDto, Principal principal,
        @RequestParam String lang) {
        ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, principal.getName(), lang);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for get order cancellation reason.
     *
     * @return {@link OrderCancellationReasonDto}.
     * @author Kharchenko Volodymyr
     */
    @Operation(summary = "Get order cancellation reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderCancellationReasonDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
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
    @Operation(summary = "Get not taken order reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = NotTakenOrderReasonDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/get-not-taken-order-reason/{id}")
    public ResponseEntity<NotTakenOrderReasonDto> getNotTakenOrderReason(
        @Valid @PathVariable("id") Long orderId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getNotTakenOrderReason(orderId));
    }

    /**
     * Controller for checking if an order status was changed from FORMED to
     * CANCELED.
     *
     * @param id {@link Long} the ID of the order to check.
     * @return {@link Boolean} {@code true} if the order status was changed from
     *         {@code FORMED} to {@code CANCELED}, {@code false} otherwise.
     *
     * @author Volodymyr Lukovskyi
     */
    @Operation(summary = "Check if the order status transitioned from FORMED to CANCELED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
    })
    @GetMapping("/check-status-transition/formed-to-canceled/{id}")
    public ResponseEntity<Boolean> checkIfOrderStatusIsFormedToCanceled(@Valid @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.checkIfOrderStatusIsFormedToCanceled(id));
    }
}
