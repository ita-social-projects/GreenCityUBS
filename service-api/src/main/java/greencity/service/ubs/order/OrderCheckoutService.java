package greencity.service.ubs.order;

import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.entity.user.User;

public interface OrderCheckoutService {
    /**
     * Method returns all bags available for order.
     *
     * @param tariffId   {@link Long} tariff id.
     * @param locationId {@link Long} location id.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Safarov Renat
     */
    UserPointsAndAllBagsDto getFirstPageDataByTariffAndLocationId(Long tariffId, Long locationId);

    /**
     * Methods returns all available for order bags and current user's bonus points.
     *
     * @param uuid    current {@link User}'s uuid.
     * @param orderId {@link Long} id of existing order.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Safarov Renat
     */
    UserPointsAndAllBagsDto getFirstPageDataByOrderId(String uuid, Long orderId);

    /**
     * Methods returns all saved user data.
     *
     * @param uuid current {@link User}'s uuid.
     * @return instance of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    PersonalDataDto getSecondPageData(String uuid);
}
