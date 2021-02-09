package greencity.service;

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
import greencity.exceptions.InvalidDistanceException;
import greencity.repository.AddressRepository;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
        List<PersonalDataDto> personalDataDtoList =
            allByUserId.stream().map(u -> modelMapper.map(u, PersonalDataDto.class)).collect(Collectors.toList());

        return personalDataDtoList;
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
    public List<GroupedCoordinatesDto> clusterization(double distance) {
        if (distance <= 0 || distance > 20) {
            throw new InvalidDistanceException("The distance should be > 0 and <= 20");
        }
        List<Coordinates> allCoords = addressRepository.undeliveredOrdersCoords();
        if (allCoords.isEmpty()) {
            throw new ActiveOrdersNotFoundException("There are no any undelivered orders found.");
        }
        Random rand = new Random();
        List<GroupedCoordinatesDto> allClusters = new ArrayList<>();

        while (allCoords.size() > 0) {
            GroupedCoordinatesDto cluster = new GroupedCoordinatesDto();
            Coordinates currentlyCoord = allCoords.get(rand.nextInt(allCoords.size()));
            List<Coordinates> closeRelatives = getCoordinateCloseRelatives(distance,
                allCoords, currentlyCoord);
            Coordinates centerCoord = getNewCentralCoordinate(closeRelatives);

            while (!centerCoord.equals(currentlyCoord)) {
                currentlyCoord = centerCoord;
                closeRelatives = getCoordinateCloseRelatives(distance, allCoords, currentlyCoord);
                centerCoord = getNewCentralCoordinate(closeRelatives);
            }

            int amountOfLitresInCluster = 0;
            cluster.setGroupOfCoordinates(closeRelatives.stream().map(c -> CoordinatesDto.builder()
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .build()).collect(Collectors.toList()));
            for (CoordinatesDto temp : cluster.getGroupOfCoordinates().stream().collect(Collectors.toSet())) {
                amountOfLitresInCluster += addressRepository.capacity(temp.getLatitude(), temp.getLongitude());
            }
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
     * @return list of {@link Coordinates} - start coordinates with it's in distant
     *         relatives.
     * @author Oleh Bilonizhka
     */
    private List<Coordinates> getCoordinateCloseRelatives(double distance,
        List<Coordinates> allCoords, Coordinates currentlyCoord) {
        List<Coordinates> coordinateWithCloseRelativesList = new ArrayList<>();

        for (int i = 0; i < allCoords.size(); i++) {
            Coordinates checked = allCoords.get(i);

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
