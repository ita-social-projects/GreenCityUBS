package greencity.service.ubs;

import greencity.dto.CoordinatesDto;
import greencity.dto.GroupedCoordinatesDto;
import java.util.Set;

public interface UBSManagementService {
    /**
     * Method to group coordinates into clusters including summary litres and
     * specified coordinates.
     *
     * @param specified          - list of {@link CoordinatesDto}.
     * @param litres             - preferred amount of litres.
     * @param additionalDistance - additional km to radius.
     * @return List of {@link CoordinatesDto} lists.
     * @author Oleh Bilonizhka
     */
    Set<GroupedCoordinatesDto> getClusteredCoordsAlongWithSpecified(Set<CoordinatesDto> specified,
                                                                    int litres, double additionalDistance);

    /**
     * Method to group coordinates into clusters including summary litres.
     *
     * @param distance - preferred distance for clusterization.
     * @param litres   - preferred amount of litres.
     * @return List of {@link CoordinatesDto} lists.
     * @author Oleh Bilonizhka
     */
    Set<GroupedCoordinatesDto> getClusteredCoords(double distance, int litres);

    /**
     * Method returns all undelivered orders including litres.
     *
     * @return List of {@link CoordinatesDto} lists.
     * @author Oleh Bilonizhka
     */
    Set<GroupedCoordinatesDto> getAllUndeliveredCoords();
}
