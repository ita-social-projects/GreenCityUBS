package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUserUuid;
import greencity.configuration.RedirectionConfigProp;
import greencity.constants.HttpStatuses;
import greencity.constant.ValidationConstant;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.EventDto;
import greencity.dto.order.FondyOrderResponse;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.dto.user.UserVO;
import greencity.entity.user.User;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/ubs")
@Validated
@RequiredArgsConstructor
public class OrderController {
    private final UBSClientService ubsClientService;
    private final UBSManagementService ubsManagementService;
    private final RedirectionConfigProp redirectionConfigProp;
    private final NotificationService notificationService;

    /**
     * Controller returns all available bags and bonus points of current user by
     * tariff and location ids. {@link UserVO}.
     *
     * @param userUuid   {@link UserVO} id.
     * @param tariffId   {@link UserVO} id of tariff.
     * @param locationId {@link UserVO} id of location.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author SafarovRenat
     */
    @Operation(summary = "Get order points by details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserPointsAndAllBagsDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
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
     * order id. {@link UserVO}.
     *
     * @param userUuid {@link UserVO} id.
     * @param orderId  {@link UserVO} id of order.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author SafarovRenat
     */
    @Operation(summary = "Get current user points by order id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserPointsAndAllBagsDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/details-for-existing-order/{orderId}")
    public ResponseEntity<UserPointsAndAllBagsDto> getCurrentUserPointsByOrderId(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @PathVariable Long orderId) {
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/certificate/{responseCode}")
    public ResponseEntity<CertificateDto> checkIfCertificateAvailable(
        @PathVariable @Pattern(regexp = ValidationConstant.CERTIFICATE_CODE_REGEXP,
            message = ValidationConstant.CERTIFICATE_CODE_REGEXP_MESSAGE) String responseCode) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.checkCertificate(responseCode));
    }

    /**
     * Controller returns list of saved {@link UserVO} data.
     *
     * @param userUuid {@link UserVO} id.
     * @return list of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    @Operation(summary = "Get user's personal data.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PersonalDataDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/personal-data")
    public ResponseEntity<PersonalDataDto> getUBSUsers(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getSecondPageData(userUuid));
    }

    /**
     * Controller saves all entered by user data to database.
     *
     * @param userUuid {@link UserVO} id.
     * @param dto      {@link OrderResponseDto} order data.
     * @param id       {@link Long} orderId.
     * @return {@link HttpStatus}.
     * @author Oleh Bilonizhka
     */
    @Operation(summary = "Process user order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping(value = {"/processOrder", "/processOrder/{id}"})
    public ResponseEntity<FondyOrderResponse> processOrder(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @Valid @RequestBody OrderResponseDto dto,
        @Valid @PathVariable("id") Optional<Long> id) {
        if (id.isPresent()) {
            OrderDetailStatusDto orderDetailStatusDto = ubsManagementService.getOrderDetailStatus(id.get());
            if (PaymentStatus.PAID.name().equals(orderDetailStatusDto.getPaymentStatus())
                || !OrderStatus.FORMED.name().equals(orderDetailStatusDto.getOrderStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            return ResponseEntity.status(HttpStatus.OK)
                .body(ubsClientService.saveFullOrderToDB(dto, userUuid, id.get()));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.saveFullOrderToDB(dto, userUuid, null));
        }
    }

    /**
     * Controller checks if received data is valid and stores payment info if is.
     *
     * @param dto {@link PaymentResponseDto} - response order data.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Receive payment from Fondy.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    @PostMapping("/receivePayment")
    public ResponseEntity<HttpStatus> receivePayment(
        PaymentResponseDto dto, HttpServletResponse response) throws IOException {
        ubsClientService.validatePayment(dto);
        if (HttpStatus.OK.is2xxSuccessful()) {
            notificationService.notifyPaidOrder(dto);
            response.sendRedirect(redirectionConfigProp.getGreenCityClient());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
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
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/user-info/{orderId}")
    public ResponseEntity<UserInfoDto> getOrderDetailsByOrderId(
        @Valid @PathVariable("orderId") Long id,
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
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/order_history/{orderId}")
    public ResponseEntity<List<EventDto>> getOderHistoryByOrderId(
        @Valid @PathVariable("orderId") Long id,
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
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_ORDER', authentication)")
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/order/{id}/cancellation")
    public ResponseEntity<OrderCancellationReasonDto> getCancellationReason(
        @PathVariable("id") final Long id,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.ok().body(ubsClientService.getOrderCancellationReason(id, uuid));
    }

    /**
     * Controller for getting status about payment from Fondy.
     *
     * @param orderId - current order.
     * @param uuid    current {@link User}'s uuid.
     * @return {@link String}
     */
    @Operation(summary = "Get status of Payment from Fondy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping(value = "/getFondyStatus/{orderId}")
    public ResponseEntity<FondyPaymentResponse> getFondyStatusPayment(
        @Valid @PathVariable Long orderId,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getPaymentResponseFromFondy(orderId, uuid));
    }

    /**
     * Controller checks if received data Client is valid and stores payment info if
     * is.
     *
     * @param dto {@link PaymentResponseDto} - response order data.
     * @return {@link HttpStatus} - http status.
     * @author Max Boiarchuk
     */
    @Operation(summary = "Receive payment from  Client Fondy.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    @PostMapping("/receivePaymentClient")
    public ResponseEntity<HttpStatus> receivePaymentClient(
        PaymentResponseDto dto, HttpServletResponse response) throws IOException {
        ubsClientService.validatePaymentClient(dto);
        if (HttpStatus.OK.is2xxSuccessful()) {
            notificationService.notifyPaidOrder(dto);
            response.sendRedirect(redirectionConfigProp.getGreenCityClient());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    public ResponseEntity<OrderCourierPopUpDto> getAllActiveLocationsByCourierId(
        @RequestParam Optional<String> changeLoc,
        @Parameter(hidden = true) @CurrentUserUuid String uuid,
        @Valid @PathVariable Long courierId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getInfoForCourierOrderingByCourierId(uuid, changeLoc, courierId));
    }

    /**
     * Controller for getting all active couriers.
     *
     * @return list of {@link CourierDto}
     *
     * @author Anton Bondar
     */
    @Operation(summary = "Get all active couriers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourierDto.class)))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
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
     * @return {@link OrderCourierPopUpDto}
     * @author Anton Bondar
     */
    @Operation(summary = "Get tariff for courier and location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/tariffinfo/{locationId}")
    public ResponseEntity<OrderCourierPopUpDto> getInfoAboutTariff(
        @RequestParam Long courierId,
        @PathVariable Long locationId) {
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/orders/{id}/tariff")
    public ResponseEntity<TariffsForLocationDto> getTariffForOrder(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getTariffForOrder(id));
    }
}
