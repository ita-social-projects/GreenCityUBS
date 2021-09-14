package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.*;
import greencity.entity.order.*;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.*;
import greencity.filters.SearchCriteria;
import greencity.repository.*;
import greencity.service.NotificationServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final AllValuesFromTableRepo allValuesFromTableRepo;
    private final ObjectMapper objectMapper;
    private final BagRepository bagRepository;
    private final BagTranslationRepository bagTranslationRepository;
    private final UpdateOrderDetail updateOrderRepository;
    private final BagsInfoRepo bagsInfoRepository;
    private final ViolationRepository violationRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final AdditionalBagsInfoRepo additionalBagsInfoRepo;
    private final NotificationServiceImpl notificationService;
    private final FileService fileService;
    private final PositionRepository positionRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;

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

        while (!allCoords.isEmpty()) {
            Optional<Coordinates> any = allCoords.stream().findAny();
            mainBlockOfGetClusteredCoords(allCoords, distance, litres, any, allClusters);
        }
        return allClusters;
    }

    private void mainBlockOfGetClusteredCoords(Set<Coordinates> allCoords, double distance,
        int litres, Optional<Coordinates> any, List<GroupedOrderDto> allClusters) {
        any.ifPresent(coordinates -> {
            Coordinates currentlyCoord = coordinates;

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
                closeRelativesSorted.sort(getComparatorByDistanceFromCenter(centralCoord));
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
        });
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

        coordinatesInsideRadiusWithoutSpecifiedCoords.sort(getComparatorByDistanceFromCenter(centralCoord));
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
    /**
     * Method gets all order payments, count paid amount, amount which user should
     * paid and overpayment amount.
     *
     * @param orderId  of {@link Long} order id;
     * @param sumToPay of {@link Long} sum to pay;
     * @return {@link PaymentTableInfoDto }
     * @author Nazar Struk, Ostap Mykhailivskyi
     */
    @Override
    public PaymentTableInfoDto getPaymentInfo(long orderId, Long sumToPay) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Long paidAmount = calculatePaidAmount(order);
        Long overpayment = calculateOverpayment(order, sumToPay);
        Long unPaidAmount = calculateUnpaidAmount(sumToPay, paidAmount);
        PaymentTableInfoDto paymentTableInfoDto = new PaymentTableInfoDto();
        paymentTableInfoDto.setOverpayment(overpayment);
        paymentTableInfoDto.setUnPaidAmount(unPaidAmount);
        paymentTableInfoDto.setPaidAmount(paidAmount);
        List<PaymentInfoDto> paymentInfoDtos = order.getPayment().stream()
            .filter(payment -> payment.getPaymentStatus().equals(PaymentStatus.PAID))
            .map(x -> modelMapper.map(x, PaymentInfoDto.class)).collect(Collectors.toList());
        paymentTableInfoDto.setPaymentInfoDtos(paymentInfoDtos);
        if ((order.getOrderStatus() == OrderStatus.DONE)) {
            notificationService.notifyBonuses(order, overpayment);
        }
        return paymentTableInfoDto;
    }

    /**
     * Method returns overpayment and bonuses to users account.
     *
     * @param orderId                   of {@link Long} order id;
     * @param overpaymentInfoRequestDto {@link OverpaymentInfoRequestDto}
     * @author Ostap Mykhailivskyi
     */
    @Override
    public void returnOverpayment(Long orderId,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        User user = userRepository.findUserByOrderId(orderId)
            .orElseThrow(
                () -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Payment payment = createPayment(order, overpaymentInfoRequestDto);

        if ((order.getOrderStatus() == OrderStatus.DONE)) {
            returnOverpaymentForStatusDone(user, order, overpaymentInfoRequestDto, payment);
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED
            && overpaymentInfoRequestDto.getComment().equals(AppConstant.PAYMENT_REFUND)) {
            returnOverpaymentAsMoneyForStatusCancelled(user, order, overpaymentInfoRequestDto);
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED && overpaymentInfoRequestDto.getComment()
            .equals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT)) {
            returnOverpaymentAsBonusesForStatusCancelled(user, order, overpaymentInfoRequestDto);
        }
        order.getPayment().add(payment);
        userRepository.save(user);
    }

    /**
     * Method return's information about overpayment and used bonuses on canceled
     * and done orders.
     *
     * @param orderId  of {@link Long} order id;
     * @param sumToPay of {@link Long} sum to pay;
     * @param marker   of {@link Long} marker;
     * @return {@link PaymentTableInfoDto }
     * @author Ostap Mykhailivskyi
     */
    @Override
    public PaymentTableInfoDto returnOverpaymentInfo(Long orderId, Long sumToPay, Long marker) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Long overpayment = calculateOverpayment(order, sumToPay);
        PaymentTableInfoDto dto = getPaymentInfo(orderId, sumToPay);
        PaymentInfoDto payDto = PaymentInfoDto.builder().amount(overpayment)
            .settlementdate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).build();
        if (marker == 1L) {
            payDto.setComment(AppConstant.PAYMENT_REFUND);
        } else {
            payDto.setComment(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT);
        }
        dto.getPaymentInfoDtos().add(payDto);
        dto.setOverpayment(dto.getOverpayment() - overpayment);
        return dto;
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
    public void addUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles) {
        Order order = orderRepository.findById(add.getOrderID()).orElseThrow(() -> new UnexistingOrderException(
            ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        User user = order.getUser();
        Violation violation = violationBuilder(add, order, user);
        if (multipartFiles.length > 0) {
            List<String> images = new LinkedList<>();
            for (int i = 0; i < multipartFiles.length; i++) {
                images.add(fileService.upload(multipartFiles[i]));
            }
            violation.setImage(String.join("; ", images));
        }
        if (violationRepository.findByOrderId(order.getId()).isEmpty()) {
            if (user.getViolations() < 0) {
                user.setViolations(0);
            }
            user.setViolations(user.getViolations() + 1);
            violationRepository.save(violation);
            userRepository.save(user);
        } else {
            throw new OrderViolationException(ORDER_ALREADY_HAS_VIOLATION);
        }
        notificationService.notifyAddViolation(order);
    }

    private Violation violationBuilder(AddingViolationsToUserDto add, Order order, User user) {
        return Violation.builder()
            .violationLevel(ViolationLevel.valueOf(add.getViolationLevel().toUpperCase()))
            .description(add.getViolationDescription())
            .violationDate(order.getOrderDate())
            .order(order)
            .user(user)
            .build();
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
        if (orderRepository.findById(orderId).isEmpty()) {
            throw new NotFoundOrderAddressException(NOT_FOUND_ADDRESS_BY_ORDER_ID + orderId);
        }
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
        Optional<Address> optionalAddress = addressRepository.findById(address.getId());
        return optionalAddress.map(value -> modelMapper.map(value, OrderAddressDtoResponse.class)).orElse(null);
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

        List<Integer> amountValues = new ArrayList<>(order.getAmountOfBagsOrdered().values());

        List<Integer> confirmedValues = new ArrayList<>(order.getConfirmedQuantity().values());

        List<Integer> exportedValues = new ArrayList<>(order.getExportedQuantity().values());

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
            order.getAmountOfBagsOrdered().values()
                .stream().reduce(Integer::sum).orElse(0).doubleValue());
        dto.setTotalConfirmed(
            order.getConfirmedQuantity().values()
                .stream().reduce(Integer::sum).orElse(0).doubleValue());
        dto.setTotalExported(
            order.getExportedQuantity().values()
                .stream().reduce(Integer::sum).orElse(0).doubleValue());

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
                currentOrder.setOrderPaymentStatus(OrderPaymentStatus.PAID);
                notificationService.notifyPaidOrder(currentOrder);
                if (currentOrder.getOrderStatus() == OrderStatus.ADJUSTMENT) {
                    notificationService.notifyCourierItineraryFormed(currentOrder);
                }
            }
            if (totalConfirmed > payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L)
                && payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L) == 0L) {
                currentOrder.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
            }
            if (totalConfirmed > 0 && payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L) > 0
                && totalConfirmed > payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L)) {
                currentOrder.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
                notificationService.notifyHalfPaidPackage(currentOrder);
            }
        } else if (currentOrder.getOrderStatus() == OrderStatus.ON_THE_ROUTE
            || currentOrder.getOrderStatus() == OrderStatus.DONE
            || currentOrder.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF
            || currentOrder.getOrderStatus() == OrderStatus.CANCELLED) {
            if (totalExported > payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L)) {
                currentOrder.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
                notificationService.notifyHalfPaidPackage(currentOrder);
            }
            if (totalExported <= payments.stream().map(Payment::getAmount).reduce(Long::sum).orElse(0L)) {
                currentOrder.setOrderPaymentStatus(OrderPaymentStatus.PAID);
                notificationService.notifyPaidOrder(currentOrder);
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
            throw new PaymentNotFoundException(PAYMENT_NOT_FOUND + id);
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
            throw new PaymentNotFoundException(PAYMENT_NOT_FOUND + id);
        }
        order.setComment(dto.getOrderComment());
        OrderStatus newStatus = OrderStatus.valueOf(dto.getOrderStatus());
        order.setOrderStatus(newStatus);
        if (newStatus == OrderStatus.ADJUSTMENT) {
            notificationService.notifyCourierItineraryFormed(order);
        }

        paymentRepository.paymentInfo(id)
            .forEach(x -> x.setPaymentStatus(PaymentStatus.valueOf(dto.getPaymentStatus())));
        orderRepository.save(order);
        paymentRepository.saveAll(payment);
        return buildStatuses(order, payment.get(0));
    }

    private OrderDetailStatusDto buildStatuses(Order order, Payment payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String orderDate = order.getOrderDate().toLocalDate().format(formatter);
        return OrderDetailStatusDto.builder()
            .orderStatus(order.getOrderStatus().name())
            .paymentStatus(payment.getPaymentStatus().name())
            .date(orderDate)
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

    @Override
    @Transactional
    public void deleteViolation(Long id) {
        Optional<Violation> violationOptional = violationRepository.findByOrderId(id);
        if (violationOptional.isPresent()) {
            if (violationOptional.get().getImage() != null) {
                List<String> images = new LinkedList<>(Arrays.asList(violationOptional.get().getImage().split("; ")));
                for (int i = 0; i < images.size(); i++) {
                    fileService.delete(images.get(i));
                }
            }
            violationRepository.deleteById(violationOptional.get().getId());
            User user = violationOptional.get().getUser();
            if (user.getViolations() <= 0) {
                user.setViolations(0);
            } else {
                user.setViolations(user.getViolations() - 1);
            }
            userRepository.save(user);
        } else {
            throw new UnexistingOrderException(VIOLATION_DOES_NOT_EXIST);
        }
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

    /**
     * Method returns export details by order id.
     *
     * @param id of {@link Long} order id;
     * @return {@link ExportDetailsDto};
     * @author Orest Mahdziak
     */
    @Override
    public ExportDetailsDto getOrderExportDetails(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<ReceivingStation> receivingStation = receivingStationRepository.findAll();
        if (receivingStation.isEmpty()) {
            throw new ReceivingStationNotFoundException(RECEIVING_STATION_NOT_FOUND);
        }
        return buildExportDto(order, receivingStation);
    }

    /**
     * Method returns update export details by order id.
     *
     * @param id  of {@link Long} order id;
     * @param dto of{@link ExportDetailsDtoRequest}
     * @return {@link ExportDetailsDto};
     * @author Orest Mahdziak
     */
    @Override
    public ExportDetailsDto updateOrderExportDetails(Long id, ExportDetailsDtoRequest dto) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<ReceivingStation> receivingStation = receivingStationRepository.findAll();
        if (receivingStation.isEmpty()) {
            throw new ReceivingStationNotFoundException(RECEIVING_STATION_NOT_FOUND);
        }
        String str = dto.getExportedDate() + " " + dto.getExportedTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
        order.setDeliverFrom(dateTime);
        order.setReceivingStation(dto.getReceivingStation());
        orderRepository.save(order);
        return buildExportDto(order, receivingStation);
    }

    private ExportDetailsDto buildExportDto(Order order, List<ReceivingStation> receivingStation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm:ss");
        String date =
            order.getDeliverFrom() == null ? "" : order.getDeliverFrom().toLocalDate().format(formatter);
        String time =
            order.getDeliverFrom() == null ? "" : order.getDeliverFrom().toLocalTime().format(formatter2);
        return ExportDetailsDto.builder()
            .allReceivingStations(receivingStation.stream().map(ReceivingStation::getName).collect(Collectors.toList()))
            .exportedDate(date)
            .exportedTime(time)
            .receivingStation(order.getReceivingStation())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdditionalBagInfoDto> getAdditionalBagsInfo(Long orderId) {
        User user = userRepository.findUserByOrderId(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        String recipientEmail = user.getRecipientEmail();
        List<AdditionalBagInfoDto> ourResult1 = new ArrayList<>();
        List<Map<String, Object>> ourResult = additionalBagsInfoRepo.getAdditionalBagInfo(orderId, recipientEmail);
        for (Map<String, Object> array : ourResult) {
            AdditionalBagInfoDto dto = objectMapper.convertValue(array, AdditionalBagInfoDto.class);
            ourResult1.add(dto);
        }
        return ourResult1;
    }

    /**
     * Method that calculate's overpayment on user's order.
     *
     * @param order    of {@link Order} order;
     * @param sumToPay of {@link Long} sum to pay;
     * @return {@link Long }
     * @author Ostap Mykhailivskyi
     */
    private Long calculateOverpayment(Order order, Long sumToPay) {
        Long paymentSum = order.getPayment().stream()
            .filter(x -> x.getPaymentStatus().equals(PaymentStatus.PAID))
            .map(Payment::getAmount)
            .reduce(Long::sum)
            .orElse(0L);
        return Math.max((paymentSum - sumToPay), 0L);
    }

    /**
     * Method that calculate paid amount.
     *
     * @param order of {@link Order} order id;
     * @return {@link Long }
     * @author Ostap Mykhailivskyi
     */
    private Long calculatePaidAmount(Order order) {
        return order.getPayment().stream().filter(x -> x.getPaymentStatus().equals(PaymentStatus.PAID))
            .map(Payment::getAmount).reduce(0L, (a, b) -> a + b);
    }

    /**
     * Method that calculate unpaid amount.
     *
     * @param sumToPay   of {@link Long} sum to pay;
     * @param paidAmount of {@link Long} sum to pay;
     * @return {@link Long }
     * @author Ostap Mykhailivskyi
     */
    private Long calculateUnpaidAmount(Long sumToPay, Long paidAmount) {
        return Math.max((sumToPay - paidAmount), 0L);
    }

    private ChangeOfPoints createChangeOfPoints(Order order, User user, Long amount) {
        return ChangeOfPoints.builder()
            .date(LocalDateTime.now())
            .user(user)
            .order(order)
            .amount(Math.toIntExact(amount))
            .build();
    }

    private Payment createPayment(Order order, OverpaymentInfoRequestDto dto) {
        return Payment.builder()
            .order(order)
            .orderStatus("approved")
            .comment(dto.getComment())
            .currency("UAH")
            .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .paymentStatus(PaymentStatus.PAYMENT_REFUNDED)
            .amount(dto.getOverpayment())
            .build();
    }

    private void returnOverpaymentForStatusDone(User user, Order order,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto,
        Payment payment) {
        user.setCurrentPoints((int) (user.getCurrentPoints() + overpaymentInfoRequestDto.getOverpayment()));
        user.getChangeOfPointsList()
            .add(createChangeOfPoints(order, user, overpaymentInfoRequestDto.getOverpayment()));
        payment.setComment(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT);
    }

    private void returnOverpaymentAsMoneyForStatusCancelled(User user, Order order,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto) {
        user.setCurrentPoints((int) (user.getCurrentPoints() + overpaymentInfoRequestDto.getBonuses()));
        user.getChangeOfPointsList().add(createChangeOfPoints(order, user, overpaymentInfoRequestDto.getBonuses()));
        order.getPayment().forEach(p -> p.setPaymentStatus(PaymentStatus.PAYMENT_REFUNDED));
    }

    private void returnOverpaymentAsBonusesForStatusCancelled(User user, Order order,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto) {
        user.setCurrentPoints((int) (user.getCurrentPoints() + overpaymentInfoRequestDto.getOverpayment()
            + overpaymentInfoRequestDto.getBonuses()));
        user.getChangeOfPointsList()
            .add(createChangeOfPoints(order, user, overpaymentInfoRequestDto.getOverpayment()));
        user.getChangeOfPointsList().add(createChangeOfPoints(order, user, overpaymentInfoRequestDto.getBonuses()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        return buildPaymentResponseDto(paymentRepository.save(buildPaymentEntity(order, paymentRequestDto, image)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteManualPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not found"));
        if (payment.getImagePath() != null) {
            fileService.delete(payment.getImagePath());
        }
        paymentRepository.deletePaymentById(paymentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManualPaymentResponseDto updateManualPayment(Long paymentId,
        ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
            () -> new PaymentNotFoundException(PAYMENT_NOT_FOUND + paymentId));
        paymentRepository.save(changePaymentEntity(payment, paymentRequestDto, image));
        return buildPaymentResponseDto(
            paymentRepository.save(changePaymentEntity(payment, paymentRequestDto, image)));
    }

    private Payment changePaymentEntity(Payment updatePayment,
        ManualPaymentRequestDto requestDto,
        MultipartFile image) {
        updatePayment.setSettlementDate(requestDto.getPaymentDate());
        updatePayment.setAmount(requestDto.getAmount());
        updatePayment.setPaymentId(requestDto.getPaymentId());
        updatePayment.setReceiptLink(requestDto.getReceiptLink());
        if (updatePayment.getImagePath() != null) {
            fileService.delete(updatePayment.getImagePath());
        }
        if (image != null) {
            updatePayment.setImagePath(fileService.upload(image));
        } else {
            updatePayment.setImagePath(null);
        }

        return updatePayment;
    }

    private ManualPaymentResponseDto buildPaymentResponseDto(Payment payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return ManualPaymentResponseDto.builder()
            .paymentId(payment.getPaymentId())
            .paymentDate(payment.getSettlementDate())
            .amount(payment.getAmount())
            .receiptLink(payment.getReceiptLink())
            .imagePath(payment.getImagePath())
            .currentDate(LocalDate.now().format(formatter))
            .build();
    }

    private Payment buildPaymentEntity(Order order, ManualPaymentRequestDto paymentRequestDto, MultipartFile image) {
        Payment payment = Payment.builder()
            .settlementDate(paymentRequestDto.getPaymentDate())
            .amount(paymentRequestDto.getAmount())
            .paymentStatus(PaymentStatus.PAID)
            .paymentId(paymentRequestDto.getPaymentId())
            .receiptLink(paymentRequestDto.getReceiptLink())
            .currency("UAH")
            .paymentType(PaymentType.MANUAL)
            .order(order)
            .orderStatus(order.getOrderStatus().toString())
            .build();
        if (image != null) {
            payment.setImagePath(fileService.upload(image));
        }
        return payment;
    }

    @Override
    public EmployeePositionDtoRequest getAllEmployeesByPosition(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        EmployeePositionDtoRequest dto = EmployeePositionDtoRequest.builder().orderId(order.getId()).build();
        List<EmployeeOrderPosition> newList = employeeOrderPositionRepository.findAllByOrderId(order.getId());
        if (!newList.isEmpty()) {
            Map<PositionDto, String> currentPositionEmployee = new HashMap<>();
            newList.forEach(x -> currentPositionEmployee.put(
                PositionDto.builder().id(x.getPosition().getId()).name(x.getPosition().getName()).build(),
                x.getEmployee().getFirstName().concat(" ").concat(x.getEmployee().getLastName())));
            dto.setCurrentPositionEmployees(currentPositionEmployee);
        }
        List<Position> positions = positionRepository.findAll();
        Map<PositionDto, List<String>> allPositionEmployee = new HashMap<>();
        for (Position position : positions) {
            PositionDto positionDto = PositionDto.builder().id(position.getId()).name(position.getName()).build();
            allPositionEmployee.put(positionDto, employeeRepository.getAllEmployeeByPositionId(position.getId())
                .stream().map(e -> e.getFirstName() + " " + e.getLastName()).collect(Collectors.toList()));
        }
        dto.setAllPositionsEmployees(allPositionEmployee);

        return dto;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Transactional
    public void updatePositions(EmployeePositionDtoResponse dto) {
        List<EmployeeOrderPosition> newList = employeeOrderPositionRepository.findAllByOrderId(dto.getOrderId());
        if (!newList.isEmpty()) {
            employeeOrderPositionRepository.deleteAll(newList);
        }

        Order order = orderRepository.findById(dto.getOrderId())
            .orElseThrow(
                () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + " " + dto.getOrderId()));

        List<EmployeeOrderPosition> employeeOrderPositions = new ArrayList<>();
        for (EmployeeOrderPositionDTO employeeOrderPositionDTO : dto.getEmployeeOrderPositionDTOS()) {
            String[] dtoFirstAndLastName = employeeOrderPositionDTO.getName().split(" ");
            Position position = positionRepository.findById(employeeOrderPositionDTO.getPositionId())
                .orElseThrow(() -> new PositionNotFoundException(POSITION_NOT_FOUND));
            Employee employee = employeeRepository.findByName(dtoFirstAndLastName[0], dtoFirstAndLastName[1])
                .orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND));
            employeeOrderPositions.add(EmployeeOrderPosition.builder()
                .employee(employee)
                .position(position)
                .order(order)
                .build());
        }
        employeeOrderPositionRepository.saveAll(employeeOrderPositions);
    }

    @Override
    public void updateUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles) {
        Violation violation = violationRepository.findByOrderId(add.getOrderID())
            .orElseThrow(() -> new UnexistingOrderException(ORDER_HAS_NOT_VIOLATION));
        updateViolation(violation, add, multipartFiles);
        violationRepository.save(violation);
    }

    private void updateViolation(Violation violation, AddingViolationsToUserDto add, MultipartFile[] multipartFiles) {
        violation.setViolationLevel(ViolationLevel.valueOf(add.getViolationLevel().toUpperCase()));
        violation.setDescription(add.getViolationDescription());
        if (violation.getImage() != null) {
            List<String> images = new LinkedList<>(Arrays.asList(violation.getImage().split("; ")));
            for (int i = 0; i < images.size(); i++) {
                fileService.delete(images.get(i));
            }
            violation.setImage(null);
            images.clear();
            if (multipartFiles.length > 0) {
                for (int i = 0; i < multipartFiles.length; i++) {
                    images.add(fileService.upload(multipartFiles[i]));
                }
                violation.setImage(String.join("; ", images));
            }
        } else {
            if (multipartFiles.length > 0) {
                List<String> images = new LinkedList<>();
                for (int i = 0; i < multipartFiles.length; i++) {
                    images.add(fileService.upload(multipartFiles[i]));
                }
                violation.setImage(String.join("; ", images));
            }
        }
    }

    @Override
    public TableParamsDTO getParametersForOrdersTable(Long userId) {
        List<ColumnStateDTO> columnStateDTOS = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new ColumnStateDTO("select", new TitleDto("Вибір", "Select"), 20, true, true, 0, EditType.CHECKBOX,
                new ArrayList<>()),
            new ColumnStateDTO("orderid", new TitleDto("Номер замовлення", "Order ID"), 20, true, false, 1,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("order_status", new TitleDto("Cтатус замовлення", "Order sataus"), 20, true, true, 2,
                EditType.SELECT, orderStatusListForDevelopStage()),
            new ColumnStateDTO("payment_status", new TitleDto("Статус оплати", "Aaaa"), 20, true, true, 3,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("order_date", new TitleDto("Дата замовлення", "Aaaa"), 20, true, true, 4,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("payment_date", new TitleDto("Дата оплати", "Aaaa"), 20, true, true, 5,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("client_name", new TitleDto("Ім'я замовника", "Bbbb"), 20, false, true, 6,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("phone_number", new TitleDto("Телефон замовника", "Cccc"), 20, false, true, 7,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("email", new TitleDto("Email замовника", "Dddd"), 20, false, true, 8,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("sender_name", new TitleDto("Ім'я відправника", "Hhhhh"), 20, false, true, 9,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("sender_phone", new TitleDto("Телефон відправника", "Oooo"), 20, false, true, 10,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("sender_email", new TitleDto("Email відправника", "Pppp"), 20, false, true, 11,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("violations", new TitleDto("Кількість порушень клієнта", "Eeee"), 20, false, true, 12,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("location", new TitleDto("Локація", "Eeee"), 20, false, true, 13, EditType.READ_ONLY,
                new ArrayList<>()),
            new ColumnStateDTO("district", new TitleDto("Район", "Ffff"), 20, false, true, 14, EditType.READ_ONLY,
                new ArrayList<>()),
            new ColumnStateDTO("address", new TitleDto("Адреса", "Gggg"), 20, false, true, 15, EditType.READ_ONLY,
                new ArrayList<>()),
            new ColumnStateDTO("comment_to_address_for_client", new TitleDto("Коментар до адреси від клієнта", ""), 20,
                false, true, 16, EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("bags_amount", new TitleDto("К-сть пакетів", "Kkkk"), 20, false, true, 17,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("total_order_sum", new TitleDto("Сума замовлення", "Nnnn"), 20, false, true, 18,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("order_certificate_code", new TitleDto("Номер сертифікату", "Ssss"), 20, false, true, 19,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("order_certificate_points", new TitleDto("Загальна знижка", "Ttttt"), 20, false, true,
                20, EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("amount_due", new TitleDto("Сума до оплати", ""), 20, false, true, 21,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("comment_for_order_by_client",
                new TitleDto("Коментар до замовлення від клієнта", "Rrrr"), 20, true, true, 22,
                EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("payment", new TitleDto("Оплата", "Xxx"), 20, false, true, 23, EditType.READ_ONLY,
                new ArrayList<>()),
            new ColumnStateDTO("date_of_export", new TitleDto("Дата вивезення", "Yyyy"), 20, false, true, 24,
                EditType.DATE, new ArrayList<>()),
            new ColumnStateDTO("time_of_export", new TitleDto("Час вивезення", "Zzzzz"), 20, false, true, 25,
                EditType.TIME, new ArrayList<>()),
            new ColumnStateDTO("id_order_from_shop", new TitleDto("Номер замовлення з магазину", "Hhhkh"), 20, false,
                true, 26, EditType.READ_ONLY, new ArrayList<>()),
            new ColumnStateDTO("receiving_station", new TitleDto("Станція приймання", ""), 20, false, true, 27,
                EditType.SELECT, orderOptionalListForDevelopStage()),
            new ColumnStateDTO("responsible_manager", new TitleDto("Менеджер послуги", "Rytryt"), 20, false, true, 28,
                EditType.SELECT, orderOptionalListForDevelopStage()),
            new ColumnStateDTO("responsible_caller", new TitleDto("Менеджер обдзвону", "Rytryt"), 20, false, true, 29,
                EditType.SELECT, orderOptionalListForDevelopStage()),
            new ColumnStateDTO("responsible_logic_man", new TitleDto("Логіст", "Hhjkhk"), 20, false, true, 30,
                EditType.SELECT, orderOptionalListForDevelopStage()),
            new ColumnStateDTO("responsible_driver", new TitleDto("Водій", "Wwrwew"), 20, false, true, 31,
                EditType.SELECT, orderOptionalListForDevelopStage()),
            new ColumnStateDTO("responsible_navigator", new TitleDto("Штурман", "Qqeqw"), 20, false, true, 32,
                EditType.SELECT, orderOptionalListForDevelopStage()),
            new ColumnStateDTO("comments_for_order", new TitleDto("Коментарі до замовлення", "Mjhjhk"), 20, false, true,
                33, EditType.READ_ONLY, new ArrayList<>()))));
        return new TableParamsDTO(columnStateDTOS, "orderid", SortingOrder.ASC);
    }

    @Override
    public HttpStatus chooseOrdersDataSwitcher(String userUuid,
        RequestToChangeOrdersDataDTO requestToChangeOrdersDataDTO) {
        String columnName = requestToChangeOrdersDataDTO.getColumnName();
        String value = requestToChangeOrdersDataDTO.getNewValue();
        List<Long> ordersId = requestToChangeOrdersDataDTO.getOrderId();
        switch (columnName) {
            case "order_status":
                orderStatusForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            case "date_of_export":
                dateOfExportForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            case "time_of_export":
                timeOfExportForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            case "receiving_station":
                receivingStationForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            case "responsible_manager":
                responsibleManagerForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            case "responsible_caller":
                responsibleCallerForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            case "responsible_logic_man":
                responsibleLogicManForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            case "responsible_driver":
                responsibleDriverForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            case "responsible_navigator":
                responsibleNavigatorForDevelopStage(ordersId, value);
                return HttpStatus.ACCEPTED;
            default:
                return HttpStatus.BAD_REQUEST;
        }
    }

    private List<TitleDto> orderStatusListForDevelopStage() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new TitleDto("Сформовано", "FORMED"),
            new TitleDto("На узгодженні", "ADJUSTMENT"),
            new TitleDto("Заберуть самостійно", "BROUGHT_IT_HIMSELF"),
            new TitleDto("Підтверджено", "CONFIRMED"),
            new TitleDto("В дорозі", "ON_THE_ROUTE"),
            new TitleDto("Виконано", "DONE"),
            new TitleDto("Не вивезено", "NOT_TAKEN_OUT"),
            new TitleDto("Скасовано", "CANCELLED"))));
    }

    private List<TitleDto> orderOptionalListForDevelopStage() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new TitleDto("Щось перше", "Something first"),
            new TitleDto("Щось інше", "Something other"),
            new TitleDto("Третій варіант", "Thirst variant"))));
    }

    private void orderStatusForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }

    private void dateOfExportForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }

    private void timeOfExportForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }

    private void receivingStationForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }

    private void responsibleManagerForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }

    private void responsibleCallerForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }

    private void responsibleLogicManForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }

    private void responsibleDriverForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }

    private void responsibleNavigatorForDevelopStage(List<Long> ordersId, String value) {
        System.out.println(ordersId + value);
    }
}