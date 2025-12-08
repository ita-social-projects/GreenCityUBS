package greencity.service.ubs.tariff;

import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.tariff.GetActiveTariffInfoDto;
import java.util.List;

public interface TariffService {
    /**
     * Method for getting info about tariff by tariff ID.
     *
     * @param tariffId The ID of the tariff.
     * @return {@link TariffInfoByLocationDto}
     * @author Anton Bondar
     */
    TariffInfoByLocationDto getTariffInfo(Long tariffId);

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

    /**
     * Retrieves the list of active tariffs.
     *
     * @return The list of active tariffs.
     */
    List<GetActiveTariffInfoDto> getTariffsInfo(Long courierId);
}
