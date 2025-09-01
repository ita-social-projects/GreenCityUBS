package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUserUuid;
import greencity.configuration.RedirectionConfigProp;
import greencity.constant.ValidationConstant;
import greencity.constants.HttpStatuses;
import greencity.dto.LocationsDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.EventDto;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.dto.payment.monobank.MonoBankPaymentResponseDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.entity.user.User;
import greencity.service.ubs.UBSClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ubs")
@Validated
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final UBSClientService ubsClientService;
    private final RedirectionConfigProp redirectionConfigProp;

    /**
     * Controller returns all available bags by tariff and location ids.
     *
     * @param tariffId   - id of tariff.
     * @param locationId - id of location.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author SafarovRenat
     */
    @Operation(summary = "Get details for the given tariff and location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserPointsAndAllBagsDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/order-details-for-tariff")
    public ResponseEntity<UserPointsAndAllBagsDto> getCurrentUserPointsByTariffAndLocationId(
        @RequestParam Long tariffId,
        @RequestParam Long locationId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getFirstPageDataByTariffAndLocationId(tariffId, locationId));
    }

    /**
     * Controller returns all available bags and bonus points of current user by
     * order id.
     *
     * @param userUuid - user's uuid.
     * @param orderId  - id of the order.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author SafarovRenat
     */
    @Operation(summary = "Get current user points by order id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserPointsAndAllBagsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/details-for-existing-order/{orderId}")
    public ResponseEntity<UserPointsAndAllBagsDto> getCurrentUserPointsByOrderId(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @Positive @PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getFirstPageDataByOrderId(userUuid, orderId));
    }

    /**
     * Controller returns entered certificate status if not absent.
     *
     * @param responseCode {@link String} responseCode of certificate.
     * @return {@link CertificateDto}.
     * @author Oleh Bilonizhka
     */
    @Operation(summary = "Check if certificate is available.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = CertificateDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/certificate/{responseCode}")
    public ResponseEntity<CertificateDto> checkIfCertificateAvailable(
        @PathVariable @Pattern(regexp = ValidationConstant.CERTIFICATE_CODE_REGEXP,
            message = ValidationConstant.CERTIFICATE_CODE_REGEXP_MESSAGE) String responseCode,
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.checkCertificate(responseCode, userUuid));
    }

    /**
     * Controller returns list of saved user's data.
     *
     * @param userUuid - user's id.
     * @return list of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    @Operation(summary = "Get user's personal data.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PersonalDataDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/personal-data")
    public ResponseEntity<PersonalDataDto> getUBSUsers(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getSecondPageData(userUuid));
    }

    /**
     * Controller adjusts and creates new order and generates payment link for the
     * order.
     *
     * @param userUuid - user's id.
     * @param dto      {@link OrderResponseDto} order data.
     * @return {@link PaymentSystemResponse}.
     * @author Oleh Bilonizhka
     */
    @Operation(summary = "Process new order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PaymentSystemResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PostMapping("/processOrder")
    public ResponseEntity<PaymentSystemResponse> processNewOrder(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @Valid @RequestBody OrderResponseDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.processNewOrder(dto, userUuid));
    }

    /**
     * Controller adjusts and saves existing order to database and generates payment
     * link for the order.
     *
     * @param userUuid current {@link User}'s uuid.
     * @param dto      {@link OrderResponseDto} order data.
     * @return {@link PaymentSystemResponse}.
     * @author Oleksandr Ilnytskyi
     */
    @Operation(summary = "Process existing order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PaymentSystemResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PostMapping("/processOrder/{id}")
    public ResponseEntity<PaymentSystemResponse> processExistingOrder(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @Valid @RequestBody OrderResponseDto dto,
        @Positive @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.processExistingOrder(dto, userUuid, id));
    }

    /**
     * Receives payment notifications from WayForPay. Parses the JSON from the
     * request parameters into a PaymentResponseDto, validates it, and returns a
     * PaymentResponseWayForPay object. HTTP status: 200 OK — payment processed
     * successfully 422 Unprocessable Entity — invalid payment data
     *
     * @param formParams Map with payment response parameters from WayForPay
     * @return PaymentResponseWayForPay containing status and orderReference
     * @throws IOException If an I/O error occurs (rare in normal webhook flow)
     */
    @Operation(summary = "Receive payment from WayForPay.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content)
    })
    @PostMapping("/receivePayment")
    public ResponseEntity<PaymentResponseWayForPay> receivePayment(@RequestParam Map<String, String> formParams)
        throws IOException {
        PaymentResponseWayForPay response = ubsClientService.convertMapIntoPaymentResponseDto(formParams);

        HttpStatus status = "ERROR".equals(response.getStatus())
            ? HttpStatus.UNPROCESSABLE_ENTITY
            : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Controller gets info about user, ubs_user and user violations by order id.
     *
     * @param id   {@link Long}.
     * @param uuid current {@link User}'s uuid.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Get user and ubs_user and violations info in order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserInfoDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @ApiLocale
    @GetMapping("/user-info/{orderId}")
    public ResponseEntity<UserInfoDto> getOrderDetailsByOrderId(
        @Positive @PathVariable("orderId") Long id,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.ok()
            .body(ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(id, uuid));
    }

    /**
     * Controller gets info about events history from,order by order id.
     *
     * @param id     {@link Long}.
     * @param locale {@link Locale}.
     * @return {@link HttpStatus} - http status.
     * @author Yuriy Bahlay.
     */
    @Operation(summary = "Get events history from order by Id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EventDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @ApiLocale
    @GetMapping("/order_history/{orderId}")
    public ResponseEntity<List<EventDto>> getOderHistoryByOrderId(
        @Positive @PathVariable("orderId") Long id,
        Principal principal,
        @Parameter(hidden = true) Locale locale) {
        return ResponseEntity.ok()
            .body(ubsClientService.getAllEventsForOrder(id, principal.getName(), locale.getLanguage()));
    }

    /**
     * Controller updates info about ubs_user in order .
     *
     * @param dto {@link UbsCustomersDtoUpdate}.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Update recipient information in order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UbsCustomersDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_ORDER', authentication) || hasAnyRole('USER','ADMIN')")
    @PutMapping("/update-recipients-data")
    public ResponseEntity<UbsCustomersDto> updateRecipientsInfo(
        @Valid @RequestBody UbsCustomersDtoUpdate dto, @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.updateUbsUserInfoInOrder(dto, uuid));
    }

    /**
     * Controller gets info about order cancellation reason.
     *
     * @param id   {@link Long}.
     * @param uuid current {@link User}'s uuid.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "gets info about order cancellation reason ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderCancellationReasonDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/order/{id}/cancellation")
    public ResponseEntity<OrderCancellationReasonDto> getCancellationReason(
        @Positive @PathVariable("id") final Long id,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.ok().body(ubsClientService.getOrderCancellationReason(id, uuid));
    }

    /**
     * Controller for getting all Active Locations by courier ID, if user haven't
     * made any order before. If user has made an order before controller returns
     * info about tariff by which it was made Controller is used to get all active
     * Locations if user want to change location to make an order
     *
     * @param changeLoc - optional param. If it is present controller will return
     *                  info about locations
     * @param courierId - id of courier
     * @param uuid      - user's uuid
     * @return {@link OrderCourierPopUpDto}
     * @author Anton Bondar
     */
    @Operation(summary = "Get all active locations where courier is working")
    @GetMapping("/locations/{courierId}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    public ResponseEntity<OrderCourierPopUpDto> getAllActiveLocationsByCourierId(
        @RequestParam Optional<String> changeLoc,
        @Parameter(hidden = true) @CurrentUserUuid String uuid,
        @Positive @PathVariable Long courierId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getInfoForCourierOrderingByCourierId(uuid, changeLoc, courierId));
    }

    /**
     * Controller for getting all active couriers.
     *
     * @return list of {@link CourierDto}
     * @author Anton Bondar
     */
    @Operation(summary = "Get all active couriers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourierDto.class)))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content)
    })
    @GetMapping("/getAllActiveCouriers")
    public ResponseEntity<List<CourierDto>> getAllActiveCouriers() {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getAllActiveCouriers());
    }

    /**
     * Controller for getting info about tariff for courier and location.
     *
     * @param courierId  - id of courier
     * @param locationId - id of location
     * @return {@link TariffInfoByLocationDto}
     * @author Anton Bondar
     */
    @Operation(summary = "Get tariff for courier and location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/tariffinfo/{locationId}")
    public ResponseEntity<TariffInfoByLocationDto> getInfoAboutTariff(
        @Positive @RequestParam Long courierId,
        @Positive @PathVariable Long locationId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getTariffInfoForLocation(courierId, locationId));
    }

    /**
     * Controller for getting info about tariff by order's id.
     *
     * @param id - order ID
     * @return {@link TariffsForLocationDto}
     */
    @Operation(summary = "Get tariff for order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/orders/{id}/tariff")
    public ResponseEntity<TariffsForLocationDto> getTariffForOrder(@Positive @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getTariffForOrder(id));
    }

    /**
     * Check if a tariff exists by its ID.
     *
     * @param id The ID of the tariff to check.
     * @return {@code ResponseEntity} with HTTP 200 OK if tariff exists, HTTP 404
     *         NOT FOUND if it doesn't exist.
     * @author Yurii Ososvskyi
     */
    @Operation(summary = "Check if tariff exists by Id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping(value = "/check-if-tariff-exists/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> checkIfTariffExistsById(@Positive @PathVariable Long id) {
        boolean exists = ubsClientService.checkIfTariffExistsById(id);
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Retrieves all active locations and returns them as DTOs.
     *
     * @return ResponseEntity with a list of DTOs representing all active locations.
     */
    @Operation(summary = "Get All Active Locations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping(value = "/locations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LocationsDto>> getAllLocations() {
        List<LocationsDto> locations = ubsClientService.getAllLocations();
        return ResponseEntity.status(HttpStatus.OK).body(locations);
    }

    /**
     * Retrieves the tariff ID by location ID.
     *
     * @param locationId The ID of the location.
     * @return ResponseEntity with the tariff ID.
     */
    @Operation(summary = "Get Tariff ID by Location ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping(value = "/tariffs/{locationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Long>> getTariffIdByLocationId(@Positive @PathVariable("locationId") Long locationId) {
        List<Long> tariffId = ubsClientService.getTariffIdByLocationId(locationId);
        return ResponseEntity.status(HttpStatus.OK).body(tariffId);
    }

    /**
     * Retrieves all active locations and returns them as DTOs.
     *
     * @return ResponseEntity with a list of DTOs representing all active locations.
     */
    @Operation(summary = "Get All Active Locations By Courier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping(value = "/locationsByCourier/{courierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LocationsDto>> getAllLocationsByCourierId(
        @Positive @PathVariable("courierId") Long courierId) {
        List<LocationsDto> locations = ubsClientService.getAllLocationsByCourierId(courierId);
        return ResponseEntity.status(HttpStatus.OK).body(locations);
    }

    /**
     * Receives and processes payment information from the Monobank API. This method
     * handles the incoming payment response and validates the payment details
     * provided by Monobank. The payment details are logged and then passed to the
     * {@code validatePaymentFromMonoBank} method for further validation and
     * processing.
     *
     * @param response the payment response received from Monobank, containing
     *                 details such as transaction ID, status, and amount.
     */
    @Operation(summary = "Receive payment information from Monobank API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @PostMapping("/monobank/payments")
    public void receivePaymentFromMonoBank(@RequestBody @Valid MonoBankPaymentResponseDto response) {
        log.info("Response from MONOBANK API: {}", response);
        ubsClientService.validatePaymentFromMonoBank(response);
    }
}
