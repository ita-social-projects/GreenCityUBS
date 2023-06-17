package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.courier.AddingReceivingStationDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.CreateCourierTranslationDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.service.GetTariffServiceDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.service.TariffServiceDto;
import greencity.dto.tariff.AddNewTariffResponseDto;
import greencity.dto.tariff.ChangeTariffLocationStatusDto;
import greencity.dto.tariff.EditTariffDto;
import greencity.dto.tariff.GetTariffLimitsDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.SetTariffLimitsDto;
import greencity.entity.order.Courier;
import greencity.enums.LocationStatus;
import greencity.exceptions.BadRequestException;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.service.SuperAdminService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/ubs/superAdmin")
@RequiredArgsConstructor
class SuperAdminController {
    private final SuperAdminService superAdminService;

    /**
     * Controller for create new tariff service.
     *
     * @param tariffId {@link Long} tariff id.
     * @param dto      {@link TariffServiceDto} dto for tariff service.
     * @param uuid     {@link String} employee uuid.
     * @return {@link GetTariffServiceDto}
     *
     * @author Vadym Makitra.
     * @author Julia Seti
     */
    @ApiOperation(value = "Create new tariff service")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = GetTariffServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @PostMapping("/{tariffId}/createTariffService")
    public ResponseEntity<GetTariffServiceDto> createTariffService(
        @Valid @PathVariable long tariffId,
        @RequestBody @Valid TariffServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.addTariffService(tariffId, dto, uuid));
    }

    /**
     * Controller for get info about tariff services.
     *
     * @param tariffId {@link Long} tariff id.
     * @return {@link List} of {@link GetTariffServiceDto} list of all tariff
     *         services for tariff with id = tariffId.
     * @author Vadym Makitra
     * @author Julia Seti
     */

    @ApiOperation(value = "Get info about tariff services")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_PRICING_CARD', authentication)")
    @GetMapping("/{tariffId}/getTariffService")
    public ResponseEntity<List<GetTariffServiceDto>> getTariffService(
        @Valid @PathVariable long tariffId) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getTariffService(tariffId));
    }

    /**
     * Controller for delete tariff service by Id.
     *
     * @param id {@link Integer} tariff service id.
     * @return {@link HttpStatuses}
     * @author Vadym Makitra.
     */

    @ApiOperation(value = "Delete tariff service by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_DELETE_PRICE_CARD', authentication)")
    @DeleteMapping("/deleteTariffService/{id}")
    public ResponseEntity<HttpStatus> deleteTariffService(
        @Valid @PathVariable Integer id) {
        superAdminService.deleteTariffService(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for edit tariff service by id.
     *
     * @param dto  {@link TariffServiceDto} dto for tariff service.
     * @param id   {@link Integer} tariff service id.
     * @param uuid {@link String} employee uuid.
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra.
     * @author Julia Seti
     */

    @ApiOperation(value = "Edit tariff service by id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_DELETE_PRICE_CARD', authentication)")
    @PutMapping("/editTariffService/{id}")
    public ResponseEntity<GetTariffServiceDto> editTariffService(
        @Valid @RequestBody TariffServiceDto dto,
        @Valid @PathVariable Integer id,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.editTariffService(dto, id, uuid));
    }

    /**
     * Controller for creating new service for tariff.
     *
     * @param tariffId {@link Long} - tariff id.
     * @param dto      {@link ServiceDto} - new service dto.
     * @param uuid     {@link String} - employee uuid.
     * @return {@link GetServiceDto} - created service dto.
     *
     * @author Vadym Makitra
     * @author Julia Seti.
     */

    @ApiOperation(value = "Add service for tariff")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = GetServiceDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @PostMapping("/{tariffId}/createService")
    public ResponseEntity<GetServiceDto> createServices(
        @Valid @PathVariable Long tariffId,
        @Valid @RequestBody ServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.addService(tariffId, dto, uuid));
    }

    /**
     * Controller for getting info about service by tariff id.
     *
     * @param tariffId {@link Long} - tariff id.
     * @return {@link GetServiceDto} - service dto.
     *
     * @author Vadym Makitra
     * @author Julia Seti
     */

    @ApiOperation(value = "Get info about service by tariff id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @GetMapping("/{tariffId}/getService")
    public ResponseEntity<GetServiceDto> getService(
        @Valid @PathVariable Long tariffId) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getService(tariffId));
    }

    /**
     * Controller for delete service by Id.
     * 
     * @param id {@link Long} - service id.
     * @author Vadym Makitra
     */

    @ApiOperation(value = "Delete service by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @DeleteMapping("/deleteService/{id}")
    public ResponseEntity<HttpStatus> deleteService(
        @Valid @PathVariable Long id) {
        superAdminService.deleteService(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for edit service by id.
     *
     * @param id   {@link Long} - service id.
     * @param dto  {@link ServiceDto} - service dto.
     * @param uuid {@link String} - employee uuid.
     * @return {@link GetServiceDto} - edited service dto.
     *
     * @author Vadym Makitra
     * @author Julia Seti
     */

    @ApiOperation(value = "Edit service by id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @PutMapping("/editService/{id}")
    public ResponseEntity<GetServiceDto> editService(
        @Valid @PathVariable Long id,
        @Valid @RequestBody ServiceDto dto,
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @GetMapping("/getLocations")
    public ResponseEntity<List<LocationInfoDto>> getLocations() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllLocation());
    }

    /**
     * Get all info about active locations.
     *
     * @return {@link LocationInfoDto}
     * @author Safarov Renat
     */
    @ApiOperation(value = "Get all active locations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = LocationInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @GetMapping("/getActiveLocations")
    public ResponseEntity<List<LocationInfoDto>> getActiveLocations() {
        return ResponseEntity.status(HttpStatus.OK)
            .body(superAdminService.getLocationsByStatus(LocationStatus.ACTIVE));
    }

    /**
     * Get all deactivated locations.
     *
     * @return {@link LocationInfoDto}
     * @author Maksym Lenets
     */
    @ApiOperation(value = "Get all deactivated locations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = LocationInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @GetMapping("/getDeactivatedLocations")
    public ResponseEntity<List<LocationInfoDto>> getDeactivatedLocations() {
        return ResponseEntity.status(HttpStatus.OK)
            .body(superAdminService.getLocationsByStatus(LocationStatus.DEACTIVATED));
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CREATE_NEW_LOCATION', authentication)")
    @PostMapping("/addLocations")
    public ResponseEntity<HttpStatus> addLocation(
        @Valid @RequestBody List<LocationCreateDto> dto) {
        superAdminService.addLocation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller for deactivating locations by list of locations ids.
     *
     * @param locationIds - list of locations ids
     * @return {@link LocationInfoDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Deactivate locations by locations ids")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = LocationInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_LOCATION_CARD', authentication)")
    @PatchMapping("/deactivateLocations")
    public ResponseEntity<HttpStatus> deactivateLocation(
        @RequestBody List<Long> locationIds) {
        superAdminService.deactivateLocations(locationIds);
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_DELETE_LOCATION_CARD', authentication)")
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
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CREATE_NEW_COURIER', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_COURIER', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @GetMapping("/getCouriers")
    public ResponseEntity<List<CourierDto>> getAllCouriers() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllCouriers());
    }

    /**
     * Controller for deactivate courier's.
     *
     * @param id - courier id that will need to be deleted;
     */
    @ApiOperation(value = "Deactivate courier's by Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping("/deactivateCourier/{id}")
    public ResponseEntity<CourierDto> deactivateCourier(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.deactivateCourier(id));
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CREATE_NEW_STATION', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_DESTINATION_NAME', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_DESTINATION_NAME', authentication)")
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
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_PRICING_CARD', authentication)")
    @GetMapping("/tariffs")
    public ResponseEntity<List<GetTariffsInfoDto>> getAllTariffsInfo(TariffsInfoFilterCriteria filterCriteria) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getAllTariffsInfo(filterCriteria));
    }

    /**
     * Controller for add new tariff info.
     *
     * @return {@link AddNewTariffResponseDto}
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Add new tariff")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 409, message = HttpStatuses.CONFLICT)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @PostMapping("/add-new-tariff")
    public ResponseEntity<AddNewTariffResponseDto> addNewTariff(@RequestBody @Valid AddNewTariffDto addNewTariffDto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.addNewTariff(addNewTariffDto, uuid));
    }

    /**
     * Controller for checking if tariff info is already in the database.
     *
     * @author Inna Yashna
     */
    @ApiOperation(value = "Check if tariff exists")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @PostMapping("/check-if-tariff-exists")
    public ResponseEntity<Boolean> checkIfTariffExists(
        @RequestBody @Valid AddNewTariffDto addNewTariffDto) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.checkIfTariffExists(addNewTariffDto));
    }

    /**
     * Controller for editing tariff info.
     *
     * @param id  {@link Long} tariff id.
     * @param dto {@link EditTariffDto} edited tariff dto.
     *
     * @author Julia Seti
     */
    @ApiOperation(value = "Edit tariff info")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 409, message = HttpStatuses.CONFLICT)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_DELETE_PRICE_CARD', authentication)")
    @PutMapping("/editTariffInfo/{id}")
    public ResponseEntity<HttpStatus> editTariff(
        @Valid @PathVariable Long id, @Valid @RequestBody EditTariffDto dto) {
        superAdminService.editTariff(id, dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for updating Tariff Limits by sum price of Order or by amount of
     * Bags.
     *
     * @param tariffId {@link Long} TariffsInfo id
     * @param dto      {@link SetTariffLimitsDto} dto
     *
     * @author Julia Seti
     */
    @ApiOperation(value = "Set tariff limits")
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_DELETE_PRICE_CARD', authentication)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/setTariffLimits/{tariffId}")
    public ResponseEntity<HttpStatus> setLimitsForTariff(
        @Valid @PathVariable Long tariffId,
        @Valid @RequestBody SetTariffLimitsDto dto) {
        superAdminService.setTariffLimits(tariffId, dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for get info about tariff limits.
     *
     * @param tariffId {@link Long} - tariff id
     * @return {@link GetTariffLimitsDto} - dto
     *
     * @author Julia Seti
     */
    @ApiOperation(value = "Get info about tariff limits")
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffLimitsDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/getTariffLimits/{tariffId}")
    public ResponseEntity<GetTariffLimitsDto> getTariffLimits(
        @Valid @PathVariable long tariffId) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getTariffLimits(tariffId));
    }

    /**
     * Controller for switching tariff activation status.
     *
     * @param tariffId {@link Long} tariff id
     * @param status   {@link String} tariff activation status
     *
     * @author Julia Seti
     */
    @ApiOperation(value = "Switch tariff activation status by tariff id")
    @PreAuthorize("@preAuthorizer.hasAuthority('DEACTIVATE_PRICING_CARD', authentication)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/switchTariffStatus/{tariffId}")
    public ResponseEntity<HttpStatus> switchTariffStatus(
        @PathVariable @ApiParam(name = "tariffId", required = true, value = "tariff id") Long tariffId,
        @Valid @RequestParam @ApiParam(name = "status", required = true, value = "status",
            allowableValues = "Active, Deactivated") String status) {
        superAdminService.switchTariffStatus(tariffId, status);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for editing Locations.
     *
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Edit Locations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_LOCATION_NAME', authentication)")
    @PostMapping("/locations/edit")
    public ResponseEntity<HttpStatus> editLocations(@Valid @RequestBody List<EditLocationDto> editLocationDtoList) {
        superAdminService.editLocations(editLocationDtoList);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for activation or deactivation Locations in Tariff depends
     * on @RequestParam.
     *
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Change Tariff Location status")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_LOCATION_CARD', authentication)")
    @PutMapping("tariffs/{id}/locations/change-status")
    public ResponseEntity<HttpStatus> changeLocationsInTariffStatus(@PathVariable Long id,
        @Valid @RequestBody ChangeTariffLocationStatusDto dto, @RequestParam String status) {
        superAdminService.changeTariffLocationsStatus(id, dto, status);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller to switching activation statuses by chosen parameters.
     *
     * @param regionsIds  - list of regions ids.
     * @param citiesIds   - list of cities ids.
     * @param stationsIds - list of receiving stations ids.
     * @param courierId   - courier id.
     * @author Nikita Korzh, Julia Seti.
     */
    @ApiOperation(value = "Switch activation status by chosen parameters. "
        + "If the deactivation status is selected, the tariff will be deactivated")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('DEACTIVATE_PRICING_CARD', authentication)")
    @PostMapping("/deactivate")
    public ResponseEntity<HttpStatus> switchActivationStatusByChosenParams(
        @RequestParam(name = "regionsIds", required = false) Optional<List<Long>> regionsIds,
        @RequestParam(name = "citiesIds", required = false) Optional<List<Long>> citiesIds,
        @RequestParam(name = "stationsIds", required = false) Optional<List<Long>> stationsIds,
        @RequestParam(name = "courierId", required = false) Optional<Long> courierId,
        @Valid @RequestParam @ApiParam(name = "status", required = true, value = "status",
            allowableValues = "Active, Deactivated") String status) {
        if (regionsIds.isPresent() || citiesIds.isPresent() || stationsIds.isPresent() || courierId.isPresent()) {
            superAdminService.switchActivationStatusByChosenParams(DetailsOfDeactivateTariffsDto.builder()
                .regionsIds(regionsIds)
                .citiesIds(citiesIds)
                .stationsIds(stationsIds)
                .courierId(courierId)
                .activationStatus(status)
                .build());
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            throw new BadRequestException("You should enter at least one parameter");
        }
    }
}
