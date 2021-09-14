package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
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

import javax.validation.Valid;

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
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getParametersForOrdersTable(userId));
    }

    /**
     * Controller.
     *
     * @author Liubomyr Pater
     */
    @ApiOperation(value = "Change order's properties over request from admin's table")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @PostMapping("/changingOrder")
    public ResponseEntity<HttpStatus> saveNewValueFromOrdersTable(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @Valid @RequestBody RequestToChangeOrdersDataDTO requestToChangeOrdersDataDTO) {
        ubsManagementService.saveNewValueIntoOrder(userUuid, requestToChangeOrdersDataDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
