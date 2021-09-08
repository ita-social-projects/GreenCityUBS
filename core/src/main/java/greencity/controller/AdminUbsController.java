package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.dto.AllFieldsFromTableDto;
import greencity.dto.PageableDto;
import greencity.filters.SearchCriteria;
import greencity.service.ubs.UBSManagementService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

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
}
