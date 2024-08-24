package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.CityDto;
import greencity.dto.DistrictDto;
import greencity.dto.order.*;
import greencity.dto.pageble.PageableDto;
import greencity.dto.table.ColumnWidthDto;
import greencity.dto.table.TableParamsDto;
import greencity.dto.violation.UserViolationsWithUserName;
import greencity.enums.SortingOrder;
import greencity.enums.UkraineRegion;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import greencity.service.ubs.OrdersAdminsPageService;
import greencity.service.ubs.OrdersForUserService;
import greencity.service.ubs.ValuesForUserTableService;
import greencity.service.ubs.ViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
    @Operation(summary = "Get users for the table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_CLIENTS_PAGE', authentication)")
    @GetMapping("/usersAll")
    public ResponseEntity<PageableDto<UserWithSomeOrderDetailDto>> getAllValuesForUserTable(CustomerPage page,
        String columnName, Principal principal,
        @RequestParam SortingOrder sortingOrder,
        UserFilterCriteria userFilterCriteria) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(valuesForUserTable.getAllFields(page, columnName, sortingOrder, userFilterCriteria,
                principal.getName()));
    }

    /**
     * Controller.
     *
     * @author Liubomyr Pater
     */
    @Operation(summary = "Get all parameters for building table of orders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @GetMapping("/tableParams")
    public ResponseEntity<TableParamsDto> getTableParameters(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ordersAdminsPageService.getParametersForOrdersTable(userUuid));
    }

    /**
     * Controller for retrieving data about order table columns width.
     *
     * @param userUuid of {@link String}
     * @author Oleh Kulbaba
     */
    @Operation(summary = "Get width of columns for order table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @GetMapping("/orderTableColumnsWidth")
    public ResponseEntity<ColumnWidthDto> getTableColumnWidthForCurrentUser(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ordersAdminsPageService.getColumnWidthForEmployee(userUuid));
    }

    /**
     * Controller for saving or updating data about order table columns width.
     *
     * @param userUuid       of {@link String}
     * @param columnWidthDto of {@link ColumnWidthDto}
     * @author Oleh Kulbaba
     */
    @Operation(summary = "Edit width of columns for order table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_BIG_ORDER_TABLE', authentication)")
    @PutMapping("/orderTableColumnsWidth")
    public ResponseEntity<HttpStatus> saveTableColumnWidthForCurrentUser(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @RequestBody ColumnWidthDto columnWidthDto) {
        ordersAdminsPageService.saveColumnWidthForEmployee(columnWidthDto, userUuid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for any changes in orders.
     *
     * @author Liubomyr Pater
     */
    @Operation(summary = "Save changes in orders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_ORDER', authentication)")
    @PutMapping("/changingOrder")
    public ResponseEntity<List<Long>> saveNewValueFromOrdersTable(
        @Parameter(hidden = true) HttpServletRequest request,
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
    @Operation(summary = "Block orders for changing by another users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PutMapping("/blockOrders")
    public ResponseEntity<List<BlockedOrderDto>> blockOrders(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @RequestBody List<Long> listOfOrdersId) {
        List<BlockedOrderDto> blockedOrderDTOS = ordersAdminsPageService.requestToBlockOrder(userUuid, listOfOrdersId);
        return ResponseEntity.status(HttpStatus.OK).body(blockedOrderDTOS);
    }

    /**
     * Controller for unblocking orders for changes.
     *
     * @author Liubomyr Pater
     */
    @Operation(summary = "Block orders for changing by another users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PutMapping("/unblockOrders")
    public ResponseEntity<List<Long>> unblockOrders(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
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
    @Operation(summary = "Get users for the table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @GetMapping("/{userId}/ordersAll")
    public ResponseEntity<UserWithOrdersDto> getAllOrdersForUser(
        @Parameter(hidden = true) Pageable page, @PathVariable Long userId,
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
    @Operation(summary = "Get user's violations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @ApiPageable
    @GetMapping("/{userId}/violationsAll")
    public ResponseEntity<UserViolationsWithUserName> getAllViolationsByUser(
        @Parameter(hidden = true) Pageable page, @PathVariable Long userId,
        @RequestParam String columnName,
        @RequestParam SortingOrder sortingOrder) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(violationService.getAllViolations(page, userId, columnName, sortingOrder));
    }
}
