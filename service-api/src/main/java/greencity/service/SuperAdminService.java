package greencity.service;

import greencity.dto.*;
import greencity.entity.order.Courier;
import greencity.entity.order.Service;

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
    GetTariffServiceDto addTariffService(AddServiceDto dto, String uuid);

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
    GetServiceDto addService(CreateServiceDto dto, String uuid);

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
    GetServiceDto editService(long id, CreateServiceDto dto, String uuid);

    /**
     * Method for get all info about location.
     *
     * @return {@link GetLocationTranslationDto}
     * @author Vadym Makitra
     */
    List<GetLocationTranslationDto> getAllLocation();

    /**
     * Method for adding location.
     *
     * @param dto {@link AddLocationDto}
     * @return {@link GetLocationTranslationDto}
     * @author Vadym Makitra
     */
    GetLocationTranslationDto addLocation(AddLocationDto dto);

    /**
     * Method for deactivate location.
     *
     * @param id - if of location
     * @return {@link GetLocationTranslationDto}
     */
    GetLocationTranslationDto deactivateLocation(Long id, String code);

    /**
     * Method for activate location.
     *
     * @param id - id of Location
     * @return {@link GetLocationTranslationDto}
     */
    GetLocationTranslationDto activateLocation(Long id, String code);

    /**
     * Method for creating courier.
     *
     * @param dto {@link CreateCourierDto} - parameters that's user entered.
     * @return {@link Courier}
     * @author Vadym Makitra
     */
    GetCourierTranslationsDto createCourier(CreateCourierDto dto);

    /**
     * Method for getting all info about couriers.
     * 
     * @return {@link GetCourierTranslationsDto}
     */
    List<GetCourierTranslationsDto> getAllCouriers();

    /**
     * Method for set courier limit by sum of order.
     *
     * @param id  - id of courier
     * @param dto {@link EditPriceOfOrder}
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    GetCourierTranslationsDto setCourierLimitBySumOfOrder(Long id, EditPriceOfOrder dto);

    /**
     * Method for set courier limit by amount of bag.
     *
     * @param id  - id of courier.
     * @param dto {@link EditAmountOfBagDto}
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    GetCourierTranslationsDto setCourierLimitByAmountOfBag(Long id, EditAmountOfBagDto dto);

    /**
     * Method for edit limit description.
     *
     * @param id               - id of courier
     * @param limitDescription - new limit description.
     * @return {@link GetCourierTranslationsDto}
     * @author Vadym Makitra
     */
    GetCourierTranslationsDto setLimitDescription(Long id, String limitDescription);

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
}