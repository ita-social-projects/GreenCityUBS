package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.service.ubs.OrdersAdminsPageService;
import greencity.service.ubs.ValuesForUserTableService;
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

@RestController
@RequestMapping("/ubs/management")
public class AdminUbsController {
    private final OrdersAdminsPageService ordersAdminsPageService;
    private final ValuesForUserTableService valuesForUserTable;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public AdminUbsController(OrdersAdminsPageService ordersAdminsPageService,
        ValuesForUserTableService valuesForUserTable) {
        this.ordersAdminsPageService = ordersAdminsPageService;
        this.valuesForUserTable = valuesForUserTable;
    }

    /**
     * Controller for obtaining all users that made at least one order.
     *
     * @author Stepan Tehlivets.
     */
    @ApiOperation("Get users for the table")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/usersAll")
    public ResponseEntity<FieldsForUsersTableDto> getAllValuesForUserTable() {
        return ResponseEntity.status(HttpStatus.OK)
            .body(valuesForUserTable.getAllFields());
    }

    /**
     * Controller.
     *
     * @author Liubomyr Pater
     */
    @ApiOperation("Get all parameters for building table of orders")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/tableParams/{userId}")
    public ResponseEntity<TableParamsDTO> getTableParameters(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(ordersAdminsPageService.getParametersForOrdersTable(userId));
    }

    /**
     * Controller for any changes in orders.
     *
     * @author Liubomyr Pater
     */
    @ApiOperation(value = "Change order's properties over request from admin's table")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @PutMapping("/changingOrder")
    public ResponseEntity<List<Long>> saveNewValueFromOrdersTable(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @Valid @RequestBody RequestToChangeOrdersDataDTO requestToChangeOrdersDataDTO) {
        ChangeOrderResponseDTO changeOrderResponseDTO =
            ordersAdminsPageService.chooseOrdersDataSwitcher(userUuid, requestToChangeOrdersDataDTO);
        return ResponseEntity.status(changeOrderResponseDTO.getHttpStatus())
            .body(changeOrderResponseDTO.getUnresolvedGoalsOrderId());
    }

    /**
     * Controller for blocking orders for changes.
     *
     * @author Liubomyr Pater
     */
    @ApiOperation(value = "Block orders for changing by another users")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @PutMapping("/blockOrders")
    public ResponseEntity<List<BlockedOrderDTO>> blockOrders(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @RequestBody List<Long> listOfOrdersId) {
        List<BlockedOrderDTO> blockedOrderDTOS = ordersAdminsPageService.requestToBlockOrder(userUuid, listOfOrdersId);
        return ResponseEntity.status(HttpStatus.OK).body(blockedOrderDTOS);
    }

    /**
     * Controller for unblocking orders for changes.
     *
     * @author Liubomyr Pater
     */
    @ApiOperation(value = "Block orders for changing by another users")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @PutMapping("/unblockOrders")
    public ResponseEntity<List<Long>> unblockOrders(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @RequestBody List<Long> listOfOrdersId) {
        List<Long> unblockedOrdersId = ordersAdminsPageService.unblockOrder(userUuid, listOfOrdersId);
        return ResponseEntity.status(HttpStatus.OK).body(unblockedOrdersId);
    }
}
