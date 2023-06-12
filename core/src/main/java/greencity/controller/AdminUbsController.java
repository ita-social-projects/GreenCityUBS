package greencity.controller;

import greencity.annotations.*;
import greencity.constants.HttpStatuses;
import greencity.dto.order.*;
import greencity.dto.pageble.PageableDto;
import greencity.dto.table.ColumnWidthDto;
import greencity.dto.table.TableParamsDto;
import greencity.dto.violation.UserViolationsWithUserName;
import greencity.enums.SortingOrder;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import greencity.service.ubs.OrdersAdminsPageService;
import greencity.service.ubs.OrdersForUserService;
import greencity.service.ubs.ValuesForUserTableService;
import greencity.service.ubs.ViolationService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/ubs/management")
@RequiredArgsConstructor
public class AdminUbsController {
    private final OrdersAdminsPageService ordersAdminsPageService;
    private final ValuesForUserTableService valuesForUserTable;
    private final OrdersForUserService ordersForUserService;
    private final ViolationService violationService;

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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_CLIENTS_PAGE', authentication)")
    @GetMapping("/usersAll")
    public ResponseEntity<PageableDto<UserWithSomeOrderDetailDto>> getAllValuesForUserTable(CustomerPage page,
        String columnName, Principal principal,
        @RequestParam SortingOrder sortingOrder, UserFilterCriteria userFilterCriteria) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(valuesForUserTable.getAllFields(page, columnName, sortingOrder, userFilterCriteria,
                principal.getName()));
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
    @GetMapping("/tableParams")
    public ResponseEntity<TableParamsDto> getTableParameters(
        @ApiIgnore @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ordersAdminsPageService.getParametersForOrdersTable(userUuid));
    }

    /**
     * Controller for retrieving data about order table columns width.
     *
     * @param userUuid of {@link String}
     * @author Oleh Kulbaba
     */
    @ApiOperation("Get width of columns for order table")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @GetMapping("/orderTableColumnsWidth")
    public ResponseEntity<ColumnWidthDto> getTableColumnWidthForCurrentUser(
        @ApiIgnore @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ordersAdminsPageService.getColumnWidthForEmployee(userUuid));
    }

    /**
     * Controller for saving or updating data about order table columns width.
     *
     * @param userUuid       of {@link String}
     * @param columnWidthDto of {@link ColumnWidthDto}
     * @author Oleh Kulbaba
     */
    @ApiOperation("Edit width of columns for order table")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @PutMapping("/orderTableColumnsWidth")
    public ResponseEntity<HttpStatus> saveTableColumnWidthForCurrentUser(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @RequestBody ColumnWidthDto columnWidthDto) {
        ordersAdminsPageService.saveColumnWidthForEmployee(columnWidthDto, userUuid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for any changes in orders.
     *
     * @author Liubomyr Pater
     */
    @ApiOperation(value = "Save changes in orders")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @PutMapping("/changingOrder")
    public ResponseEntity<List<Long>> saveNewValueFromOrdersTable(
        @ApiIgnore HttpServletRequest request,
        @Valid @RequestBody RequestToChangeOrdersDataDto requestToChangeOrdersDataDTO) {
        ChangeOrderResponseDTO changeOrderResponseDTO =
            ordersAdminsPageService.chooseOrdersDataSwitcher(request.getUserPrincipal().getName(),
                requestToChangeOrdersDataDTO);
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
    public ResponseEntity<List<BlockedOrderDto>> blockOrders(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @RequestBody List<Long> listOfOrdersId) {
        List<BlockedOrderDto> blockedOrderDTOS = ordersAdminsPageService.requestToBlockOrder(userUuid, listOfOrdersId);
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
     * @author Roman Sulymka and Max Bohonko.
     */
    @ApiOperation("Get user's violations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @ApiPageable
    @GetMapping("/{userId}/violationsAll")
    public ResponseEntity<UserViolationsWithUserName> getAllViolationsByUser(
        @ApiIgnore Pageable page, @PathVariable Long userId,
        @RequestParam String columnName,
        @RequestParam SortingOrder sortingOrder) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(violationService.getAllViolations(page, userId, columnName, sortingOrder));
    }
}
