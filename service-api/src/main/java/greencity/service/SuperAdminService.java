package greencity.service;

import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.courier.AddingReceivingStationDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.service.TariffServiceDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.*;
import greencity.dto.service.GetTariffServiceDto;
import greencity.entity.order.Courier;
import greencity.enums.LocationStatus;
import greencity.filters.TariffsInfoFilterCriteria;

import java.util.List;

public interface SuperAdminService {
    /**
     * Methods that add new Tariff Service for Tariff.
     *
     * @param tariffId {@link Long} tariff id.
     * @param dto      {@link TariffServiceDto} tariff service dto
     * @param uuid     {@link String} - employee uuid.
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra
     * @author Julia Seti
     */
    GetTariffServiceDto addTariffService(long tariffId, TariffServiceDto dto, String uuid);

    /**
     * Method return All Tariff Service by Tariff id.
     * 
     * @param id {@link Long} - selected tariff id.
     * @return {@link List} of {@link GetTariffServiceDto} - returned list of Tariff
     *         Service.
     * @author Vadym Makitra
     */
    List<GetTariffServiceDto> getTariffService(long id);

    /**
     * Method for delete tariff service by Id.
     *
     * @param id {@link Integer} - Tariff Service Id.
     * @author Vadym Makitra
     */
    void deleteTariffService(Integer id);

    /**
     * Method for edit tariff service by Id.
     *
     * @param dto  {@link TariffServiceDto}
     * @param id   {@link Integer} - tariff service id.
     * @param uuid {@link String} - employee uuid;
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra
     * @author Julia Seti
     */
    GetTariffServiceDto editTariffService(TariffServiceDto dto, Integer id, String uuid);

    /**
     * Method for add new Service for Tariff.
     *
     * @param tariffId {@link Long} - tariff id.
     * @param dto      {@link ServiceDto} - new service dto.
     * @param uuid     {@link String} - employee uuid.
     * @return {@link GetServiceDto} - created service dto.
     * @author Vadym Makitra
     * @author Julia Seti
     */
    GetServiceDto addService(Long tariffId, ServiceDto dto, String uuid);

    /**
     * Method for get service by tariff id.
     *
     * @param tariffId {@link Long} - tariff id.
     * @return {@link GetServiceDto} - service dto.
     * @author Vadym Makitra
     * @author Julia Seti
     */
    GetServiceDto getService(long tariffId);

    /**
     * Method for delete service by id.
     *
     * @param id {@link Long} - Service Id.
     * @author Vadym Makitra
     */
    void deleteService(long id);

    /**
     * Method for editing service by id.
     *
     * @param id   {@link Long} - service id.
     * @param dto  {@link ServiceDto} - entered info about field that need to edit.
     * @param uuid - employee uuid.
     * @return {@link GetServiceDto} - info about edited service.
     * @author Vadym Makitra
     * @author Julia Seti
     */
    GetServiceDto editService(Long id, ServiceDto dto, String uuid);

    /**
     * Method for get all info about location.
     *
     * @return {@link LocationInfoDto}
     * @author Vadym Makitra
     */
    List<LocationInfoDto> getAllLocation();

    /**
     * Method for getting info about locations by LocationStatus.
     *
     * @param locationStatus {@link LocationStatus} - status of searched locations.
     * @return {@link LocationInfoDto}.
     * @author Maksym Lenets.
     */
    List<LocationInfoDto> getLocationsByStatus(LocationStatus locationStatus);

    /**
     * Method for adding location.
     *
     * @param dto {@link LocationCreateDto}
     * @author Vadym Makitra
     */
    void addLocation(List<LocationCreateDto> dto);

    /**
     * Method for deactivate location.
     *
     * @param id - if of location
     */
    void deactivateLocation(Long id);

    /**
     * Method for activate location.
     *
     * @param id - id of Location
     */
    void activateLocation(Long id);

    /**
     * Method for creating courier.
     *
     * @param dto {@link CreateCourierDto} - parameters that's user entered.
     * @return {@link Courier}
     * @author Vadym Makitra
     */
    CreateCourierDto createCourier(CreateCourierDto dto, String uuid);

