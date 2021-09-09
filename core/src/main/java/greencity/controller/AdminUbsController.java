package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.entity.enums.SelectType;
import greencity.entity.enums.SortingOrder;
import greencity.filters.SearchCriteria;
import greencity.service.ubs.UBSManagementService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ubs/management")
public class AdminUbsController {
    private final UBSManagementService ubsManagementService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public AdminUbsController(UBSManagementService ubsManagementService) {
        this.ubsManagementService = ubsManagementService;
    }

    /**
     * Controller.
     *
     * @author Liubomyr Pater
     */
    @ApiOperation("Get all info from Table orders")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/ordersNew")
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
        List<ColumnStateDTO> columnStateDTOS = new ArrayList<>();
        columnStateDTOS
            .add(new ColumnStateDTO("select", new TitleDto("", ""), 20, true, true, false, 0, SelectType.CHECKBOX));
        columnStateDTOS.add(new ColumnStateDTO("orderid", new TitleDto("Замовлення ID", "Order ID"), 20, true, true,
            false, 1, SelectType.NONE));
        columnStateDTOS.add(new ColumnStateDTO("order_status", new TitleDto("Cтатус замовлення", "Order sataus"), 20,
            true, true, false, 2, SelectType.SELECT));
        columnStateDTOS
            .add(new ColumnStateDTO("order_date", new TitleDto("", ""), 20, true, true, false, 3, SelectType.DATE));
        columnStateDTOS
            .add(new ColumnStateDTO("clientname", new TitleDto("", ""), 20, false, true, false, 4, SelectType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("phone_number", new TitleDto("", ""), 20, false, true, false, 5, SelectType.INLINE));
        columnStateDTOS
            .add(new ColumnStateDTO("email", new TitleDto("", ""), 20, false, true, false, 6, SelectType.INLINE));
        columnStateDTOS
            .add(new ColumnStateDTO("violations", new TitleDto("", ""), 20, false, true, false, 7, SelectType.NONE));
        columnStateDTOS
            .add(new ColumnStateDTO("district", new TitleDto("", ""), 20, false, true, false, 8, SelectType.NONE));
        columnStateDTOS
            .add(new ColumnStateDTO("address", new TitleDto("", ""), 20, false, true, false, 9, SelectType.NONE));
        columnStateDTOS.add(
            new ColumnStateDTO("recipient_name", new TitleDto("", ""), 20, false, true, false, 10, SelectType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("recipient_phone", new TitleDto("", ""), 20, false, true, false, 11, SelectType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("recipient_email", new TitleDto("", ""), 20, false, true, false, 12, SelectType.INLINE));
        columnStateDTOS.add(new ColumnStateDTO("comment_to_address_for_client", new TitleDto("", ""), 20, false, true,
            false, 13, SelectType.INLINE));
        columnStateDTOS.add(new ColumnStateDTO("garbage_bags_120_amount", new TitleDto("", ""), 20, false, true, false,
            14, SelectType.INLINE));
        columnStateDTOS.add(new ColumnStateDTO("bo_bags_120_amount", new TitleDto("", ""), 20, false, true, false, 15,
            SelectType.INLINE));
        columnStateDTOS.add(new ColumnStateDTO("bo_bags_20_amount", new TitleDto("", ""), 20, false, true, false, 16,
            SelectType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("total_order_sum", new TitleDto("", ""), 20, false, true, false, 17, SelectType.NONE));
        columnStateDTOS.add(new ColumnStateDTO("order_certificate_code", new TitleDto("", ""), 20, false, true, false,
            18, SelectType.NONE));
        columnStateDTOS.add(new ColumnStateDTO("order_certificate_points", new TitleDto("", ""), 20, false, true, false,
            19, SelectType.NONE));
        columnStateDTOS
            .add(new ColumnStateDTO("amount_due", new TitleDto("", ""), 20, false, true, false, 20, SelectType.NONE));
        columnStateDTOS.add(new ColumnStateDTO("comment_for_order_by_client", new TitleDto("", ""), 20, false, true,
            false, 21, SelectType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("payment_system", new TitleDto("", ""), 20, false, true, false, 22, SelectType.NONE));
        columnStateDTOS.add(
            new ColumnStateDTO("date_of_export", new TitleDto("", ""), 20, false, true, false, 23, SelectType.DATE));
        columnStateDTOS.add(
            new ColumnStateDTO("time_of_export", new TitleDto("", ""), 20, false, true, false, 24, SelectType.NONE));
        columnStateDTOS.add(new ColumnStateDTO("id_order_from_shop", new TitleDto("", ""), 20, false, true, false, 25,
            SelectType.NONE));
        columnStateDTOS.add(
            new ColumnStateDTO("receiving_station", new TitleDto("", ""), 20, false, true, false, 26, SelectType.NONE));
        columnStateDTOS.add(new ColumnStateDTO("responsible_manager", new TitleDto("", ""), 20, false, true, false, 27,
            SelectType.SELECT));
        columnStateDTOS.add(new ColumnStateDTO("responsible_logic_man", new TitleDto("", ""), 20, false, true, false,
            28, SelectType.SELECT));
        columnStateDTOS.add(new ColumnStateDTO("responsible_driver", new TitleDto("", ""), 20, false, true, false, 29,
            SelectType.SELECT));
        columnStateDTOS.add(new ColumnStateDTO("responsible_navigator", new TitleDto("", ""), 20, false, true, false,
            30, SelectType.SELECT));
        columnStateDTOS.add(new ColumnStateDTO("comments_for_order", new TitleDto("", ""), 20, false, true, false, 31,
            SelectType.INLINE));

        TableParamsDTO paramsDTO = new TableParamsDTO(columnStateDTOS, "orderid", SortingOrder.ASC);
        return ResponseEntity.status(HttpStatus.OK).body(paramsDTO);
    }
}
