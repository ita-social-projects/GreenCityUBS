package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.entity.enums.SortingOrder;
import greencity.filters.UserFilterCriteria;
import greencity.service.ubs.OrdersAdminsPageService;
import greencity.service.ubs.OrdersForUserService;
import greencity.service.ubs.UserViolationsService;
import greencity.service.ubs.ValuesForUserTableService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
    private final OrdersForUserService ordersForUserService;
    private final UserViolationsService userViolationsService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public AdminUbsController(OrdersAdminsPageService ordersAdminsPageService,
        ValuesForUserTableService valuesForUserTable,
        OrdersForUserService ordersForUserService,
        UserViolationsService userViolationsService) {
        this.ordersAdminsPageService = ordersAdminsPageService;
        this.valuesForUserTable = valuesForUserTable;
        this.ordersForUserService = ordersForUserService;
        this.userViolationsService = userViolationsService;
    }

    /**
     * Controller for obtaining all users that made at least one order.
     *
     * @param columnName   {@link String}
     * @param sortingOrder {@link SortingOrder}
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
    public ResponseEntity<PageableDto<UserWithSomeOrderDetailDto>> getAllValuesForUserTable(@ApiIgnore Pageable page,
        String columnName,
        @RequestParam SortingOrder sortingOrder, UserFilterCriteria userFilterCriteria) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(valuesForUserTable.getAllFields(page, columnName, sortingOrder, userFilterCriteria));
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

    /**
     * Controller for obtaining all order by user and sorting by column name.
     *
     * @param userId {@link Long}
     * @author Roman Sulymka.
     */
    @ApiOperation("Get users for the table")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/{userId}/ordersAll")
    public ResponseEntity<UserWithOrdersDto> getAllOrdersForUser(
        @ApiIgnore Pageable page, @PathVariable Long userId,
        @RequestParam SortingOrder sortingType, @RequestParam String column) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ordersForUserService.getAllOrders(page, userId, sortingType, column));
    }

    /**
     * Controller for obtaining all violations by user.
     *
     * @param userId {@link Long}
     * @author Roman Sulymka.
     */
    @ApiOperation("Get users for the table")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/{userId}/violationsAll")
    public ResponseEntity<UserWithViolationsDto> getAllViolationsByUser(
        @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userViolationsService.getAllViolations(userId));
    }
}
