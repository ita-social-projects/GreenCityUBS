package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.AddServiceDto;
import greencity.dto.CreateServiceDto;
import greencity.dto.EditTariffServiceDto;
import greencity.dto.GetTariffServiceDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Service;
import greencity.service.SuperAdminService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/ubs/superAdmin")
class SuperAdminController {
    private final SuperAdminService superAdminService;

    /**
     * Constructor for initialize SuperAdminService.
     */
    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    /**
     * Controller created tariff Service.
     *
     * @param dto {@link AddServiceDto} dto for service.
     * @return {@link Bag}
     * @author Vadym Makitra.
     */
    @ApiOperation(value = "Create tariff service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = AddServiceDto[].class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/createTariffService")
    public ResponseEntity<Bag> createTariffService(
        @RequestBody AddServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.addTariffService(dto, uuid));
    }

    /**
     * Controller got all tariff service and return List of this tariff.
     * 
     * @return {@link GetTariffServiceDto} list of all tariff service.
     * @author Vadym Makitra.
     */

    @ApiOperation(value = "Get tariff service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = AddServiceDto[].class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/getTariffService")
    public ResponseEntity<List<GetTariffServiceDto>> createService() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getTariffService());
    }

    /**
     * Controller for delete tariff service by Id.
     *
     * @return {@link HttpStatuses}
     * @author Vadym Makitra.
     */

    @ApiOperation(value = "Delete tariff service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @DeleteMapping("/deleteTariffService/{id}")
    public ResponseEntity<HttpStatus> deleteTariffService(
        @Valid @PathVariable Integer id) {
        superAdminService.deleteTariffService(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for edit tariff service by Id.
     *
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra.
     */

    @ApiOperation(value = "Edit tariff service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PutMapping("/editTariffService/{id}")
    public ResponseEntity<GetTariffServiceDto> editTariffService(
        @RequestBody EditTariffServiceDto editTariff, @Valid @PathVariable Integer id,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.editTariffService(editTariff, id, uuid));
    }

    /**
     * Controller for creating new tariff.
     *
     * @param dto  {@link CreateServiceDto}
     * @param uuid {@link String} - user uuid.
     * @return {@link Service}
     * @author Vadym Makitra
     */

    @ApiOperation(value = "Add new tariff")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/createTariff")
    public ResponseEntity<Service> createTariff(
        @RequestBody CreateServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.addService(dto, uuid));
    }
}
