package greencity.service.ubs;

import greencity.dto.CoordinatesDto;
import greencity.dto.GroupedOrderDto;

import java.util.List;
import java.util.Set;

public interface CoordinateService {
    /**
     * Method returns all undelivered orders including litres.
     *
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getAllUndeliveredOrdersWithLiters();

    /**
     * Method to group orders into clusters including summary litres.
     *
     * @param distance - preferred distance for clusterization.
     * @param litres   - preferred amount of litres.
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getClusteredCoords(double distance, int litres);

    /**
     * Method to group orders into clusters including summary litres and specified
     * coordinates.
     *
     * @param specified          - list of {@link CoordinatesDto}.
     * @param litres             - preferred amount of litres.
     * @param additionalDistance - additional km to radius.
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getClusteredCoordsAlongWithSpecified(Set<CoordinatesDto> specified,
        int litres, double additionalDistance);
}
