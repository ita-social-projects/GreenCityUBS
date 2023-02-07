package greencity.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.tariff.AddNewTariffResponseDto;
import greencity.dto.tariff.ChangeTariffLocationStatusDto;
import greencity.dto.service.GetTariffServiceDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.SetTariffLimitsDto;
import greencity.exceptions.BadRequestException;
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

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.AddNewTariffDto;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.AddingReceivingStationDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.CreateCourierTranslationDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.TariffServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.entity.order.Courier;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.service.SuperAdminService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_PRICING_CARD', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_PRICING_CARD', authentication)")
    @PutMapping("/editTariffService/{id}")
    public ResponseEntity<GetTariffServiceDto> editTariffService(
        @RequestBody TariffServiceDto dto,
        @Valid @PathVariable Integer id,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.editTariffService(dto, id, uuid));
    }

    /**
     * Controller for creating new service.
     *
     * @param dto  {@link CreateServiceDto}
     * @param uuid {@link String} - user uuid.
     * @return {@link ServiceDto}
     * @author Vadym Makitra
     * @author Julia Seti.
     */

    @ApiOperation(value = "Add new service")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ServiceDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @PostMapping("/createService")
    public ResponseEntity<ServiceDto> createServices(
        @RequestBody CreateServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.addService(dto, uuid));
    }

    /**
     * Controller for getting info about service by tariff id.
     *
     * @param tariffId {@link Long} - tariff id.
     * @return {@link ServiceDto}
     * @author Vadym Makitra
     * @author Julia Seti
     */

    @ApiOperation(value = "Get info about service by tariff id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @GetMapping("/{tariffId}/getService")
    public ResponseEntity<ServiceDto> getService(
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
     * @param dto  {@link ServiceDto}
     * @param uuid {@link String}
     * @return {@link ServiceDto}
     * @author Vadym Makitra
     * @author Julia Seti
     */

    @ApiOperation(value = "Edit service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ServiceDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @PutMapping("/editService")
    public ResponseEntity<ServiceDto> editService(
        @RequestBody @Valid ServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.editService(dto, uuid));
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
     * Get all info about active locations, and min amount of bag for locations.
     *
     * @return {@link LocationInfoDto}
     * @author Safarov Renat
     */
    @ApiOperation(value = "Get info about active locations and min amount of bags for every location")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = LocationInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @GetMapping("/getActiveLocations")
    public ResponseEntity<List<LocationInfoDto>> getActiveLocations() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getActiveLocations());
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
     * Controller for deactivating location by Id.
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_LOCATION_CARD', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CREATE_NEW_LOCATION_CARD', authentication)")
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
     * Controller for set limit description.
     *
     * @return {@link GetTariffsInfoDto}
     * @author Vadym Makitra
     */
    @ApiOperation(value = "Set new Limit Description")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GetTariffsInfoDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
    @PatchMapping("/setLimitDescription/{tariffId}")
    public ResponseEntity<GetTariffsInfoDto> setLimitDescription(
        @PathVariable Long tariffId, @RequestBody String limitDescription) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(superAdminService.setLimitDescription(tariffId, limitDescription));
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
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/excludeBag/{id}")
    public ResponseEntity<GetTariffServiceDto> excludeBag(
        @PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.excludeBag(id));
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CREATE_NEW_RECEIVING_STATION', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_RECEIVING_STATION', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_RECEIVING_STATION', authentication)")
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
     * Controller for updating TariffsInfo and setting courierLimit by amount of
     * bags.
     *
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Edit tariffLimits by  sum of Bags")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_PRICING_CARD', authentication)")
    @PatchMapping("/setLimitsByAmountOfBags/{tariffId}")
    public ResponseEntity<HttpStatus> setLimitsByAmountOfBags(@Valid @PathVariable Long tariffId,
        @Valid @RequestBody EditAmountOfBagDto dto) {
        superAdminService.setTariffLimitByAmountOfBags(tariffId, dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for updating TariffsInfo and setting courierLimit by sum of Order.
     *
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Edit tariff limits by sum of order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_PRICING_CARD', authentication)")
    @PatchMapping("/setLimitsBySumOfOrder/{tariffId}")
    public ResponseEntity<HttpStatus> setLimitsByPriceOfOrder(@Valid @PathVariable Long tariffId,
        @Valid @RequestBody EditPriceOfOrder dto) {
        superAdminService.setTariffLimitBySumOfOrder(tariffId, dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "Edit tariff limits by sum of order or by sum of Bags")
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_PRICING_CARD', authentication)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping("/setTariffLimits/{tariffId}")
    public ResponseEntity<HttpStatus> setLimitsForTariff(@Valid @PathVariable Long tariffId,
        @Valid @RequestBody SetTariffLimitsDto setTariffLimits) {
        superAdminService.setTariffLimits(tariffId, setTariffLimits);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller for deactivation of TariffsInfo.
     *
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Deactivate tariff")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_PRICING_CARD', authentication)")
    @PutMapping("/deactivateTariff/{tariffId}")
    public ResponseEntity<HttpStatus> deactivateTariff(@PathVariable Long tariffId) {
        superAdminService.deactivateTariffCard(tariffId);
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_LOCATION', authentication)")
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
     * Controller for deactivation tariff with chosen parameters.
     *
     * @param regionsId  - list of regions ids.
     * @param citiesId   - list of cities ids.
     * @param stationsId - list of receiving stations ids.
     * @param courierId  - courier id.
     * @author Nikita Korzh.
     */
    @ApiOperation(value = "Deactivation tariff with chosen parameters.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('DEACTIVATE_TARIFF', authentication)")
    @PostMapping("/deactivate")
    public ResponseEntity<HttpStatus> deactivateTariffForChosenParam(
        @RequestParam(name = "regionsId", required = false) Optional<List<Long>> regionsId,
        @RequestParam(name = "citiesId", required = false) Optional<List<Long>> citiesId,
        @RequestParam(name = "stationsId", required = false) Optional<List<Long>> stationsId,
        @RequestParam(name = "courierId", required = false) Optional<Long> courierId) {
        if (regionsId.isPresent() || citiesId.isPresent() || stationsId.isPresent() || courierId.isPresent()) {
            superAdminService.deactivateTariffForChosenParam(DetailsOfDeactivateTariffsDto.builder()
                .regionsId(regionsId)
                .citiesId(citiesId)
                .stationsId(stationsId)
                .courierId(courierId)
                .build());
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            throw new BadRequestException("You should enter at least one parameter");
        }
    }
}
