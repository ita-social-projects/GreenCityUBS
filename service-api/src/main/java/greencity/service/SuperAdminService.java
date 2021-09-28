package greencity.service;

import greencity.dto.AddServiceDto;
import greencity.dto.GetTariffServiceDto;
import greencity.entity.order.Bag;

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
    void deleteTariffService(long id);
}