    /**
     * Method for updating courier.
     *
     * @param dto {@link CourierDto} - parameters that's user entered.
     * @return {@link CourierDto}
     * @author Max Bohonko
     */
    CourierDto updateCourier(CourierUpdateDto dto);

    /**
     * Method for getting all couriers.
     *
     * @return {@link CourierDto} * @author Max Bohonko
     */
    List<CourierDto> getAllCouriers();

    /**
     * Method for change status courier and tariffs to deactivate.
     *
     * @param id - courier Id.
     */
    CourierDto deactivateCourier(Long id);

    /**
     * Method for getting all info about tariffs.
     *
     * @return {@link GetTariffsInfoDto}
     */
    List<GetTariffsInfoDto> getAllTariffsInfo(TariffsInfoFilterCriteria filterCriteria);

    /**
     * Method creates new receiving station.
     *
     * @param dto {@link AddingReceivingStationDto}
     * @return {@link ReceivingStationDto}
     */
    ReceivingStationDto createReceivingStation(AddingReceivingStationDto dto, String uuid);

    /**
     * Method gets all receiving stations.
     *
     * @return {@link ReceivingStationDto}
     */
    List<ReceivingStationDto> getAllReceivingStations();

    /**
     * Method updates information about receiving station.
     *
     * @param dto {@link ReceivingStationDto}
     * @return {@link ReceivingStationDto}
     */
    ReceivingStationDto updateReceivingStation(ReceivingStationDto dto);

    /**
     * Method deletes receiving station by id.
     *
     * @param id {@link Long} receiving station's id
     */
    void deleteReceivingStation(Long id);

    /**
     * Method creates new TariffsInfo.
     *
     * @param addNewTariffDto {@link AddNewTariffDto}
     */
    AddNewTariffResponseDto addNewTariff(AddNewTariffDto addNewTariffDto, String userUUID);

    /**
     * Method for editing TariffsInfo.
     *
     * @param id  {@link Long} tariff id.
     * @param dto {@link EditTariffDto} edited tariff dto.
     */
    void editTariff(Long id, EditTariffDto dto);

    /**
     * Method checks if passed TariffsInfo exists in database.
     *
     * @param addNewTariffDto {@link AddNewTariffDto}
     */
    boolean checkIfTariffExists(AddNewTariffDto addNewTariffDto);

    /**
     * Method for edit info about tariff limits by sum price of Order or by total
     * amount of Bags.
     *
     * @param tariffId        {@link Long} tariff id
     * @param setTariffLimits {@link SetTariffLimitsDto} dto
     *
     * @author Julia Seti
     */
    void setTariffLimits(Long tariffId, SetTariffLimitsDto setTariffLimits);

    /**
     * Method for get info about tariff limits.
     *
     * @param tariffId {@link Long} tariff id
     * @return {@link GetTariffLimitsDto} dto
     *
     * @author Julia Seti
     */
    GetTariffLimitsDto getTariffLimits(Long tariffId);

    /**
     * Method to switch the tariff status to active or deactivated.
     *
     * @param tariffId     {@link Long} tariff id
     * @param tariffStatus {@link String} tariff status
     *
     * @author Julia Seti
     */
    void switchTariffStatus(Long tariffId, String tariffStatus);

    /**
     * Method for editing Locations.
     *
     * @param editLocationDtoList - dto contains id of location wanted to be edited
     *                            and changed naming
     */
    void editLocations(List<EditLocationDto> editLocationDtoList);

    /**
     * Method for changing status of {@link greencity.entity.order.TariffLocation}.
     *
     * @param tariffId - id of tariff where location statuses want to be changed
     * @param dto      - contains List of Location id's to update status
     * @param param    - should be "activate" or "deactivate" or throws
     *                 BadRequestException
     */
    void changeTariffLocationsStatus(Long tariffId, ChangeTariffLocationStatusDto dto, String param);

    /**
     * Method that switch activation status by chosen parameters.
     *
     * @param detailsOfDeactivateTariffsDto - contains list of regionsId, list of
     *                                      citiesId, list of stationsId, courierId
     *                                      and activation status.
     */
    void switchActivationStatusByChosenParams(DetailsOfDeactivateTariffsDto detailsOfDeactivateTariffsDto);
}
