package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.pageble.PageableDto;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserVO;
import greencity.enums.OrderStatus;
import greencity.service.ubs.UBSClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@RestController
@RequestMapping("/ubs/client")
@RequiredArgsConstructor
public class ClientController {
    private final UBSClientService ubsClientService;

    /**
     * Controller for getting all user orders.
     *
     * @return {@link List OrdersDataForUserDto}.
     * @author Oleksandr Khomiakov
     */
    @Operation(summary = "returns all user orders for logged user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PageableDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
    })
    @GetMapping("/user-orders")
    @ApiPageable
    public ResponseEntity<PageableDto<OrdersDataForUserDto>> getAllDataForOrder(
        @Parameter(hidden = true) @CurrentUserUuid String uuid, @Parameter(hidden = true) Pageable page,
        @RequestParam(value = "status", required = false) List<OrderStatus> statuses) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getOrdersForUser(uuid, page, statuses));
    }

    /**
     * Controller for getting user order.
     *
     * @return {@link OrdersDataForUserDto}.
     * @author Oleg Postolovskyi
     */
    @Operation(summary = "returns user order for logged user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrdersDataForUserDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/user-order/{id}")
    public ResponseEntity<OrdersDataForUserDto> getAllDataForOneOrder(
        @Parameter(hidden = true) @CurrentUserUuid String uuid, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getOrderForUser(uuid, id));
    }

    /**
     * Controller for delete user order.
     *
     * @return {@link HttpStatus http status}.
     * @author Max Boiarchuk
     */
    @Operation(summary = "delete user order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @DeleteMapping("/delete-order/{id}")
    public ResponseEntity<HttpStatus> deleteOrder(
        @Parameter(hidden = true) @CurrentUserUuid String uuid,
        @PathVariable Long id) {
        ubsClientService.deleteOrder(uuid, id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller returns all bonuses of user..
     *
     * @param uuid {@link String} id.
     * @return list of {@link AllPointsUserDto}.
     * @author Liubomyr Bratakh
     */
    @Operation(summary = "Get user's bonuses.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = AllPointsUserDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content)
    })
    @GetMapping("/users-pointsToUse")
    public ResponseEntity<AllPointsUserDto> getAllPointsForUser(
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.findAllCurrentPointsForUser(uuid));
    }

    /**
     * Controller returns information about order payment by orderId.
     *
     * @return {@link OrderPaymentDetailDto}
     * @author Mykola Danylko.
     */
    @Operation(summary = "Return order payment details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderPaymentDetailDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/order-payment-detail/{orderId}")
    public ResponseEntity<OrderPaymentDetailDto> getOrderPaymentDetail(@PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getOrderPaymentDetail(orderId));
    }

    /**
     * Controller returns bonus points of current user. {@link UserVO}.
     *
     * @param userUuid {@link String} uuid.
     * @return {@link UserPointDto}.
     * @author Max Boiarchuk
     */
    @Operation(summary = "Get current user points.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserPointDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/user-bonuses")
    public ResponseEntity<UserPointDto> getUserBonuses(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getUserPoint(userUuid));
    }

    /**
     * Return link Way For Pay payment.
     *
     * @param userUuid current user id.
     * @param dto      order info.
     * @return {@link PaymentSystemResponse} response with payment link.
     */
    @Operation(summary = "Return link for payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PaymentSystemResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/processOrder")
    public ResponseEntity<PaymentSystemResponse> processOrder(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @Valid @RequestBody OrderWayForPayClientDto dto) {
        return ResponseEntity.ok(ubsClientService.processOrder(userUuid, dto));
    }
}
