package greencity.service;

import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.*;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.*;
import greencity.entity.order.Courier;
import greencity.entity.order.Service;
import greencity.filters.TariffsInfoFilterCriteria;

import java.util.List;

public interface SuperAdminService {
    /**
     * Methods that add new Service.
     *
     * @param dto  {@link AddServiceDto}
     * @param uuid {@link String} - uuid current user.
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra
     */
    AddServiceDto addTariffService(AddServiceDto dto, String uuid);

    /**
     * Method return All Tariff Service.
     *
     * @return {@link GetTariffServiceDto} - returned list of Tariff Service.
     * @author Vadym Makitra
     */
    List<GetTariffServiceDto> getTariffService();

    /**
     * Method for delete tariff service by Id.
     *
     * @param id - Tariff Service Id.
     * @author Vadym Makitra
     */
    void deleteTariffService(Integer id);

    /**
     * Method for edit tariff service by Id.
     *
     * @param dto  {@link EditTariffServiceDto}
     * @param id   {@link Long} - selected tariff id.
     * @param uuid {@link String} - current user;
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra
     */
    GetTariffServiceDto editTariffService(EditTariffServiceDto dto, Integer id, String uuid);

    /**
     * Method for add new Service.
     *
     * @param dto  {@link CreateServiceDto}
     * @param uuid {@link String} - user uuid.
     * @return {@link Service}
     * @author Vadym Makitra
     */
    CreateServiceDto addService(CreateServiceDto dto, String uuid);

    /**
     * Method for get All service.
     *
     * @return {@link GetServiceDto}
     * @author Vadym Makitra
     */
    List<GetServiceDto> getService();

    /**
     * Method for delete service by Id.
     *
     * @param id - Service Id.
     * @author Vadym Makitra
     */
    void deleteService(long id);

    /**
     * Method for editing service by Id.
     *
     * @param id   - id of current service.
     * @param dto  - entered info about field that need to edit.
     * @param uuid - user uuid.
     * @return {@link GetServiceDto} - info about edited service.
     * @author Vadym Makitra
     */
    GetServiceDto editService(long id, EditServiceDto dto, String uuid);

    /**
     * Method for get all info about location.
     *
     * @return {@link LocationInfoDto}
     * @author Vadym Makitra
     */
    List<LocationInfoDto> getAllLocation();

    /**
     * Method for get all info about active locations.
     *
     * @return {@link LocationInfoDto}.
     * @author Safarov Renat.
     */
    List<LocationInfoDto> getActiveLocations();

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
    public CourierDto updateCourier(CourierUpdateDto dto);

    /**
     * Method for getting all couriers.
     *
     * @return {@link CourierDto} * @author Max Bohonko
     */
    List<CourierDto> getAllCouriers();

    /**
     * Method for edit limit description.
     *
     * @param courierId        - id of courier
     * @param limitDescription - new limit description.
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    GetCourierTranslationsDto setLimitDescription(Long courierId, String limitDescription);

    /**
     * Method for include bag into minimum set of package.
     *
     * @param id - if of bag.
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra
     */
    GetTariffServiceDto includeBag(Integer id);

    /**
     * Method for exclude bag from minimum set of package.
     *
     * @param id - if of bag.
     * @return {@link GetTariffServiceDto}
     * @author Vadym Makitra
     */
    GetTariffServiceDto excludeBag(Integer id);

    /**
     * Method for delete courier.
     *
     * @param id - courier Id.
     */
    void deleteCourier(Long id);

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
     * Method checks if passed TariffsInfo exists in database.
     *
     * @param addNewTariffDto {@link AddNewTariffDto}
     */
    boolean checkIfTariffExists(AddNewTariffDto addNewTariffDto);

    /**
     * Method for edit info about tariff.
     *
     * @param tariffId - id of tariff
     * @param dto      {@link EditAmountOfBagDto}
     */
    void setTariffLimitByAmountOfBags(Long tariffId, EditAmountOfBagDto dto);

    /**
     * Method for edit info about tariff.
     *
     * @param tariffId - id of tariff
     * @param dto      {@link EditPriceOfOrder}
     */
    void setTariffLimitBySumOfOrder(Long tariffId, EditPriceOfOrder dto);

    /**
     * Method for deactivation or deleting Tariff depends on orders were made by
     * this tariff.
     *
     * @param tariffId - id of tariff
     *
     */
    void deactivateTariffCard(Long tariffId);

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
     * Method that deactivate tariffs for chosen parameters.
     *
     * @param detailsOfDeactivateTariffsDto - contains list of regionsId, list of
     *                                      citiesId, list of stationsId and
     *                                      courierId.
     */
    void deactivateTariffForChosenParam(DetailsOfDeactivateTariffsDto detailsOfDeactivateTariffsDto);
}
