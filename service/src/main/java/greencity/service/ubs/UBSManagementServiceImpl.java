package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.*;
import greencity.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static greencity.constant.ErrorMessage.*;

@Service
@AllArgsConstructor
public class UBSManagementServiceImpl implements UBSManagementService {
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final RestClient restClient;
    private final UserRepository userRepository;
    private final HttpServletRequest httpServletRequest;
    private final AllValuesFromTableRepo allValuesFromTableRepo;
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupedOrderDto> getAllUndeliveredOrdersWithLiters() {
        Set<Coordinates> allCoords = addressRepository.undeliveredOrdersCoords();
        List<Order> allOrders = getAllUndeliveredOrders();
        List<GroupedOrderDto> allOrdersWithLitres = new ArrayList<>();
        for (Coordinates temp : allCoords) {
            int currentCoordinatesCapacity =
                addressRepository.capacity(temp.getLatitude(), temp.getLongitude());
            List<Order> currentCoordinatesOrders = allOrders.stream().filter(
                o -> o.getUbsUser().getAddress().getCoordinates().equals(temp)).collect(Collectors.toList());
            List<OrderDto> currentCoordinatesOrdersDto = currentCoordinatesOrders.stream()
                .map(o -> modelMapper.map(o, OrderDto.class)).collect(Collectors.toList());
            allOrdersWithLitres.add(GroupedOrderDto.builder()
                .amountOfLitres(currentCoordinatesCapacity)
                .groupOfOrders(currentCoordinatesOrdersDto)
                .build());
        }
        return allOrdersWithLitres;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupedOrderDto> getClusteredCoords(double distance, int litres) {
        checkIfSpecifiedLitresAndDistancesAreValid(distance, litres);
        Set<Coordinates> allCoords = addressRepository.undeliveredOrdersCoordsWithCapacityLimit(litres);
        List<GroupedOrderDto> allClusters = new ArrayList<>();

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

            int amountOfLitresInCluster = 0;
            for (Coordinates current : closeRelatives) {
                int currentCoordinatesCapacity =
                    addressRepository.capacity(current.getLatitude(), current.getLongitude());
                amountOfLitresInCluster += currentCoordinatesCapacity;
            }

            if (amountOfLitresInCluster > litres) {
                List<Coordinates> closeRelativesSorted = new ArrayList<>(closeRelatives);
                Collections.sort(closeRelativesSorted, getComparatorByDistanceFromCenter(centralCoord));
                int indexOfCoordToBeDeleted = -1;
                while (amountOfLitresInCluster > litres) {
                    Coordinates coordToBeDeleted = closeRelativesSorted.get(++indexOfCoordToBeDeleted);
                    int anountOfLitresInCurrentOrder = addressRepository
                        .capacity(coordToBeDeleted.getLatitude(), coordToBeDeleted.getLongitude());
                    amountOfLitresInCluster -= anountOfLitresInCurrentOrder;
                    closeRelatives.remove(coordToBeDeleted);
                }
            }

            for (Coordinates grouped : closeRelatives) {
                allCoords.remove(grouped);
            }

            // mapping coordinates to orderDto
            getUndeliveredOrdersByGroupedCoordinates(closeRelatives,
                amountOfLitresInCluster, allClusters);
        }

        return allClusters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupedOrderDto> getClusteredCoordsAlongWithSpecified(Set<CoordinatesDto> specified,
        int litres, double additionalDistance) {
        checkIfSpecifiedLitresAndDistancesAreValid(additionalDistance, litres);

        Set<Coordinates> allCoords = addressRepository.undeliveredOrdersCoords();
        Set<Coordinates> result = specified.stream()
            .map(c -> modelMapper.map(c, Coordinates.class)).collect(Collectors.toSet());
        for (Coordinates temp : result) {
            if (!allCoords.contains(temp)) {
                throw new IncorrectValueException(NO_SUCH_COORDINATES + temp.getLatitude()
                    + ", " + temp.getLongitude());
            }
        }

        Coordinates centralCoord = getNewCentralCoordinate(result);
        int specifiedCoordsCapacity = 0;
        double newRadius = 0;
        for (Coordinates temp : result) {
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
        coordinatesInsideRadiusWithoutSpecifiedCoords.removeAll(result);

        Collections.sort(coordinatesInsideRadiusWithoutSpecifiedCoords,
            getComparatorByDistanceFromCenter(centralCoord));
        int amountOfLitresToFill = litres - specifiedCoordsCapacity;
        double fill = 0;
        int allCoordsCapacity = specifiedCoordsCapacity;
        for (int i = coordinatesInsideRadiusWithoutSpecifiedCoords.size() - 1; i > -1; i--) {
            Coordinates temp = coordinatesInsideRadiusWithoutSpecifiedCoords.get(i);
            int capacity = addressRepository.capacity(temp.getLatitude(), temp.getLongitude());
            if (fill < amountOfLitresToFill) {
                if ((fill + capacity) <= amountOfLitresToFill) {
                    fill += capacity;
                    allCoordsCapacity += capacity;
                    result.add(temp);
                }
            } else {
                break;
            }
        }

        // mapping coordinates to orderDto
        List<GroupedOrderDto> groupedOrderDtos = new ArrayList<>();
        getUndeliveredOrdersByGroupedCoordinates(result,
            allCoordsCapacity, groupedOrderDtos);

        return groupedOrderDtos;
    }

    /**
     * Method checks if entered parameters are valid.
     *
     * @param distance - preferred amount of litres.
     * @param litres   - preferred search radius.
     */
    private void checkIfSpecifiedLitresAndDistancesAreValid(double distance, int litres) {
        if (distance < 0 || distance > 20) {
            throw new IncorrectValueException(INAVALID_DISTANCE_AMOUNT);
        }
        if (litres < 0 || litres > 10000) {
            throw new IncorrectValueException(INAVALID_LITRES_AMOUNT);
        }
    }

    /**
     * Method finds undelivered orders.
     *
     * @return List of {@link Order}
     */
    private List<Order> getAllUndeliveredOrders() {
        List<Order> allCoords = orderRepository.undeliveredAddresses();
        if (allCoords.isEmpty()) {
            throw new ActiveOrdersNotFoundException(UNDELIVERED_ORDERS_NOT_FOUND);
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
     * @return list of {@link Coordinates} - start coordinates with it's
     *         distant @relatives.
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

    private void getUndeliveredOrdersByGroupedCoordinates(Set<Coordinates> closeRelatives, int amountOfLitresInCluster,
        List<GroupedOrderDto> allClusters) {
        List<Order> orderslist = new ArrayList<>();
        for (Coordinates coordinates : closeRelatives) {
            List<Order> orders =
                orderRepository.undeliveredOrdersGroupThem(coordinates.getLatitude(), coordinates.getLongitude());
            orderslist.addAll(orders);
        }
        GroupedOrderDto cluster = new GroupedOrderDto();
        cluster.setGroupOfOrders(
            orderslist.stream().map(order -> modelMapper.map(order, OrderDto.class)).collect(Collectors.toList()));
        cluster.setAmountOfLitres(amountOfLitresInCluster);
        allClusters.add(cluster);
    }

    @Override
    public PageableDto<CertificateDtoForSearching> getAllCertificates(Pageable page) {
        Page<Certificate> certificates = certificateRepository.getAll(page);
        return getAllCertificatesTranslationDto(certificates);
    }

    @Override
    public void addCertificate(CertificateDtoForAdding add) {
        Certificate certificate = modelMapper.map(add, Certificate.class);
        certificateRepository.save(certificate);
    }

    @Override
    public ViolationsInfoDto getAllUserViolations(String email) {
        String uuidId = restClient.findUuidByEmail(email);
        User user = userRepository.findUserByUuid(uuidId).orElseThrow(() -> new UnexistingUuidExeption(
            USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        return modelMapper.map(user, ViolationsInfoDto.class);
    }

    @Override
    public void addUserViolation(AddingViolationsToUserDto add) {
        Order order = orderRepository.findById(add.getOrderID()).orElseThrow(() -> new UnexistingOrderException(
            ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        User ourUser = order.getUser();
        ourUser.getViolationsDescription().put(order.getId(), add.getViolationDescription());
        ourUser.setViolations(ourUser.getViolations() + 1);
        userRepository.save(ourUser);
    }

    private PageableDto<CertificateDtoForSearching> getAllCertificatesTranslationDto(Page<Certificate> pages) {
        List<CertificateDtoForSearching> certificateForSearchingDTOS = pages
            .stream()
            .map(
                certificatesTranslations -> modelMapper.map(certificatesTranslations, CertificateDtoForSearching.class))
            .collect(Collectors.toList());
        return new PageableDto<>(
            certificateForSearchingDTOS,
            pages.getTotalElements(),
            pages.getPageable().getPageNumber(),
            pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPointsToUser(AddingPointsToUserDto addingPointsToUserDto) {
        String ourUUid = restClient.findUuidByEmail(addingPointsToUserDto.getEmail());
        User ourUser = userRepository.findUserByUuid(ourUUid).orElseThrow(() -> new UnexistingUuidExeption(
            USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        ourUser.setCurrentPoints(ourUser.getCurrentPoints() + addingPointsToUserDto.getAdditionalPoints());
        ChangeOfPoints changeOfPoints = ChangeOfPoints.builder()
            .amount(addingPointsToUserDto.getAdditionalPoints())
            .date(LocalDateTime.now())
            .user(ourUser)
            .build();
        ourUser.getChangeOfPointsList().add(changeOfPoints);
        userRepository.save(ourUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNotificationAboutViolation(AddingViolationsToUserDto dto, String language) {
        Order order = orderRepository.findById(dto.getOrderID()).orElse(null);
        UserViolationMailDto mailDto;
        if (order != null) {
            mailDto = UserViolationMailDto.builder()
                .name(order.getUser().getRecipientName())
                .email(order.getUser().getRecipientEmail())
                .violationDescription(dto.getViolationDescription())
                .language(language)
                .build();
            restClient.sendViolationOnMail(mailDto);
        }
    }

    @Override
    public List<AllFieldsFromTableDto> getAllValuesFromTable() {
        List<AllFieldsFromTableDto> ourDtos = new ArrayList<>();
        List<Map<String, Object>> ourResult = allValuesFromTableRepo.findAll();
        for (Map<String, Object> map : ourResult) {
            AllFieldsFromTableDto allFieldsFromTableDto = objectMapper.convertValue(map, AllFieldsFromTableDto.class);
            if (allFieldsFromTableDto.getDateOfExport() == null || allFieldsFromTableDto.getTimeOfExport() == null) {
                allFieldsFromTableDto.setDateOfExport(LocalDate.now().toString());
                allFieldsFromTableDto.setTimeOfExport(LocalTime.now().toString());
            }
            List<Map<String, Object>> employees = allValuesFromTableRepo
                .findAllEmpl(allFieldsFromTableDto.getOrderId());
            for (Map<String, Object> objectMap : employees) {
                Long positionId = (Long) objectMap.get("position_id");
                if (positionId == 1) {
                    allFieldsFromTableDto.setResponsibleManager((String) objectMap.get("name"));
                } else if (positionId == 2) {
                    allFieldsFromTableDto.setResponsibleLogicMan((String) objectMap.get("name"));
                } else if (positionId == 3) {
                    allFieldsFromTableDto.setResponsibleDriver((String) objectMap.get("name"));
                } else if (positionId == 4) {
                    allFieldsFromTableDto.setResponsibleNavigator((String) objectMap.get("name"));
                }
            }
            ourDtos.add(allFieldsFromTableDto);
        }
        return ourDtos;
    }

    @Override
    public List<AllFieldsFromTableDto> getAllSortedValuesFromTable(String column, String sortingType) {
        List<AllFieldsFromTableDto> ourDtos = new ArrayList<>();
        List<Map<String, Object>> ourResult = allValuesFromTableRepo.findAllWithSorting(column, sortingType);
        for (Map<String, Object> map : ourResult) {
            AllFieldsFromTableDto allFieldsFromTableDto = objectMapper.convertValue(map, AllFieldsFromTableDto.class);
            if (allFieldsFromTableDto.getDateOfExport() == null || allFieldsFromTableDto.getTimeOfExport() == null) {
                allFieldsFromTableDto.setDateOfExport(LocalDate.now().toString());
                allFieldsFromTableDto.setTimeOfExport(LocalTime.now().toString());
            }
            List<Map<String, Object>> employees = allValuesFromTableRepo
                .findAllEmpl(allFieldsFromTableDto.getOrderId());
            for (Map<String, Object> objectMap : employees) {
                Long positionId = (Long) objectMap.get("position_id");
                if (positionId == 1) {
                    allFieldsFromTableDto.setResponsibleManager((String) objectMap.get("name"));
                } else if (positionId == 2) {
                    allFieldsFromTableDto.setResponsibleLogicMan((String) objectMap.get("name"));
                } else if (positionId == 3) {
                    allFieldsFromTableDto.setResponsibleDriver((String) objectMap.get("name"));
                } else if (positionId == 4) {
                    allFieldsFromTableDto.setResponsibleNavigator((String) objectMap.get("name"));
                }
            }
            ourDtos.add(allFieldsFromTableDto);
        }
        return ourDtos;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public ReadAddressByOrderDto getAddressByOrderId(Long orderId) {
        orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundOrderAddressException(ErrorMessage.NOT_FOUND_ADDRESS_BY_ORDER_ID + orderId));
        return modelMapper.map(addressRepository.getAddressByOrderId(orderId), ReadAddressByOrderDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderAddressDtoResponse updateAddress(OrderAddressDtoUpdate dtoUpdate) {
        Address address = orderRepository.findById(dtoUpdate.getId())
            .orElseThrow(() -> new NotFoundOrderAddressException(NOT_FOUND_ADDRESS_BY_ORDER_ID + dtoUpdate.getId()))
            .getUbsUser().getAddress();
        addressRepository.save(updateAddressOrderInfo(address, dtoUpdate));
        return modelMapper.map(addressRepository.findById(address.getId()).get(), OrderAddressDtoResponse.class);
    }

    private Address updateAddressOrderInfo(Address address, OrderAddressDtoUpdate dto) {
        address.setHouseNumber(dto.getHouseNumber());
        address.setEntranceNumber(dto.getEntranceNumber());
        address.setDistrict(dto.getDistrict());
        address.setStreet(dto.getStreet());
        address.setHouseCorpus(dto.getHouseCorpus());
        return address;
    }
}
