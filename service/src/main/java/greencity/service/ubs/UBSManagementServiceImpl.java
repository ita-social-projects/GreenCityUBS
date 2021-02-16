package greencity.service.ubs;

import com.google.common.collect.TreeMultimap;
import greencity.dto.CoordinatesDto;
import greencity.dto.GroupedCoordinatesDto;
import greencity.entity.coords.Coordinates;
import greencity.exceptions.ActiveOrdersNotFoundException;
import greencity.exceptions.IncorrectValueException;
import greencity.repository.AddressRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UBSManagementServiceImpl implements UBSManagementService {
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<GroupedCoordinatesDto> getClusteredCoordsAlongWithSpecified(Set<CoordinatesDto> specified,
                                                                           int litres, double additionalDistance) {
        checkIfSpecifiedLitresAndDistancesAreValid(additionalDistance, litres);
        Set<Coordinates> allCoords = getAllUndeliveredOrdersCoords();
        Set<Coordinates> temporarySpecified = specified.stream()
            .map(c -> modelMapper.map(c, Coordinates.class)).collect(Collectors.toSet());
        for (Coordinates temp : temporarySpecified) {
            if (!allCoords.contains(temp)) {
                throw new IncorrectValueException("There are no order with coordinates: " + temp.getLatitude()
                    + ", " + temp.getLongitude());
            }
        }

        Coordinates centralCoord = getNewCentralCoordinate(temporarySpecified);
        int specifiedCoordsCapacity = 0;
        double newRadius = 0;
        for (Coordinates temp : temporarySpecified) {
            double distanceFromCentralCoord = distanceBetweenEarthCoordinates(temp.getLatitude(), temp.getLongitude(),
                centralCoord.getLatitude(), centralCoord.getLongitude());
            if (distanceFromCentralCoord > newRadius) {
                newRadius = distanceFromCentralCoord;
            }
            specifiedCoordsCapacity += addressRepository.capacity(temp.getLatitude(), temp.getLongitude());
        }
        newRadius += additionalDistance;

        List<Coordinates> coordinatesInsideRadiusWithoutSpecifiedCoords = new ArrayList<>();
        for (Coordinates temp : allCoords) {
            double distanceFromCentralCoord = distanceBetweenEarthCoordinates(temp.getLatitude(), temp.getLongitude(),
                centralCoord.getLatitude(), centralCoord.getLongitude());
            if (distanceFromCentralCoord < newRadius) {
                coordinatesInsideRadiusWithoutSpecifiedCoords.add(temp);
            }
        }
        coordinatesInsideRadiusWithoutSpecifiedCoords.removeAll(temporarySpecified);
        Collections.sort(coordinatesInsideRadiusWithoutSpecifiedCoords,
            getComparatorByDistanceFromCenter(centralCoord));

        int amountOfLitresToFill = litres - specifiedCoordsCapacity;
        double fill = 0;
        int allCoordsCapacity = specifiedCoordsCapacity;
        for (Coordinates temp : coordinatesInsideRadiusWithoutSpecifiedCoords) {
            int capacity = addressRepository.capacity(temp.getLatitude(), temp.getLongitude());
            if (fill < amountOfLitresToFill) {
                if ((fill + capacity) <= amountOfLitresToFill) {
                    fill += capacity;
                    allCoordsCapacity += capacity;
                    specified.add(modelMapper.map(temp, CoordinatesDto.class));
                }
            } else {
                break;
            }
        }

        Set<GroupedCoordinatesDto> clusterWithSpecifiedCoords = new HashSet<>();
        clusterWithSpecifiedCoords.add(GroupedCoordinatesDto.builder()
            .groupOfCoordinates(specified)
            .amountOfLitres(allCoordsCapacity)
            .build());

        return clusterWithSpecifiedCoords;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<GroupedCoordinatesDto> getClusteredCoords(double distance, int litres) {
        checkIfSpecifiedLitresAndDistancesAreValid(distance, litres);
        Set<Coordinates> allCoords = getAllUndeliveredOrdersCoords();
        Set<GroupedCoordinatesDto> allClusters = new HashSet<>();

        while (allCoords.size() > 0) {
            Coordinates currentlyCoord = allCoords.stream().findAny().get();

            Set<Coordinates> closeRelatives = getCoordinateCloseRelatives(distance,
                allCoords, currentlyCoord);
            Coordinates centralCoord = getNewCentralCoordinate(closeRelatives);

            while (!centralCoord.equals(currentlyCoord)) {
                currentlyCoord = centralCoord;
                closeRelatives = getCoordinateCloseRelatives(distance, allCoords, currentlyCoord);
                centralCoord = getNewCentralCoordinate(closeRelatives);
            }

            TreeMultimap<Integer, Coordinates> sortedByLitres = TreeMultimap.create(
                Integer::compareTo, getComparatorByDistanceFromCenter(centralCoord));
            int amountOfLitresInCluster = 0;
            for (Coordinates current : closeRelatives) {
                int currentCoordinatesCapacity =
                    addressRepository.capacity(current.getLatitude(), current.getLongitude());
                sortedByLitres.put(currentCoordinatesCapacity, current);
                amountOfLitresInCluster += currentCoordinatesCapacity;
            }
            while (amountOfLitresInCluster > litres) {
                Integer smallestAmountOfLitres = sortedByLitres.keySet().first();
                Coordinates theMostDistantOrder = sortedByLitres.get(sortedByLitres.keySet().first()).first();
                closeRelatives.remove(theMostDistantOrder);
                amountOfLitresInCluster -= smallestAmountOfLitres;
                sortedByLitres.remove(smallestAmountOfLitres, theMostDistantOrder);
            }

            GroupedCoordinatesDto cluster = new GroupedCoordinatesDto();
            cluster.setGroupOfCoordinates(closeRelatives.stream().map(c ->
                modelMapper.map(c, CoordinatesDto.class)).collect(Collectors.toSet()));
            cluster.setAmountOfLitres(amountOfLitresInCluster);
            allClusters.add(cluster);

            for (Coordinates checked : closeRelatives) {
                allCoords.remove(checked);
            }
        }

        return allClusters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<GroupedCoordinatesDto> getAllUndeliveredCoords() {
        Set<Coordinates> allCoords = getAllUndeliveredOrdersCoords();
        Set<GroupedCoordinatesDto> allOrdersWithLitres = new HashSet<>();

        for (Coordinates current : allCoords) {
            int currentCoordinatesCapacity =
                addressRepository.capacity(current.getLatitude(), current.getLongitude());
            allOrdersWithLitres.add(GroupedCoordinatesDto.builder()
                .amountOfLitres(currentCoordinatesCapacity)
                .groupOfCoordinates(Collections.singleton(
                    modelMapper.map(current, CoordinatesDto.class)))
                .build());
        }

        return allOrdersWithLitres;
    }

    /**
     * Method checks if entered parameters are valid.
     *
     * @param distance - preferred amount of litres.
     * @param litres   - preferred search radius.
     */
    private void checkIfSpecifiedLitresAndDistancesAreValid(double distance, int litres) {
        if (distance < 0 || distance >= 20) {
            throw new IncorrectValueException("The distance should be between 0 and 20 km.");
        }
        if (litres < 0 || litres > 10000) {
            throw new IncorrectValueException("The amount of litres should be between 0 and 10.000 litres.");
        }
    }

    /**
     * Method finds coordinates of undelivered orders.
     *
     * @return
     */
    private Set<Coordinates> getAllUndeliveredOrdersCoords() {
        Set<Coordinates> allCoords = addressRepository.undeliveredOrdersCoords();
        if (allCoords.isEmpty()) {
            throw new ActiveOrdersNotFoundException("There are no any undelivered orders found.");
        }
        return allCoords;
    }

    /**
     * Method returns coordinates comparator by theirs distance from center of
     * cluster.
     *
     * @param centralCoord {@link Integer}.
     * @return {@link Comparator} of Coordinates.
     * @author Oleh Bilonizhka
     */
    private Comparator<Coordinates> getComparatorByDistanceFromCenter(Coordinates centralCoord) {
        return (o1, o2) -> {
            Double o1Int = distanceBetweenEarthCoordinates(o1.getLatitude(), o1.getLongitude(),
                centralCoord.getLatitude(), centralCoord.getLongitude()) * 1000;

            Double o2Int = distanceBetweenEarthCoordinates(o2.getLatitude(), o2.getLongitude(),
                centralCoord.getLatitude(), centralCoord.getLongitude()) * 1000;

            return o2Int.compareTo(o1Int);
        };
    }

    /**
     * Method defines and returns all coordinates in certain radius.
     *
     * @param distance       - preferred distance for clusterization.
     * @param allCoords      - list of {@link Coordinates} which shows all
     *                       unclustered coordinates.
     * @param currentlyCoord - {@link Coordinates} - chosen start coordinates.
     * @return list of {@link Coordinates} - start coordinates with it's in distant relatives.
     * @author Oleh Bilonizhka
     */
    private Set<Coordinates> getCoordinateCloseRelatives(double distance,
                                                         Set<Coordinates> allCoords, Coordinates currentlyCoord) {
        Set<Coordinates> coordinateWithCloseRelativesList = new HashSet<>();

        for (Coordinates checked : allCoords) {
            if (distanceBetweenEarthCoordinates(currentlyCoord.getLatitude(), currentlyCoord.getLongitude(),
                checked.getLatitude(), checked.getLongitude()) <= distance) {
                coordinateWithCloseRelativesList.add(checked);
            }
        }

        return coordinateWithCloseRelativesList;
    }

    /**
     * Method defines new central coordinate for existing ones.
     *
     * @param coordinateWithCloseRelatives list of {@link Coordinates}.
     * @return {@link Coordinates} new central coordinate.
     * @author Oleh Bilonizhka
     */
    private Coordinates getNewCentralCoordinate(Set<Coordinates> coordinateWithCloseRelatives) {
        double sumLat = 0;
        double sumLon = 0;
        int amountOfCoords = coordinateWithCloseRelatives.size();

        for (Coordinates checked : coordinateWithCloseRelatives) {
            sumLat += checked.getLatitude();
            sumLon += checked.getLongitude();
        }

        return new Coordinates(sumLat / amountOfCoords, sumLon / amountOfCoords);
    }

    /**
     * Method to convert degrees to radians.
     *
     * @param degrees {@link Double} degrees.
     * @return {@link Double} radians.
     */
    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    /**
     * Method to determine distance between 2 earth coordinates.
     *
     * @param lat1 {@link Double} - latitude of 1 coordinate.
     * @param lon1 {@link Double} - longitude of 1 coordinate.
     * @param lat2 {@link Double} - latitude of 2 coordinate.
     * @param lon2 {@link Double} - longitude of 2 coordinate.
     * @return {@link Integer} distance in meters.
     */
    private double distanceBetweenEarthCoordinates(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371;

        double radiansLatitude = degreesToRadians(lat2 - lat1);
        double radiansLongitude = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(radiansLatitude / 2) * Math.sin(radiansLatitude / 2)
            + Math.sin(radiansLongitude / 2) * Math.sin(radiansLongitude / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
