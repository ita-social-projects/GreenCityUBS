package greencity.service;

import greencity.dto.AddServiceDto;
import greencity.dto.CreateServiceDto;
import greencity.dto.EditTariffServiceDto;
import greencity.dto.GetTariffServiceDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Service;

import java.util.List;

public interface SuperAdminService {
    /**
     * Methods that add new Service.
     * 
     * @param dto  {@link AddServiceDto}
     * @param uuid {@link String} - uuid current user.
     * @return {@link Bag}
     * @author Vadym Makitra
     */
    Bag addTariffService(AddServiceDto dto, String uuid);

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
    Service addService(CreateServiceDto dto, String uuid);
}
