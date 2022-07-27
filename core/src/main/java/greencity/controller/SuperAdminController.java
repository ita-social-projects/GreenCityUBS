package greencity.controller;

import java.util.List;

import javax.validation.Valid;

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
import greencity.dto.AddNewTariffDto;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.AddingReceivingStationDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.GetCourierTranslationsDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.AddNewTariffResponseDto;
import greencity.dto.tariff.ChangeTariffLocationStatusDto;
import greencity.dto.tariff.EditTariffServiceDto;
import greencity.dto.tariff.GetTariffServiceDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.entity.order.Courier;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.service.SuperAdminService;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Validated
@RequestMapping("/ubs/superAdmin")
@RequiredArgsConstructor
class SuperAdminController {
    private final SuperAdminService superAdminService;

    /**
     * Controller for create new tariff.
     *
     * @param dto {@link AddServiceDto} dto for service.
     * @return {@link AddServiceDto}
     * @author Vadym Makitra.
     */
    @ApiOperation(value = "Create new tariff")
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_PRICING_CARD', authentication)")
    @GetMapping("/getTariffService")
    public ResponseEntity<List<GetTariffServiceDto>> createService() {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.getTariffService());
    }

    /**
     * Controller for delete tariff service by Id.
     *
     * @return {@link HttpStatus}
     * @author Vadym Makitra.
     */

    @ApiOperation(value = "Delete tariff by Id")
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CREATE_NEW_LOCATION', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_LOCATION', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CREATE_NEW_LOCATION', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_COURIER', authentication)")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_COURIER', authentication)")
    @DeleteMapping("/courier/{id}")
    public ResponseEntity<HttpStatus> deleteCourier(@PathVariable Long id) {
        superAdminService.deleteCourier(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller creates employee receiving station.
     *
     * @return {@link ReceivingStationDto}
     */
    @ApiOperation(value = "Create employee receiving station")
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
    @PreAuthorize("@preAuthorizer.hasAuthority('CONTROL_SERVICE', authentication)")
    @PostMapping("/add-new-tariff")
    public ResponseEntity<AddNewTariffResponseDto> addNewTariff(@RequestBody @Valid AddNewTariffDto addNewTariffDto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.addNewTariff(addNewTariffDto, uuid));
    }

    /**
     * Controller for updating TariffsInfo and setting courierLimit by amount of
     * bags.
     *
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Edit tariffLimits by  sum of Bags")
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_PRICING_CARD', authentication)")
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
    @ApiOperation(value = "Edit tariff limits by sum of order")
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_PRICING_CARD', authentication)")
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
    @ApiOperation(value = "Deactivate tariff")
    @PreAuthorize("@preAuthorizer.hasAuthority('DELETE_PRICING_CARD', authentication)")
    @PutMapping("/deactivateTariff/{tariffId}")
    public ResponseEntity<String> deactivateTariff(@PathVariable Long tariffId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(superAdminService.deactivateTariffCard(tariffId));
    }

    /**
     * Controller for editing Locations.
     *
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Edit Locations")
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_LOCATION', authentication)")
    @PostMapping("/locations/edit")
    public ResponseEntity<HttpStatus> editLocations(@Valid @RequestBody List<EditLocationDto> editLocationDtoList) {
        superAdminService.editLocations(editLocationDtoList);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * Controller for activation or deactivation Locations in Tariff depends
     * on @RequestParam.
     *
     * @author Yurii Fedorko
     */
    @ApiOperation(value = "Change Tariff Location status")
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_LOCATION', authentication)")
    @PutMapping("tariffs/{id}/locations/change-status")
    public ResponseEntity changeLocationsInTariffStatus(@PathVariable Long id,
        @Valid @RequestBody ChangeTariffLocationStatusDto dto, @RequestParam String status) {
        superAdminService.changeTariffLocationsStatus(id, dto, status);
        return ResponseEntity.ok().build();
    }
}
