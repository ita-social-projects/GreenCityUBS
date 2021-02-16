package greencity.controller;

import greencity.constants.HttpStatuses;
import greencity.dto.CoordinatesDto;
import greencity.dto.GroupedCoordinatesDto;
import greencity.service.ubs.UBSManagementService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Set;
import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * Method finds undelivered orders.
     *
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get all undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedCoordinatesDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/all-undelivered")
    public ResponseEntity<Set<GroupedCoordinatesDto>> allUndeliveredCoords() {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getAllUndeliveredCoords());
    }

    /**
     * Method groups undelivered orders.
     *
     * @param radius {@link Double} preferred searching radius.
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get grouped undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedCoordinatesDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/group-undelivered")
    public ResponseEntity<Set<GroupedCoordinatesDto>> groupCoords(@RequestParam Double radius,
        @RequestParam(required = false, defaultValue = "3000") Integer litres) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getClusteredCoords(radius, litres));
    }

    /**
     * Method groups orders along with specified.
     */
    @ApiOperation(value = "Get grouped orders along with specified.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedCoordinatesDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("group-undelivered-with-specified")
    public ResponseEntity<Set<GroupedCoordinatesDto>> groupCoordsWithSpecifiedOnes(
        @Valid @RequestBody Set<CoordinatesDto> specified,
        @RequestParam(required = false, defaultValue = "3000") Integer litres,
        @RequestParam(required = false, defaultValue = "0") Double additionalDistance) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getClusteredCoordsAlongWithSpecified(specified, litres, additionalDistance));
    }
}
