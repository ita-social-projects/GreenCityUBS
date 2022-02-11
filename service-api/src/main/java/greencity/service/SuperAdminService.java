package greencity.service;

import greencity.dto.*;
import greencity.entity.order.Courier;
import greencity.entity.order.Service;
import greencity.entity.order.TariffsInfo;

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
    CreateCourierDto createCourier(CreateCourierDto dto);

    /**
     * Method for getting all info about couriers.
     * 
     * @return {@link GetCourierTranslationsDto}
     */
    List<GetCourierLocationDto> getAllCouriers();

    /**
     * Method for set courier limit by sum of order.
     *
     * @param id  - id of courier
     * @param dto {@link EditPriceOfOrder}
     * 
     * @author Vadym Makitra
     */
    void setCourierLimitBySumOfOrder(Long id, EditPriceOfOrder dto);

    /**
     * Method for set courier limit by amount of bag.
     *
     * @param id  - id of courier.
     * @param dto {@link EditAmountOfBagDto}
     * @author Vadym Makitra
     */
    void setCourierLimitByAmountOfBag(Long id, EditAmountOfBagDto dto);

    /**
     * Method for edit limit description.
     *
     * @param courierId        - id of courier
     * @param limitDescription - new limit description.
     * @param languageId       - id of current language.
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    GetCourierTranslationsDto setLimitDescription(Long courierId, String limitDescription, Long languageId);

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
     * Method for edit info about tariff.
     * 
     * @param dto {@link EditTariffInfoDto}
     * @return {@link EditTariffInfoDto}
     */
    EditTariffInfoDto editInfoInTariff(EditTariffInfoDto dto);

    /**
     * Method for delete courier.
     * 
     * @param id - courier Id.
     */
    void deleteCourier(Long id);

    /**
     * Method for add new location to courier.
     *
     * @param dto {@link NewLocationForCourierDto}
     * @author Vadym Makitra
     */
    void addLocationToCourier(NewLocationForCourierDto dto);

    /**
     * Method for getting all info about tariffs.
     *
     * @return {@link GetTariffsInfoDto}
     */
    List<GetTariffsInfoDto> getAllTariffsInfo();
}