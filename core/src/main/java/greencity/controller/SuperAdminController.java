package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.*;
import greencity.dto.location.GetCourierLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.EditTariffInfoDto;
import greencity.dto.tariff.EditTariffServiceDto;
import greencity.dto.tariff.GetTariffServiceDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.entity.order.Courier;
import greencity.service.SuperAdminService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
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
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = AddServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping("/createTariffService")
    public ResponseEntity<AddServiceDto> createTariffService(
        @RequestBody AddServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.addTariffService(dto, uuid));
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
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
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = CreateServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping("/createService")
    public ResponseEntity<CreateServiceDto> createServices(
        @RequestBody CreateServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.addService(dto, uuid));
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PutMapping("/editService/{id}")
    public ResponseEntity<GetServiceDto> editService(
        @Valid @PathVariable Long id,
        @RequestBody @Valid EditServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.editService(id, dto, uuid));
    }

    /**
     * Get all info about locations, and min amount of bag for locations.
     *
     * @return {@link LocationInfoDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Get info about location and min amount of bag for this location")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = LocationInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/getLocations")
    public ResponseEntity<List<LocationInfoDto>> getLocations() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllLocation());
    }

    /**
     * Create new Location.
     *
     * @param dto {@link LocationCreateDto}
     * @return {@link LocationInfoDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Create new location")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping("/addLocations")
    public ResponseEntity<HttpStatus> addLocation(
        @Valid @RequestBody List<LocationCreateDto> dto) {
        superAdminService.addLocation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for deactivating location byb Id.
     *
     * @param id - id of location
     * @return {@link LocationInfoDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Deactivate location by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = LocationInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping("/deactivateLocations/{id}")
    public ResponseEntity<HttpStatus> deactivateLocation(
        @PathVariable Long id) {
        superAdminService.deactivateLocation(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for activating location by Id.
     *
     * @param id - id location
     * @return {@link LocationInfoDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Active location Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = LocationInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping("/activeLocations/{id}")
    public ResponseEntity<HttpStatus> activeLocation(
        @PathVariable Long id) {
        superAdminService.activateLocation(id);
        return ResponseEntity.status(HttpStatus.OK).build();
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping("/createCourier")
    public ResponseEntity<CreateCourierDto> addService(
        @Valid @RequestBody CreateCourierDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.createCourier(dto, uuid));
    }

    /**
     * Controller updates courier.
     *
     * @return {@link CourierDto}
     */
    @ApiOperation(value = "Update courier")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = CourierDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/update-courier")
    public ResponseEntity<CourierDto> updateCourier(@RequestBody @Valid CourierUpdateDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.updateCourier(dto));
    }

    /**
     * Controller for get all info about couriers.
     *
     * @return {@link CourierDto}
     * @author Max Bohonko
     */
    @ApiOperation(value = "Get all info about couriers")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = CourierDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/getCouriers")
    public ResponseEntity<List<CourierDto>> getAllCouriers() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllCouriers());
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping("/setLimitDescription/{courierId}")
    public ResponseEntity<GetCourierTranslationsDto> setLimitDescription(
        @PathVariable Long courierId, String limitDescription) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(superAdminService.setLimitDescription(courierId, limitDescription));
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping("/excludeBag/{id}")
    public ResponseEntity<GetTariffServiceDto> excludeBag(
        @PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.excludeBag(id));
    }

    /**
     * Controller for delete courier's.
     *
     * @param id - courier id that will need to be deleted;
     */
    @ApiOperation(value = "Delete courier's by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @DeleteMapping("/courier/{id}")
    public ResponseEntity<HttpStatuses> deleteCourier(@PathVariable Long id) {
        superAdminService.deleteCourier(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller creates employee receiving station.
     *
     * @return {@link ReceivingStationDto}
     */
    @ApiOperation(value = "Create employee receiving station")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ReceivingStationDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PostMapping("/create-receiving-station")
    public ResponseEntity<ReceivingStationDto> createReceivingStation(@Valid AddingReceivingStationDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.createReceivingStation(dto, uuid));
    }

    /**
     * Controller updates receiving station.
     *
     * @return {@link ReceivingStationDto}
     */
    @ApiOperation(value = "Update employee receiving station")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ReceivingStationDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/update-receiving-station")
    public ResponseEntity<ReceivingStationDto> updateReceivingStation(@RequestBody @Valid ReceivingStationDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.updateReceivingStation(dto));
    }

    /**
     * Controller gets all employee receiving stations.
     *
     * @return {@link ReceivingStationDto}
     */
    @ApiOperation(value = "Get all employee receiving stations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ReceivingStationDto[].class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/get-all-receiving-station")
    public ResponseEntity<List<ReceivingStationDto>> getAllReceivingStation() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllReceivingStations());
    }

    /**
     * Controller deletes employee receiving station.
     *
     */
    @ApiOperation(value = "Deletes employee receiving station")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete-receiving-station/{id}")
    public ResponseEntity<HttpStatus> deleteReceivingStation(@PathVariable Long id) {
        superAdminService.deleteReceivingStation(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller for get all tariff's info.
     *
     * @return {@link GetTariffsInfoDto}
     * @author Bohdan Melnyk
     */
    @ApiOperation(value = "Get all info about tariffs.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/tariffs/all")
    public ResponseEntity<List<GetTariffsInfoDto>> getAllTariffsInfo() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllTariffsInfo());
    }

    /**
     * Controller for add new tariff info.
     *
     * @return {@link GetTariffsInfoDto}
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Add new tariff.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PostMapping("/add-new-tariff")
    public ResponseEntity<HttpStatus> addNewTariff(@RequestBody @Valid AddNewTariffDto addNewTariffDto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        superAdminService.addNewTariff(addNewTariffDto, uuid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for updating TariffsInfo and setting courierLimit by amount of
     * bags.
     *
     * @author Yurii Fedorko
     */
    @PatchMapping("/setLimitsByAmountOfBags/{tariffId}")
    public ResponseEntity<HttpStatus> setLimitsByAmountOfBags(@Valid @PathVariable Long tariffId,
        @Valid @RequestBody EditAmountOfBagDto dto) {
        superAdminService.setTariffLimitByAmountOfBags(tariffId, dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * Controller for updating TariffsInfo and setting courierLimit by sum of Order.
     *
     * @author Yurii Fedorko
     */
    @PatchMapping("/setLimitsBySumOfOrder/{tariffId}")
    public ResponseEntity<HttpStatus> setLimitsByPriceOfOrder(@Valid @PathVariable Long tariffId,
        @Valid @RequestBody EditPriceOfOrder dto) {
        superAdminService.setTariffLimitBySumOfOrder(tariffId, dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * Controller for deleting or deactivation of TariffsInfo.
     *
     * @author Yurii Fedorko
     */
    @PutMapping("/deactivateTariff/{tariffId}")
    public ResponseEntity<String> deactivateTariff(@PathVariable Long tariffId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(superAdminService.deactivateTariffCard(tariffId));
    }
}
