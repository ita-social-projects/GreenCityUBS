package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.entity.enums.EditType;
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
            .add(new ColumnStateDTO("select", new TitleDto("", ""), 20, true, false, 0, EditType.CHECKBOX));
        columnStateDTOS.add(new ColumnStateDTO("orderid", new TitleDto("Замовлення ID", "Order ID"), 20, true,
            false, 1, EditType.READ_ONLY));
        columnStateDTOS.add(new ColumnStateDTO("order_status", new TitleDto("Cтатус замовлення", "Order sataus"), 20,
            true, false, 2, EditType.SELECT));
        columnStateDTOS
            .add(new ColumnStateDTO("order_date", new TitleDto("Аааа", "Aaaa"), 20, true, true, 3, EditType.DATE));
        columnStateDTOS
            .add(new ColumnStateDTO("clientname", new TitleDto("Ббббб", "Bbbb"), 20, false, true, 4, EditType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("phone_number", new TitleDto("Вввв", "Cccc"), 20, false, true, 5, EditType.INLINE));
        columnStateDTOS
            .add(new ColumnStateDTO("email", new TitleDto("Гггг", "Dddd"), 20, true, false, 6, EditType.INLINE));
        columnStateDTOS
            .add(new ColumnStateDTO("violations", new TitleDto("Дддд", "Eeee"), 20, true, false, 7, EditType.READ_ONLY));
        columnStateDTOS
            .add(new ColumnStateDTO("district", new TitleDto("Еееее", "Ffff"), 20, true, false, 8, EditType.READ_ONLY));
        columnStateDTOS
            .add(new ColumnStateDTO("address", new TitleDto("Єєєє", "Gggg"), 20, true, false, 9, EditType.READ_ONLY));
        columnStateDTOS.add(
            new ColumnStateDTO("recipient_name", new TitleDto("Жжжж", "Hhhhh"), 20, true, false, 10, EditType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("recipient_phone", new TitleDto("Ззззз", "Oooo"), 20, true, false, 11, EditType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("recipient_email", new TitleDto("Иииии", "Pppp"), 20, true, false, 12, EditType.INLINE));
        columnStateDTOS.add(new ColumnStateDTO("comment_to_address_for_client", new TitleDto("Іііі", ""), 20, true,
            false, 13, EditType.INLINE));
        columnStateDTOS.add(new ColumnStateDTO("garbage_bags_120_amount", new TitleDto("Їїїїї", "Kkkk"), 20, true, false,
            14, EditType.INLINE));
        columnStateDTOS.add(new ColumnStateDTO("bo_bags_120_amount", new TitleDto("Йййй", "Lllll"), 20, true, false, 15,
            EditType.INLINE));
        columnStateDTOS.add(new ColumnStateDTO("bo_bags_20_amount", new TitleDto("Кккк", "Mmmmm"), 20, true, false, 16,
            EditType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("total_order_sum", new TitleDto("Ллллл", "Nnnn"), 20, true, false, 17, EditType.READ_ONLY));
        columnStateDTOS.add(new ColumnStateDTO("order_certificate_code", new TitleDto("Мммм", "Ssss"), 20, true, false,
            18, EditType.READ_ONLY));
        columnStateDTOS.add(new ColumnStateDTO("order_certificate_points", new TitleDto("Ннннн", "Ttttt"), 20, true, false,
            19, EditType.READ_ONLY));
        columnStateDTOS
            .add(new ColumnStateDTO("amount_due", new TitleDto("Ооооо", ""), 20, true, false, 20, EditType.READ_ONLY));
        columnStateDTOS.add(new ColumnStateDTO("comment_for_order_by_client", new TitleDto("Пппп", "Rrrr"), 20, true,
            false, 21, EditType.INLINE));
        columnStateDTOS.add(
            new ColumnStateDTO("payment_system", new TitleDto("Рррр", "Xxx"), 20, true, false, 22, EditType.READ_ONLY));
        columnStateDTOS.add(
            new ColumnStateDTO("date_of_export", new TitleDto("Сссс", "Yyyy"), 20, true, false, 23, EditType.DATE));
        columnStateDTOS.add(
            new ColumnStateDTO("time_of_export", new TitleDto("Ттттт", "Zzzzz"), 20, true, false, 24, EditType.READ_ONLY));
        columnStateDTOS.add(new ColumnStateDTO("id_order_from_shop", new TitleDto("Уууу", "Hhhkh"), 20, true, false, 25,
            EditType.READ_ONLY));
        columnStateDTOS.add(
            new ColumnStateDTO("receiving_station", new TitleDto("Ффффф", ""), 20, true, false, 26, EditType.READ_ONLY));
        columnStateDTOS.add(new ColumnStateDTO("responsible_manager", new TitleDto("Хххх", "Rytryt"), 20, true, false, 27,
            EditType.SELECT));
        columnStateDTOS.add(new ColumnStateDTO("responsible_logic_man", new TitleDto("Цццц", "Hhjkhk"), 20, true, false,
            28, EditType.SELECT));
        columnStateDTOS.add(new ColumnStateDTO("responsible_driver", new TitleDto("Чччч", "Wwrwew"), 20, true, false, 29,
            EditType.SELECT));
        columnStateDTOS.add(new ColumnStateDTO("responsible_navigator", new TitleDto("Шшшш", "Qqeqw"), 20, true, false,
            30, EditType.SELECT));
        columnStateDTOS.add(new ColumnStateDTO("comments_for_order", new TitleDto("Щщщщ", "Mjhjhk"), 20, true, false, 31,
            EditType.INLINE));

        TableParamsDTO paramsDTO = new TableParamsDTO(columnStateDTOS, "orderid", SortingOrder.ASC);
        return ResponseEntity.status(HttpStatus.OK).body(paramsDTO);
    }
}
