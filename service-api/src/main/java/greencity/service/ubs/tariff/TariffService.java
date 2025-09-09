package greencity.service.ubs.tariff;

import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import java.util.List;

//TODO add test
public interface TariffService {
    /**
     * Method for getting info about tariff by courier ID and location ID.
     *
     * @param courierId  - id of courier
     * @param locationId - id of location
     * @return {@link TariffInfoByLocationDto}
     * @author Anton Bondar
     */
    TariffInfoByLocationDto getTariffInfoForLocation(Long courierId, Long locationId);

    /**
     * Method for getting info about tariff by order's id.
     *
     * @param id - id of order
     * @return {@link TariffsForLocationDto}
     */
    TariffsForLocationDto getTariffForOrder(Long id);

    /**
     * Checks if a tariff exists by its ID.
     *
     * @param tariffInfoId The ID of the tariff to check.
     * @return {@code true} if the tariff exists, {@code false} otherwise.
     */
    boolean checkIfTariffExistsById(Long tariffInfoId);

    /**
     * Retrieves the tariff ID associated with the specified location ID.
     *
     * @param locationId The ID of the location for which to retrieve the tariff ID.
     * @return The tariff ID associated with the specified location ID.
     */
    List<Long> getTariffIdByLocationId(Long locationId);
}
