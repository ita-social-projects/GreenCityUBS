package greencity.service;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import greencity.dto.CertificateDto;
import greencity.dto.CoordinatesDto;
import greencity.dto.GroupedCoordinatesDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UserPointsAndAllBagsDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.ActiveOrdersNotFoundException;
import greencity.exceptions.CertificateNotFoundException;
import greencity.exceptions.IncorrectValueException;
import greencity.repository.AddressRepository;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.google.common.collect.TreeMultimap;


/**
 * Implementation of {@link UBSService}.
 */
@Service
@AllArgsConstructor
public class UBSServiceImpl implements UBSService {
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSuserRepository ubsUserRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final AddressRepository addressRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPointsAndAllBagsDto getFirstPageData(Long userId) {
        return new UserPointsAndAllBagsDto((List<Bag>) bagRepository.findAll(),
            userRepository.findById(userId).get().getCurrentPoints());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<PersonalDataDto> getSecondPageData(Long userId) {
        List<UBSuser> allByUserId = ubsUserRepository.getAllByUserId(userId);
        if (allByUserId.isEmpty()) {
            return List.of(PersonalDataDto.builder().email(userRepository.findById(userId).get().getEmail()).build());
        }

        return allByUserId.stream().map(u -> modelMapper.map(u, PersonalDataDto.class)).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CertificateDto checkCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code).orElse(null);

        if (certificate == null) {
            throw new CertificateNotFoundException("There is no such а certificate.");
        } else {
            return new CertificateDto(certificate.getCertificateStatus().toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveFullOrderToDB(OrderResponseDto dto, Long userId) {
        User currentUser = userRepository.findById(userId).get();

        UBSuser ubsUser = modelMapper.map(dto.getPersonalData(), UBSuser.class);
        ubsUser.setUser(currentUser);
        System.out.println(ubsUser);
        System.out.println(ubsUserRepository.findById(ubsUser.getId()).get());

        if (ubsUser.getId() == null || !ubsUser.equals(ubsUserRepository.findById(ubsUser.getId()).get())) {
            ubsUser.setId(null);
            ubsUserRepository.save(ubsUser);
            currentUser.getUbsUsers().add(ubsUser);
        } else {
            ubsUser = ubsUserRepository.findById(ubsUser.getId()).get();
        }

        Order order = modelMapper.map(dto, Order.class);
        order.setOrderStatus(OrderStatus.NEW);
        order.setUbsUser(ubsUser);
        order.setUser(currentUser);

        currentUser.getOrders().add(order);
        currentUser.setCurrentPoints(currentUser.getCurrentPoints() - dto.getPointsToUse());
        currentUser.getChangeOfPoints().put(LocalDateTime.now(), -dto.getPointsToUse());

        Certificate certificate = order.getCertificate();
        if (certificate != null) {
            certificate.setCertificateStatus(CertificateStatus.USED);
        }

        userRepository.save(currentUser);
    }

    /**
     * {@inheritDoc}
     */
    public Set<GroupedCoordinatesDto> clusterization(double distance, int litres) {
        if (distance <= 0 || distance >= 20) {
            throw new IncorrectValueException("The distance should be between 0 and 20 km.");
        }
        if (litres < 3000 || litres > 10000) {
            throw new IncorrectValueException("The amount of litres should be between 3.000 and 10.000 litres.");
        }

        Set<Coordinates> allCoords = addressRepository.undeliveredOrdersCoords();
        if (allCoords.isEmpty()) {
            throw new ActiveOrdersNotFoundException("There are no any undelivered orders found.");
        }

        Set<GroupedCoordinatesDto> allClusters = new HashSet<>();

        while (allCoords.size() > 0) {
            Coordinates currentlyCoord = allCoords.stream().findAny().get();

            List<Coordinates> closeRelatives = getCoordinateCloseRelatives(distance,
                allCoords, currentlyCoord);
            Coordinates centerCoord = getNewCentralCoordinate(closeRelatives);


            while (!centerCoord.equals(currentlyCoord)) {
                currentlyCoord = centerCoord;
                closeRelatives = getCoordinateCloseRelatives(distance, allCoords, currentlyCoord);
                centerCoord = getNewCentralCoordinate(closeRelatives);
            }

            int amountOfLitresInCluster = 0;

            Coordinates finalCenterCoord = centerCoord;
            TreeMultimap<Integer, Coordinates> sortedByLitres = TreeMultimap.create(
                Integer::compareTo, (o1, o2) -> {
                    Double o1Int =  distanceBetweenEarthCoordinates(o1.getLatitude(), o1.getLongitude(),
                        finalCenterCoord.getLatitude(), finalCenterCoord.getLongitude()) * 1000;

                    Double o2Int =  distanceBetweenEarthCoordinates(o2.getLatitude(), o2.getLongitude(),
                        finalCenterCoord.getLatitude(), finalCenterCoord.getLongitude()) * 1000;

                    return o2Int.compareTo(o1Int);
                }
            );

            for (Coordinates current : closeRelatives) {
                int currentCoordinatesCapacity
                    = addressRepository.capacity(current.getLatitude(), current.getLongitude());
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
            cluster.setGroupOfCoordinates(closeRelatives.stream().map(c -> CoordinatesDto.builder()
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .build()).collect(Collectors.toSet()));
            cluster.setAmountOfLitres(amountOfLitresInCluster);
            allClusters.add(cluster);

            for (Coordinates checked : closeRelatives) {
                allCoords.remove(checked);
            }
        }


        return allClusters;
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
    private List<Coordinates> getCoordinateCloseRelatives(double distance,
                                                          Set<Coordinates> allCoords, Coordinates currentlyCoord) {
        List<Coordinates> coordinateWithCloseRelativesList = new ArrayList<>();

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
     */
    private Coordinates getNewCentralCoordinate(List<Coordinates> coordinateWithCloseRelatives) {
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
