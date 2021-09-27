package greencity.service;

import greencity.dto.AddServiceDto;
import greencity.entity.order.Bag;

public interface SuperAdminService {
    /**
     * Methods that add new Service.
     * 
     * @param dto  {@link AddServiceDto}
     * @param uuid {@link String} - uuid current user.
     * @return {@link Bag}
     * @author Vadym Makitra
     */
    Bag addService(AddServiceDto dto, String uuid);
}
