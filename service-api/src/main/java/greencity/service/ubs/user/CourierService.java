package greencity.service.ubs.user;

import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.courier.CourierDto;
import java.util.List;
import java.util.Optional;

public interface CourierService {
    /**
     * Method for getting info about all active locations by courier ID or if user
     * has made an order before to get info about tariff.
     *
     * @param uuid      - user's uuid
     * @param changeLoc - optional param. If it's present provide info about
     *                  locations
     * @param courierId - id of courier
     * @return {@link OrderCourierPopUpDto}
     * @author Anton Bondar
     */
    OrderCourierPopUpDto getInfoForCourierOrderingByCourierId(String uuid, Optional<String> changeLoc, Long courierId);

    /**
     * Method for getting all active couriers.
     *
     * @return list of {@link CourierDto}
     * @author Anton Bondar
     */
    List<CourierDto> getAllActiveCouriers();
}
