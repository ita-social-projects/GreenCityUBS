package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;

import static greencity.constant.ErrorMessage.*;

import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.PaymentStatus;
import greencity.entity.order.*;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.*;
import greencity.filters.SearchCriteria;
import greencity.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UBSManagementServiceImpl implements UBSManagementService {
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final RestClient restClient;
    private final UserRepository userRepository;
    private final AllValuesFromTableRepo allValuesFromTableRepo;
    private final ObjectMapper objectMapper;
    private final BagRepository bagRepository;
    private final BagTranslationRepository bagTranslationRepository;
    private final UpdateOrderDetail updateOrderRepository;
    private final BagsInfoRepo bagsInfoRepository;
    private final ViolationRepository violationRepository;
    private final PaymentRepository paymentRepository;

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
        List<GroupedOrderDto> groupedOrderDtos = new ArrayList<>();
        getUndeliveredOrdersByGroupedCoordinates(result,
            allCoordsCapacity, groupedOrderDtos);

        return groupedOrderDtos;
    }

    /**
     * Method gets all order payments and count paid amount and amount which user
     * should paid.
     *
     * @return {@link PaymentTableInfoDto }
     */
    @Override
    public PaymentTableInfoDto getPaymentInfo(long orderId) {
        PaymentTableInfoDto paymentTableInfoDto = new PaymentTableInfoDto();
        Long paidAmount = 0L;
        Long unPaidAmount = 0L;
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        for (Payment payment : order.getPayment()) {
            if (payment.getOrderStatus().equals("approved")) {
                paidAmount += payment.getAmount();
            } else {
                unPaidAmount += payment.getAmount();
            }
        }
        paymentTableInfoDto.setUnPaidAmount(unPaidAmount);
        paymentTableInfoDto.setPaidAmount(paidAmount);
        List<PaymentInfoDto> paymentInfoDtos = order.getPayment().stream()
            .map(x -> modelMapper.map(x, PaymentInfoDto.class)).collect(Collectors.toList());
        paymentTableInfoDto.setPaymentInfoDtos(paymentInfoDtos);
        return paymentTableInfoDto;
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
    public PageableDto<AllFieldsFromTableDto> getAllValuesFromTable(SearchCriteria searchCriteria, int pages,
        int size) {
        List<AllFieldsFromTableDto> ourDtos = new ArrayList<>();
        if (searchCriteria.getPayment() == null) {
            searchCriteria.setPayment("");
        }
        if (searchCriteria.getOrderStatus() == null) {
            searchCriteria.setOrderStatus("");
        }
        if (searchCriteria.getReceivingStation() == null) {
            searchCriteria.setReceivingStation("");
        }
        if (searchCriteria.getDistrict() == null) {
            searchCriteria.setDistrict("");
        }
        int elements;
        try {
            List<Map<String, Object>> ourResult = allValuesFromTableRepo.findAlL(searchCriteria, pages, size);
            elements = userRepository.orderCounter();
            for (Map<String, Object> map : ourResult) {
                AllFieldsFromTableDto allFieldsFromTableDto =
                    objectMapper.convertValue(map, AllFieldsFromTableDto.class);
                if (allFieldsFromTableDto.getDateOfExport() == null
                    || allFieldsFromTableDto.getTimeOfExport() == null) {
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
        } catch (NullPointerException nullPointerException) {
            throw new NullPointerException();
        }
        int totalPages = (elements / size);
        int totalPagesWithCheck = (elements % size) == 0 ? totalPages : totalPages + 1;

        return new PageableDto<>(
            ourDtos,
            size,
            pages,
            totalPagesWithCheck);
    }

    @Override
    public PageableDto<AllFieldsFromTableDto> getAllSortedValuesFromTable(String column, String sortingType, int pages,
        int size) {
        int numberOfElements1 = 0;
        List<AllFieldsFromTableDto> ourDtos = new ArrayList<>();
        try {
            List<Map<String, Object>> ourResult =
                allValuesFromTableRepo.findAllWithSorting(column, sortingType, pages, size);
            numberOfElements1 += userRepository.orderCounterForSorting();
            for (Map<String, Object> map : ourResult) {
                AllFieldsFromTableDto allFieldsFromTableDto =
                    objectMapper.convertValue(map, AllFieldsFromTableDto.class);
                if (allFieldsFromTableDto.getDateOfExport() == null
                    || allFieldsFromTableDto.getTimeOfExport() == null) {
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
        } catch (NullPointerException nullPointerException) {
            throw new NullPointerException();
        }
        int totalPages = (numberOfElements1 / size);
        int totalPagesLast = (numberOfElements1 % size) == 0 ? totalPages : totalPages + 1;

        return new PageableDto<>(
            ourDtos,
            size,
            pages,
            totalPagesLast);
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

    /**
     * {@inheritDoc}
     */

    @Override
    public List<OrderDetailInfoDto> getOrderDetails(Long orderId, String language) {
        OrderDetailDto dto = new OrderDetailDto();
        Order order = orderRepository.getOrderDetails(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        setOrderDetailDto(dto, order, orderId, language);
        return modelMapper.map(dto, new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<OrderDetailInfoDto> setOrderDetail(List<UpdateOrderDetailDto> request, String language) {
        OrderDetailDto dto = new OrderDetailDto();

        for (UpdateOrderDetailDto updateOrderDetailDto : request) {
            updateOrderRepository.updateAmount(updateOrderDetailDto.getAmount(), updateOrderDetailDto.getOrderId(),
                updateOrderDetailDto.getBagId().longValue());
            updateOrderRepository
                .updateExporter(updateOrderDetailDto.getExportedQuantity(), updateOrderDetailDto.getOrderId(),
                    updateOrderDetailDto.getBagId().longValue());
            updateOrderRepository
                .updateConfirm(updateOrderDetailDto.getConfirmedQuantity(), updateOrderDetailDto.getOrderId(),
                    updateOrderDetailDto.getBagId().longValue());
        }

        Order order = orderRepository.getOrderDetails(request.get(0).getOrderId())
            .orElseThrow(() -> new UnexistingOrderException(
                ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + request.get(0).getOrderId()));

        setOrderDetailDto(dto, order, request.get(0).getOrderId(), language);

        return modelMapper.map(dto, new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public CounterOrderDetailsDto getOrderSumDetails(Long id) {
        CounterOrderDetailsDto dto = new CounterOrderDetailsDto();
        Order order = orderRepository.getOrderDetails(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Bag> bag = bagRepository.findBagByOrderId(id);
        List<Certificate> currentCertificate = certificateRepository.findCertificate(id);
        final List<Payment> payment = paymentRepository.paymentInfo(id);

        double sumAmount = 0;
        double sumConfirmed = 0;
        double sumExported = 0;
        double totalSumAmount;
        double totalSumConfirmed;
        double totalSumExported;

        List<Integer> amountValues =
            order.getAmountOfBagsOrdered().entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList());

        List<Integer> confirmedValues =
            order.getConfirmedQuantity().entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList());

        List<Integer> exportedValues =
            order.getExportedQuantity().entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (int i = 0; i < bag.size(); i++) {
            sumAmount += amountValues.get(i) * bag.get(i).getPrice();
            sumConfirmed += confirmedValues.get(i) * bag.get(i).getPrice();
            sumExported += exportedValues.get(i) * bag.get(i).getPrice();
        }

        if (!currentCertificate.isEmpty()) {
            totalSumAmount =
                (sumAmount - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                    - order.getPointsToUse()));
            totalSumConfirmed =
                (sumConfirmed
                    - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                        - order.getPointsToUse()));
            totalSumExported =
                (sumExported - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                    - order.getPointsToUse()));
            dto.setCertificateBonus(
                currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0).doubleValue());
            dto.setCertificate(
                currentCertificate.stream().map(Certificate::getCode).collect(Collectors.toList()));
        } else {
            totalSumAmount = sumAmount - order.getPointsToUse();
            totalSumConfirmed = sumConfirmed - order.getPointsToUse();
            totalSumExported = sumExported - order.getPointsToUse();
        }

        dto.setTotalAmount(
            order.getAmountOfBagsOrdered().entrySet()
                .stream().map(Map.Entry::getValue).reduce(Integer::sum).orElse(0).doubleValue());
        dto.setTotalConfirmed(
            order.getConfirmedQuantity().entrySet()
                .stream().map(Map.Entry::getValue).reduce(Integer::sum).orElse(0).doubleValue());
        dto.setTotalExported(
            order.getExportedQuantity().entrySet()
                .stream().map(Map.Entry::getValue).reduce(Integer::sum).orElse(0).doubleValue());

        setDtoInfo(dto, sumAmount, sumExported, sumConfirmed, totalSumAmount, totalSumConfirmed, totalSumExported,
            order);
        updateStatus(payment, order, totalSumConfirmed, totalSumExported);
        return dto;
    }

    private void setDtoInfo(CounterOrderDetailsDto dto, double sumAmount, double sumExported, double sumConfirmed,
        double totalSumAmount, double totalSumConfirmed, double totalSumExported, Order order) {
        dto.setSumAmount(sumAmount);
        dto.setSumConfirmed(sumConfirmed);
        dto.setSumExported(sumExported);
        dto.setOrderComment(order.getComment());
        dto.setNumberOrderFromShop(order.getAdditionalOrders());
        dto.setBonus(order.getPointsToUse().doubleValue());
        dto.setTotalSumAmount(totalSumAmount);
        dto.setTotalSumConfirmed(totalSumConfirmed);
        dto.setTotalSumExported(totalSumExported);
    }

    private void updateStatus(List<Payment> payments, Order currentOrder, double totalConfirmed, double totalExported) {
        if (currentOrder.getOrderStatus() == OrderStatus.FORMED
            || currentOrder.getOrderStatus() == OrderStatus.CONFIRMED
            || currentOrder.getOrderStatus() == OrderStatus.ADJUSTMENT) {
            if (payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L) >= totalConfirmed) {
                payments.forEach(x -> x.setPaymentStatus(PaymentStatus.PAID));
            }
            if (totalConfirmed > payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L)
                && payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L) == 0L) {
                payments.forEach(x -> x.setPaymentStatus(PaymentStatus.UNPAID));
            }
            if (totalConfirmed > 0 && payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L) > 0
                && totalConfirmed > payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L)) {
                payments.forEach(x -> x.setPaymentStatus(PaymentStatus.HALF_PAID));
            }
        } else if (currentOrder.getOrderStatus() == OrderStatus.ON_THE_ROUTE
            || currentOrder.getOrderStatus() == OrderStatus.DONE
            || currentOrder.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF
            || currentOrder.getOrderStatus() == OrderStatus.CANCELLED) {
            if (totalExported > payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L)) {
                payments.forEach(x -> x.setPaymentStatus(PaymentStatus.HALF_PAID));
            }
            if (totalExported <= payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L)) {
                payments.forEach(x -> x.setPaymentStatus(PaymentStatus.PAID));
            }
        }
        paymentRepository.saveAll(payments);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public OrderDetailStatusDto getOrderDetailStatus(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Payment> payment = paymentRepository.paymentInfo(id);
        if (payment.isEmpty()) {
            throw new PaymentNotFoundException("payment not found for order id " + id);
        }
        return buildStatuses(order, payment.get(0));
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public OrderDetailStatusDto updateOrderDetailStatus(Long id, OrderDetailStatusRequestDto dto) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Payment> payment = paymentRepository.paymentInfo(id);
        if (payment.isEmpty()) {
            throw new PaymentNotFoundException("payment not found for order id " + id);
        }
        order.setComment(dto.getOrderComment());
        order.setOrderStatus(OrderStatus.valueOf(dto.getOrderStatus()));
        paymentRepository.paymentInfo(id)
            .forEach(x -> x.setPaymentStatus(PaymentStatus.valueOf(dto.getPaymentStatus())));
        orderRepository.save(order);
        paymentRepository.saveAll(payment);
        return buildStatuses(order, payment.get(0));
    }

    private OrderDetailStatusDto buildStatuses(Order order, Payment payment) {
        return OrderDetailStatusDto.builder()
            .orderStatus(order.getOrderStatus().name())
            .paymentStatus(payment.getPaymentStatus().name())
            .date(order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .build();
    }

    /**
     * Method returns detailed information about user violation by order id.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link ViolationDetailInfoDto};
     * @author Rusanovscaia Nadejda
     */
    @Override
    @Transactional
    public Optional<ViolationDetailInfoDto> getViolationDetailsByOrderId(Long orderId) {
        return violationRepository.findByOrderId(orderId).map(v -> ViolationDetailInfoDto.builder()
            .orderId(orderId)
            .userName(v.getUser().getRecipientName())
            .violationLevel(v.getViolationLevel())
            .description(v.getDescription())
            .violationDate(v.getViolationDate())
            .build());
    }

    private OrderDetailDto setOrderDetailDto(OrderDetailDto dto, Order order, Long orderId, String language) {
        dto.setAmount(modelMapper.map(order, new TypeToken<List<BagMappingDto>>() {
        }.getType()));

        dto.setCapacityAndPrice(bagRepository.findBagByOrderId(orderId)
            .stream()
            .map(b -> modelMapper.map(b, BagInfoDto.class))
            .collect(Collectors.toList()));

        dto.setName(bagTranslationRepository.findAllByLanguageOrder(language, orderId)
            .stream()
            .map(b -> modelMapper.map(b, BagTransDto.class))
            .collect(Collectors.toList()));

        dto.setOrderId(orderId);

        return dto;
    }

    private Address updateAddressOrderInfo(Address address, OrderAddressDtoUpdate dto) {
        address.setHouseNumber(dto.getHouseNumber());
        address.setEntranceNumber(dto.getEntranceNumber());
        address.setDistrict(dto.getDistrict());
        address.setStreet(dto.getStreet());
        address.setHouseCorpus(dto.getHouseCorpus());
        return address;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<DetailsOrderInfoDto> getOrderBagsDetails(Long orderId) {
        List<DetailsOrderInfoDto> detailsOrderInfoDtos = new ArrayList<>();
        List<Map<String, Object>> ourResult = bagsInfoRepository.getBagInfo(orderId);
        for (Map<String, Object> array : ourResult) {
            DetailsOrderInfoDto dto = objectMapper.convertValue(array, DetailsOrderInfoDto.class);
            detailsOrderInfoDtos.add(dto);
        }
        return detailsOrderInfoDtos;
    }
}