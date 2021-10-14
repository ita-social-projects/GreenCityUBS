package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.entity.order.Courier;
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
     * Controller for create new tariff.
     *
     * @param dto {@link AddServiceDto} dto for service.
     * @return {@link AddServiceDto}
     * @author Vadym Makitra.
     */
    @ApiOperation(value = "Create new tariff")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = AddServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/createTariffService")
    public ResponseEntity<AddServiceDto> createTariffService(
        @RequestBody AddServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.addTariffService(dto, uuid));
    }

    /**
     * Controller for get all info about tariff.
     * 
     * @return {@link GetTariffServiceDto} list of all tariff service.
     * @author Vadym Makitra.
     */

    @ApiOperation(value = "Get all info about tariff")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffServiceDto.class),
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

    @ApiOperation(value = "Delete tariff by Id")
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

    @ApiOperation(value = "Edit tariff by id")
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
     * Controller for creating new service.
     *
     * @param dto  {@link CreateServiceDto}
     * @param uuid {@link String} - user uuid.
     * @return {@link CreateServiceDto}
     * @author Vadym Makitra.
     */

    @ApiOperation(value = "Add new service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = CreateServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/createService")
    public ResponseEntity<CreateServiceDto> createServices(
        @RequestBody CreateServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.addService(dto, uuid));
    }

    /**
     * Controller for getting all info about service.
     * 
     * @return {@link GetServiceDto}
     * @author Vadym Makitra
     */

    @ApiOperation(value = "Get all info about service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/getService")
    public ResponseEntity<List<GetServiceDto>> getService() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getService());
    }

    /**
     * Controller for delete service by Id.
     *
     * @author Vadym Makitra
     */

    @ApiOperation(value = "Delete service by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @DeleteMapping("/deleteService/{id}")
    public ResponseEntity<HttpStatus> deleteService(
        @Valid @PathVariable Long id) {
        superAdminService.deleteService(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for edit service by Id.
     *
     * @author Vadym Makitra
     */

    @ApiOperation(value = "Edit service by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PutMapping("/editService/{id}")
    public ResponseEntity<GetServiceDto> deleteService(
        @Valid @PathVariable Long id,
        @RequestBody @Valid EditServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.editService(id, dto, uuid));
    }

    /**
     * Get all info about locations, and min amount of bag for locations.
     * 
     * @return {@link GetLocationTranslationDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Get info about location and min amount of bag for this location")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetLocationTranslationDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/getLocations")
    public ResponseEntity<List<GetLocationTranslationDto>> getLocations() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllLocation());
    }

    /**
     * Create new Location.
     * 
     * @param dto {@link AddLocationDto}
     * @return {@link GetLocationTranslationDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Create new location")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = AddLocationDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/addLocations")
    public ResponseEntity<AddLocationDto> addLocation(
        @RequestBody AddLocationDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.addLocation(dto));
    }

    /**
     * Controller for deactivating location byb Id.
     * 
     * @param id - id of location
     * @return {@link GetLocationTranslationDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Deactivate location by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetLocationTranslationDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/deactivateLocations/{id}")
    public ResponseEntity<GetLocationTranslationDto> deactivateLocation(
        @PathVariable Long id, String languageCode) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.deactivateLocation(id, languageCode));
    }

    /**
     * Controller for activating location by Id.
     * 
     * @param id - id location
     * @return {@link GetLocationTranslationDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Active location Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetLocationTranslationDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/activeLocations/{id}")
    public ResponseEntity<GetLocationTranslationDto> activeLocation(
        @PathVariable Long id, String languageCode) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.activateLocation(id, languageCode));
    }

    /**
     * Controller for creating new courier.
     *
     * @param dto {@link CreateCourierDto}
     * @return {@link Courier}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Create new Courier")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = CreateCourierTranslationDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/createCourier")
    public ResponseEntity<CreateCourierDto> addService(
        @RequestBody CreateCourierDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.createCourier(dto));
    }

    /**
     * Controller for get all info about couriers.
     *
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Get all info about couriers")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetCourierTranslationsDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/getCouriers")
    public ResponseEntity<List<GetCourierTranslationsDto>> getAllCouriers() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllCouriers());
    }

    /**
     * Controller for set amount of sum.
     *
     * @param id  - id of courier.
     * @param dto {@link EditPriceOfOrder} - entered info about new Price.
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Set limit by amount of sum")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetCourierTranslationsDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/setAmountOfSum/{id}")
    public ResponseEntity<GetCourierTranslationsDto> setAmountOfBag(
        @PathVariable Long id, @RequestBody EditPriceOfOrder dto) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.setCourierLimitBySumOfOrder(id, dto));
    }

    /**
     * Controller for set amount of bag.
     *
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Set limit by amount of bag")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetCourierTranslationsDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/setAmountOfBag/{id}")
    public ResponseEntity<GetCourierTranslationsDto> setAmountOfSum(
        @PathVariable Long id, @RequestBody EditAmountOfBagDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.setCourierLimitByAmountOfBag(id, dto));
    }

    /**
     * Controller for set limit description.
     *
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Set new Limit Description")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetCourierTranslationsDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/setLimitDescription/{id}")
    public ResponseEntity<GetCourierTranslationsDto> setLimitDescription(
        @PathVariable Long id, String limitDescription) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.setLimitDescription(id, limitDescription));
    }

    /**
     * Controller for include Bag.
     *
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Include bag")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/includeBag/{id}")
    public ResponseEntity<GetTariffServiceDto> includeBag(
        @PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.includeBag(id));
    }

    /**
     * Controller for include Bag.
     *
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Exclude bag")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PatchMapping("/excludeBag/{id}")
    public ResponseEntity<GetTariffServiceDto> excludeBag(
        @PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.excludeBag(id));
    }
}