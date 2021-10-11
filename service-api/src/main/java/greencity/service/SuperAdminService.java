package greencity.service;

import greencity.dto.*;
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
     * @return {@link GetLocationDto}
     * @author Vadym Makitra
     */
    List<GetLocationDto> getAllLocation();

    /**
     * Method for adding location.
     * 
     * @param dto {@link AddLocationDto}
     * @return {@link GetLocationDto}
     * @author Vadym Makitra
     */
    GetLocationDto addLocation(AddLocationDto dto);

    /**
     * Method for deactivate location.
     *
     * @param id - if of location
     * @return {@link GetLocationDto}
     */
    GetLocationDto deactivateLocation(Long id);

    /**
     * Method for activate location.
     *
     * @param id - id of Location
     * @return {@link GetLocationDto}
     */
    GetLocationDto activateLocation(Long id);
}