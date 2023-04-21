package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUserUuid;
import greencity.configuration.RedirectionConfigProp;
import greencity.constants.HttpStatuses;
import greencity.constant.ValidationConstant;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.*;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.dto.user.UserVO;
import greencity.entity.user.User;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
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
    @ApiOperation(value = "Get order points by details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = UserPointsAndAllBagsDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/order-details-for-tariff")
    public ResponseEntity<UserPointsAndAllBagsDto> getCurrentUserPointsByTariffAndLocationId(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @RequestParam Long tariffId,
        @RequestParam Long locationId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getFirstPageDataByTariffAndLocationId(userUuid, tariffId, locationId));
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
    @ApiOperation(value = "Get current user points by order id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = UserPointsAndAllBagsDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/details-for-existing-order/{orderId}")
    public ResponseEntity<UserPointsAndAllBagsDto> getCurrentUserPointsByOrderId(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getFirstPageDataByOrderId(userUuid, orderId));
    }

    /**
     * Controller returns entered certificate status if not absent.
     *
     * @param code {@link String} code of certificate.
     * @return {@link CertificateDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Check if certificate is available.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = CertificateDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/certificate/{code}")
    public ResponseEntity<CertificateDto> checkIfCertificateAvailable(
        @PathVariable @Pattern(regexp = ValidationConstant.SERTIFICATE_CODE_REGEXP,
            message = ValidationConstant.SERTIFICATE_CODE_REGEXP_MESSAGE) String code) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.checkCertificate(code));
    }

    /**
     * Controller returns list of saved {@link UserVO} data.
     *
     * @param userUuid {@link UserVO} id.
     * @return list of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get user's personal data.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PersonalDataDto[].class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/personal-data")
    public ResponseEntity<PersonalDataDto> getUBSUsers(
        @ApiIgnore @CurrentUserUuid String userUuid) {
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
    @ApiOperation(value = "Process user order.")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping(value = {"/processOrder", "/processOrder/{id}"})
    public ResponseEntity<FondyOrderResponse> processOrder(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @Valid @RequestBody OrderResponseDto dto,
        @Valid @PathVariable("id") Optional<Long> id) {
        if (id.isPresent()) {
            OrderDetailStatusDto orderDetailStatusDto = ubsManagementService.getOrderDetailStatus(id.get());
            if (orderDetailStatusDto.getPaymentStatus().equals("PAID")) {
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
    @ApiOperation(value = "Receive payment from Fondy.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
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
     * Controller for getting all addresses for current order.
     *
     * @param userUuid {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "Get all addresses for order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderWithAddressesResponseDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/findAll-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> getAllAddressesForCurrentUser(
        @ApiIgnore @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.findAllAddressesForCurrentOrder(userUuid));
    }

    /**
     * Controller save address for current order.
     *
     * @param dtoRequest {@link CreateAddressRequestDto}.
     * @param uuid       {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "Save order address")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = OrderWithAddressesResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/save-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> saveAddressForOrder(
        @Valid @RequestBody CreateAddressRequestDto dtoRequest,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsClientService.saveCurrentAddressForOrder(dtoRequest, uuid));
    }

    /**
     * Controller update address for current order.
     *
     * @param dtoRequest {@link OrderAddressDtoRequest}.
     * @param uuid       {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "Update order address(if placeId is null updates only addressComment)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderWithAddressesResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/update-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> updateAddressForOrder(
        @Valid @RequestBody OrderAddressDtoRequest dtoRequest,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.updateCurrentAddressForOrder(dtoRequest, uuid));
    }

    /**
     * Controller delete order address.
     *
     * @param id   {@link Long}.
     * @param uuid {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "Delete order address")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.CREATED, response = OrderWithAddressesResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/order-addresses/{id}")
    public ResponseEntity<OrderWithAddressesResponseDto> deleteOrderAddress(
        @Valid @PathVariable("id") Long id,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.deleteCurrentAddressForOrder(id, uuid));
    }

    /**
     * Controller gets info about user, ubs_user and user violations by order id.
     *
     * @param id   {@link Long}.
     * @param uuid current {@link User}'s uuid.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "Get user and ubs_user and violations info in order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = UserInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/user-info/{orderId}")
    public ResponseEntity<UserInfoDto> getOrderDetailsByOrderId(
        @Valid @PathVariable("orderId") Long id,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.ok()
            .body(ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(id, uuid));
    }

    /**
     * Controller gets info about events history from,order by order id.
     *
     * @param id {@link Long}.
     * @return {@link HttpStatus} - http status.
     * @author Yuriy Bahlay.
     */
    @ApiOperation(value = "Get events history from order by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = EventDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/order_history/{orderId}")
    public ResponseEntity<List<EventDto>> getOderHistoryByOrderId(
        @Valid @PathVariable("orderId") Long id,
        Principal principal) {
        return ResponseEntity.ok().body(ubsClientService.getAllEventsForOrder(id, principal.getName()));
    }

    /**
     * Controller updates info about ubs_user in order .
     *
     * @param dto {@link UbsCustomersDtoUpdate}.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "Update recipient information in order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = UbsCustomersDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_ORDER', authentication)")
    @PutMapping("/update-recipients-data")
    public ResponseEntity<UbsCustomersDto> updateRecipientsInfo(
        @Valid @RequestBody UbsCustomersDtoUpdate dto, @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.updateUbsUserInfoInOrder(dto, uuid));
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
    @PostMapping("/order/{id}/cancellation/")
    public ResponseEntity<OrderCancellationReasonDto> updateCancellationReason(
        @RequestBody final OrderCancellationReasonDto dto,
        @PathVariable("id") final Long id,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.updateOrderCancellationReason(id, dto, uuid));
    }

    /**
     * Controller gets info about order cancellation reason.
     *
     * @param id   {@link Long}.
     * @param uuid current {@link User}'s uuid.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "gets info about order cancellation reason ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderCancellationReasonDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/order/{id}/cancellation")
    public ResponseEntity<OrderCancellationReasonDto> getCancellationReason(
        @PathVariable("id") final Long id,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.ok().body(ubsClientService.getOrderCancellationReason(id, uuid));
    }

    /**
     * Controller for getting status about payment from Fondy.
     *
     * @param orderId - current order.
     * @param uuid    current {@link User}'s uuid.
     * @return {@link String}
     */
    @ApiOperation(value = "Get status of Payment from Fondy")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping(value = "/getFondyStatus/{orderId}")
    public ResponseEntity<FondyPaymentResponse> getFondyStatusPayment(
        @Valid @PathVariable Long orderId,
        @ApiIgnore @CurrentUserUuid String uuid) {
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
    @ApiOperation(value = "Receive payment from  Client Fondy.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
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
     * Controller for getting all Active Locations if user haven't made any order
     * before. If user has made an order before controller returns info about tariff
     * by which it was made Controller is used to get all active Locations if user
     * want to change location to make an order
     *
     * @param changeLoc - optional param. If it is present controller will return
     *                  info about locations
     * @param uuid      - user's uuid
     * @return {@link OrderCourierPopUpDto}
     */
    @ApiOperation(value = "Get all location where courier is working")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/allLocations")
    public ResponseEntity<OrderCourierPopUpDto> getAllActiveLocatins(
        @RequestParam Optional<String> changeLoc,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getInfoForCourierOrdering(uuid, changeLoc));
    }

    /**
     * Controller for getting info about tariff for location.
     *
     * @param locationId - id of location
     * @return {@link OrderCourierPopUpDto}
     */
    @ApiOperation(value = "Get tariff for location and courier")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/tariffinfo-for-location/{locationId}")
    public ResponseEntity<OrderCourierPopUpDto> getInfoAboutTariff(@Valid @PathVariable Long locationId) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getTariffInfoForLocation(locationId));
    }

    /**
     * Controller for getting info about tariff by order's id.
     *
     * @param id - order ID
     * @return {@link TariffsForLocationDto}
     */
    @ApiOperation(value = "Get tariff for order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/orders/{id}/tariff")
    public ResponseEntity<TariffsForLocationDto> getTariffForOrder(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getTariffForOrder(id));
    }
}
