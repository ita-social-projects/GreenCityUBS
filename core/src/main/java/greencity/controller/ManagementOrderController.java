package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ApiPageable;
import greencity.annotations.ValidLanguage;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.filters.SearchCriteria;
import greencity.service.ubs.UBSManagementService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/ubs/management")
public class ManagementOrderController {
    private final UBSManagementService ubsManagementService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public ManagementOrderController(UBSManagementService ubsManagementService) {
        this.ubsManagementService = ubsManagementService;
    }

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
    @GetMapping("/getAllCertificates")
    @ApiPageable
    public ResponseEntity<PageableDto<CertificateDtoForSearching>> allCertificates(
        @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getAllCertificates(pageable));
    }

    /**
     * Controller getting all certificates with sorting possibility.
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
    @PostMapping("/addCertificate")
    public ResponseEntity<HttpStatus> addCertificate(
        @Valid @RequestBody CertificateDtoForAdding certificateDtoForAdding) {
        ubsManagementService.addCertificate(certificateDtoForAdding);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
    public ResponseEntity<List<GroupedOrderDto>> allUndeliveredCoords() {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getAllUndeliveredOrdersWithLiters());
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
            .body(ubsManagementService.getClusteredCoords(radius, litres));
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
            .body(ubsManagementService.getClusteredCoordsAlongWithSpecified(specified, litres, additionalDistance));
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
     * @author Nazar Struk
     */
    @ApiOperation("Add Violation to User")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addViolationToUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpStatus> addUsersViolation(@Valid @RequestPart AddingViolationsToUserDto add,
        @ApiIgnore @ValidLanguage Locale locale, @RequestPart(required = false) MultipartFile[] files) {
        ubsManagementService.addUserViolation(add, files);
        ubsManagementService.sendNotificationAboutViolation(add, locale.getLanguage());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Controller for getting User violations.
     *
     * @author Nazar Struk
     */
    @ApiOperation("Get all info from Table orders")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/orders")
    @ApiPageable
    public ResponseEntity<PageableDto<AllFieldsFromTableDto>> getAllValuesFromOrderTable(
        @ApiIgnore int page,
        @ApiIgnore int size,
        @RequestParam(value = "columnName", required = false) String columnName,
        @RequestParam(value = "sortingType", required = false) String sortingType,
        SearchCriteria searchCriteria) {
        if (columnName == null || sortingType == null) {
            return ResponseEntity.status(HttpStatus.OK)
                .body(ubsManagementService.getAllValuesFromTable(searchCriteria, page, size));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                .body(ubsManagementService.getAllSortedValuesFromTable(columnName, sortingType, page, size));
        }
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
     * Controller for update order address.
     *
     * @return {@link OrderAddressDtoResponse}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Update order address")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = OrderAddressDtoResponse.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/update-address")
    public ResponseEntity<OrderAddressDtoResponse> updateAddressByOrderId(
        @Valid @RequestBody OrderAddressDtoUpdate dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.updateAddress(dto));
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
        @RequestParam Long sumToPay) {
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
     * Controller for update order info.
     *
     * @return {@link List OrderDetailInfoDto}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Update order detail info")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.CREATED, response = OrderDetailInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/set-order-detail")
    public ResponseEntity<List<OrderDetailInfoDto>> setOrderDetailAmount(
        @RequestParam String language, @RequestBody List<UpdateOrderDetailDto> dto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.setOrderDetail(dto, language));
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
            ubsManagementService.getViolationDetailsByOrderId(orderId);
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
     * Controller for update order and payment status.
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
    @PutMapping("/update-order-detail-status/{id}")
    public ResponseEntity<OrderDetailStatusDto> updateOrderDetailStatus(
        @Valid @PathVariable("id") Long id, @RequestBody OrderDetailStatusRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.updateOrderDetailStatus(id, dto));
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
        @PathVariable("id") Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getOrderStatusData(orderId));
    }

    /**
     * Controller for update export details.
     *
     * @return {@link ExportDetailsDto}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Update export details")
    @ApiResponses({
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ExportDetailsDtoRequest.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/update-order-export-details/{id}")
    public ResponseEntity<ExportDetailsDto> updateOrderExportInfo(
        @Valid @PathVariable("id") Long id, @RequestBody ExportDetailsDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.updateOrderExportDetails(id, dto));
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
    public ResponseEntity<HttpStatus> deleteViolationFromOrder(@PathVariable Long orderId) {
        ubsManagementService.deleteViolation(orderId);
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
        @RequestParam Long sumToPay) {
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
        @RequestParam Long sumToPay) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.returnOverpaymentInfo(orderId, sumToPay, 1L));
    }

    /**
     * Controller returns overpayment to user.
     *
     * @param orderId                   {@link Long}.
     * @param overpaymentInfoRequestDto {@link OverpaymentInfoRequestDto}.
     * @return {@link HttpStatus} - http status.
     * @author Ostap Mykhailivskyi
     */
    @ApiOperation(value = "Return overpayment to user")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/return-overpayment")
    public ResponseEntity<HttpStatus> returnOverpayment(@RequestParam Long orderId,
        @RequestBody OverpaymentInfoRequestDto overpaymentInfoRequestDto) {
        ubsManagementService.returnOverpayment(orderId, overpaymentInfoRequestDto);
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
        @RequestPart ManualPaymentRequestDto manualPaymentDto,
        @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.saveNewManualPayment(orderId, manualPaymentDto, image));
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
    public ResponseEntity<ResponseStatus> deleteManualPayment(@PathVariable(name = "id") Long paymentId) {
        ubsManagementService.deleteManualPayment(paymentId);
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
        @RequestPart ManualPaymentRequestDto manualPaymentDto,
        @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.updateManualPayment(paymentId, manualPaymentDto, image));
    }

    /**
     * Controller for get employees depends from position.
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
    public ResponseEntity<EmployeePositionDtoRequest> getAllEmployeeByPosition(@Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getAllEmployeesByPosition(id));
    }

    /**
     * Controller for update depends from position.
     *
     * @return {EmployeePositionDtoResponse}.
     * @author Bohdan Fedorkiv
     */

    @ApiOperation(value = "Update employee position by order")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/update-position-by-order")
    public ResponseEntity<HttpStatus> updateByOrder(
        @RequestBody @Valid EmployeePositionDtoResponse dto) {
        ubsManagementService.updatePositions(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for updating User violation.
     *
     * @author Bohdan Melnyk
     */
    @ApiOperation("Update Violation to User")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @ResponseStatus(value = HttpStatus.CREATED)
    @PutMapping(value = "/updateViolationToUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpStatus> updateUsersViolation(@Valid @RequestPart AddingViolationsToUserDto add,
        @RequestPart(required = false) MultipartFile[] multipartFiles) {
        ubsManagementService.updateUserViolation(add, multipartFiles);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Controller for save reason not taking the bag. *
     *
     * @return {ReasonNotTakeBagDto}. * @author Bohdan Fedorkiv
     */

    @ApiOperation(value = "Save reason for not taking the bag")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ReasonNotTakeBagDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)})
    @PutMapping(value = "/save-reason/{id}",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ReasonNotTakeBagDto> saveReason(@PathVariable("id") Long id,
        @RequestParam String description,
        @RequestPart(required = false) List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ubsManagementService.saveReason(id, description, images));
    }
}
