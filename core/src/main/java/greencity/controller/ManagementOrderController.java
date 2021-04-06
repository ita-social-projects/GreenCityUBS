package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.dto.CertificateDtoForSearching;
import greencity.dto.CoordinatesDto;
import greencity.dto.GroupedOrderDto;
import greencity.dto.PageableDto;
import greencity.service.ubs.UBSManagementService;
import io.swagger.annotations.*;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/ubs/management")
public class ManagementOrderController {
    private final UBSManagementService ubsManagementService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public ManagementOrderController(UBSManagementService ubsManagementService) {
        this.ubsManagementService = ubsManagementService;
    }

    /**
     * Controller getting all certificates with sorting possibility.
     *
     * @return list of all certificates.
     * @author Nazar Struk
     */
    @ApiOperation(value = "Get all certificates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/getAllCertificates")
    @ApiPageable
    public ResponseEntity<PageableDto<CertificateDtoForSearching>> allCertificates(
        @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getAllCertificates(pageable));
    }

    /**
     * Controller finds undelivered orders.
     *
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get all undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedOrderDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/all-undelivered")
    public ResponseEntity<List<GroupedOrderDto>> allUndeliveredCoords() {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getAllUndeliveredOrdersWithLiters());
    }

    /**
     * Controller groups undelivered orders.
     *
     * @param radius {@link Double} preferred searching radius.
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get grouped undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedOrderDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/group-undelivered")
    public ResponseEntity<List<GroupedOrderDto>> groupCoords(@RequestParam Double radius,
        @RequestParam(required = false, defaultValue = "3000") Integer litres) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getClusteredCoords(radius, litres));
    }

    /**
     * Controller groups orders along with specified.
     */
    @ApiOperation(value = "Get grouped orders along with specified.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedOrderDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/group-undelivered-with-specified")
    public ResponseEntity<List<GroupedOrderDto>> groupCoordsWithSpecifiedOnes(
        @Valid @RequestBody Set<CoordinatesDto> specified,
        @RequestParam(required = false, defaultValue = "3000") Integer litres,
        @RequestParam(required = false, defaultValue = "0") Double additionalDistance) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getClusteredCoordsAlongWithSpecified(specified, litres, additionalDistance));
    }
}
