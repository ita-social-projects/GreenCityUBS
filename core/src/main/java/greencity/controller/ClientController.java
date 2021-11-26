package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.annotations.ValidLanguage;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.service.ubs.UBSClientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/ubs/client")
public class ClientController {
    private final UBSClientService ubsClientService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public ClientController(UBSClientService ubsClientService) {
        this.ubsClientService = ubsClientService;
    }

    /**
     * Controller returns all user's orders.
     *
     * @param userUuid {@link UserVO} id.
     * @return {@link OrderClientDto} list of user's orders.
     * @author Danylko Mykola.
     */
    @ApiOperation(value = "Get all orders done by user")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderClientDto[].class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/getAll-users-orders")
    public ResponseEntity<List<OrderClientDto>> getAllOrdersDoneByUser(
        @ApiIgnore @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getAllOrdersDoneByUser(userUuid));
    }

    /**
     * Controller for getting all user orders.
     *
     * @return {@link List OrderStatusPageDto}.
     * @author Oleksandr Khomiakov
     */
    @ApiOperation(value = "returns all user orders for logged user")
    @ApiResponses({
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderStatusPageDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-all-orders-data/{lang}")
    public ResponseEntity<List<OrderStatusPageDto>> getAllDataForOrder(
        @ApiIgnore @CurrentUserUuid String uuid, @PathVariable Long lang) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getOrdersForUser(uuid, lang));
    }

    /**
     * Controller for delete user order.
     *
     * @return {@link HttpStatus http status}.
     * @author Max Boiarchuk
     */
    @ApiOperation(value = "delete user order")
    @ApiResponses({
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderStatusPageDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete-order/{id}")
    public ResponseEntity<HttpStatus> deleteOrder(@PathVariable Long id) {
        ubsClientService.deleteOrder(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller return link fondy payment .
     *
     * @return {@link OrderFondyClientDto} dto.
     * @author Max Boiarchuk
     */
    @ApiOperation(value = "return the link for payment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = MakeOrderAgainDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/processOrderFondy")
    public ResponseEntity<FondyOrderResponse> processOrderFondy(@Valid @RequestBody OrderFondyClientDto dto)
        throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.processOrderFondyClient(dto));
    }

    /**
     * Controller return link liqpay payment .
     *
     * @return {@link OrderLiqpayClienDto} dto.
     * @author Max Boiarchuk
     */
    @ApiOperation(value = "return the link for liqpay payment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = MakeOrderAgainDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/processOrderLiqpay")
    public ResponseEntity<LiqPayOrderResponse> processOrderLiqpay(@Valid @RequestBody OrderLiqpayClienDto dto)
        throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.proccessOrderLiqpayClient(dto));
    }

    /**
     * Controller cancel order with status FORMED.
     *
     * @param orderId {@link Long} - order id.
     * @return {@link HttpStatus} - http status.
     * @author Danylko Mykola
     */
    @ApiOperation(value = "Cancel order with status FORMED")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/{id}/cancel-formed-order")
    public ResponseEntity<OrderClientDto> cancelFormedOrder(
        @PathVariable(name = "id") Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.cancelFormedOrder(orderId));
    }

    /**
     * Controller that make order again if our status of Order is ON_THE_ROUTE,
     * CONFIRMED, DONE.
     *
     * @param orderId {@link Long} order id.
     * @return {@link HttpStatus} - http status.
     * @author Danylko Mykola
     */
    @ApiOperation(value = "Make order again if our status of Order is ON_THE_ROUTE, CONFIRMED, DONE")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = MakeOrderAgainDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{id}/make-order-again")
    public ResponseEntity<MakeOrderAgainDto> makeOrderAgain(@PathVariable(name = "id") Long orderId,
        @ApiIgnore @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.makeOrderAgain(locale, orderId));
    }

    /**
     * Controller returns all bonuses of user..
     *
     * @param uuid {@link String} id.
     * @return list of {@link AllPointsUserDto}.
     * @author Liubomyr Bratakh
     */
    @ApiOperation(value = "Get user's bonuses.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = AllPointsUserDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/users-pointsToUse")
    public ResponseEntity<AllPointsUserDto> getAllPointsForUser(
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.findAllCurrentPointsForUser(uuid));
    }

    /**
     * Controller returns information about order payment by orderId.
     *
     * @return {@link OrderPaymentDetailDto}
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Return order payment details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderPaymentDetailDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/order-payment-detail/{orderId}")
    public ResponseEntity<OrderPaymentDetailDto> getOrderPaymentDetail(@PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getOrderPaymentDetail(orderId));
    }

    /**
     * Controller for getting order info data about surcharge.
     *
     * @return {@link OrderStatusPageDto}.
     * @author Igor Boykov
     */
    @ApiOperation(value = "Controller for getting order info data about surcharge")
    @ApiResponses({
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderStatusPageDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-data-for-order-surcharge/{id}/{langId}")
    public ResponseEntity<OrderStatusPageDto> getDataForOrderSurcharge(
        @PathVariable(name = "id") Long orderId, @PathVariable(name = "langId") Long languageId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getOrderInfoForSurcharge(orderId, languageId));
    }
}