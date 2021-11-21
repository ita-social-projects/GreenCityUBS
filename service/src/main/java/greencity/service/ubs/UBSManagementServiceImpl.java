package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.*;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.*;
import greencity.filters.*;
import greencity.repository.*;
import greencity.service.NotificationServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
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
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

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
    private final BigOrderTableRepository bigOrderTableRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final AdditionalBagsInfoRepo additionalBagsInfoRepo;
    private final NotificationServiceImpl notificationService;
    private final FileService fileService;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private static final String defaultImagePath = AppConstant.DEFAULT_IMAGE;
    private final EventService eventService;
    private final LanguageRepository languageRepository;
    private final CertificateCriteriaRepo certificateCriteriaRepo;
    private final CustomTableViewRepo customTableViewRepo;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private UBSClientService ubsClientService;

    /**
     * This is method which inject {@link UBSClientService} in order to avoid
     * cycling between beans.
     *
     * @param ubsClientService {@link UBSClientServiceImpl}.
     */
    @Autowired
    public void setUbsClientService(@Qualifier("UBSClientServiceImpl") UBSClientService ubsClientService) {
        this.ubsClientService = ubsClientService;
    }

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
     * This method save or update view of orders table.
     *
     * @author Sikhovskiy Rostyslav.
     */
    @Override
    public void changeOrderTableView(String uuid, String titles) {
        if (Boolean.TRUE.equals(customTableViewRepo.existsByUuid(uuid))) {
            customTableViewRepo.update(uuid, titles);
        } else {
            CustomTableView customTableView = CustomTableView.builder()
                .uuid(uuid)
                .titles(titles)
                .build();
            customTableViewRepo.save(customTableView);
        }
    }

    /**
     * This method return parameters for orders table view.
     *
     * @author Sikhovskiy Rostyslav.
     */
    @Override
    public CustomTableViewDto getCustomTableParameters(String uuid) {
        if (Boolean.TRUE.equals(customTableViewRepo.existsByUuid(uuid))) {
            return castTableViewToDto(customTableViewRepo.findByUuid(uuid).getTitles());
        } else {
            return CustomTableViewDto.builder()
                .titles("")
                .build();
        }
    }

    private CustomTableViewDto castTableViewToDto(String titles) {
        return CustomTableViewDto.builder()
            .titles(titles)
            .build();
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
     * @param uuid                      {@link String}.
     * @author Ostap Mykhailivskyi
     */
    @Override
    public void returnOverpayment(Long orderId,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
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
            collectEventsAboutOverpayment(overpaymentInfoRequestDto.getComment(), order, currentUser);
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED && overpaymentInfoRequestDto.getComment()
            .equals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT)) {
            returnOverpaymentAsBonusesForStatusCancelled(user, order, overpaymentInfoRequestDto);
            collectEventsAboutOverpayment(overpaymentInfoRequestDto.getComment(), order, currentUser);
        }
        order.getPayment().add(payment);
        userRepository.save(user);
    }

    /**
     * This is method which collect's events about overpayment for order.
     *
     * @param commentStatus {@link String} comments status.
     * @param order         {@link Order}.
     * @param currentUser   {@link User}.
     * @author Yuriy Bahlay.
     */
    private void collectEventsAboutOverpayment(String commentStatus, Order order, User currentUser) {
        if (commentStatus.equals(AppConstant.PAYMENT_REFUND)) {
            eventService.save(OrderHistory.RETURN_OVERPAYMENT_TO_CLIENT, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
        } else if (commentStatus.equals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT)) {
            eventService.save(OrderHistory.RETURN_OVERPAYMENT_AS_BONUS_TO_CLIENT,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
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
    public PageableDto<CertificateDtoForSearching> getAllCertificates(Pageable page, String columnName,
        SortingOrder sortingOrder) {
        PageRequest pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(),
            Sort.by(Sort.Direction.fromString(sortingOrder.toString()), columnName));
        Page<Certificate> certificates = certificateRepository.getAll(pageRequest);
        return getAllCertificatesTranslationDto(certificates);
    }

    @Override
    public PageableDto<CertificateDtoForSearching> getCertificatesWithFilter(CertificatePage certificatePage,
        CertificateFilterCriteria certificateFilterCriteria) {
        Page<Certificate> certificates =
            certificateCriteriaRepo.findAllWithFilter(certificatePage, certificateFilterCriteria);
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
    public void addUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles, String uuid) {
        Order order = orderRepository.findById(add.getOrderID()).orElseThrow(() -> new UnexistingOrderException(
            ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (violationRepository.findByOrderId(order.getId()).isEmpty()) {
            User user = order.getUser();
            Violation violation = violationBuilder(add, order);
            if (multipartFiles.length > 0) {
                List<String> images = new LinkedList<>();
                setImages(multipartFiles, images);
                violation.setImages(images);
            }
            violationRepository.save(violation);
            user.setViolations(userRepository.countTotalUsersViolations(user.getId()));
            userRepository.save(user);
            eventService.save(OrderHistory.ADD_VIOLATION, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
            notificationService.notifyAddViolation(order);
        } else {
            throw new OrderViolationException(ORDER_ALREADY_HAS_VIOLATION);
        }
    }

    private Violation violationBuilder(AddingViolationsToUserDto add, Order order) {
        return Violation.builder()
            .violationLevel(ViolationLevel.valueOf(add.getViolationLevel().toUpperCase()))
            .description(add.getViolationDescription())
            .violationDate(order.getOrderDate())
            .order(order)
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
    public PageableDto<AllFieldsFromTableDto> getAllValuesFromTable(SearchCriteria searchCriteria, int page, int size,
        String column, String sortingType) {
        List<AllFieldsFromTableDto> orderList = new ArrayList<>();

        SortingOrder resultSortingOrder = Arrays.stream(SortingOrder.values())
            .filter(s -> s.name().equalsIgnoreCase(sortingType))
            .findAny().orElseThrow(() -> new IncorrectValueException("Invalid column name"));

        String resultColumn = allValuesFromTableRepo.getCustomColumns().stream()
            .filter(c -> c.equalsIgnoreCase(column))
            .findAny().orElseThrow(() -> new IncorrectValueException("Invalid column name"));

        List<Map<String, Object>> ordersInfo = allValuesFromTableRepo
            .findAll(searchCriteria, page, size, resultColumn, resultSortingOrder);

        for (Map<String, Object> map : ordersInfo) {
            AllFieldsFromTableDto allFieldsFromTableDto =
                objectMapper.convertValue(map, AllFieldsFromTableDto.class);

            if (allFieldsFromTableDto.getDateOfExport() == null || allFieldsFromTableDto.getTimeOfExport() == null) {
                allFieldsFromTableDto.setDateOfExport(LocalDate.now().toString());
                allFieldsFromTableDto.setTimeOfExport(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            List<Map<String, Object>> employees =
                allValuesFromTableRepo.findAllEmployee(allFieldsFromTableDto.getOrderId());

            for (Map<String, Object> objectMap : employees) {
                Long positionId = (Long) objectMap.get("position_id");
                String employeeName = (String) objectMap.get("name");

                if (positionId == 1) {
                    allFieldsFromTableDto.setResponsibleManager(employeeName);
                } else if (positionId == 2) {
                    allFieldsFromTableDto.setResponsibleLogicMan(employeeName);
                } else if (positionId == 3) {
                    allFieldsFromTableDto.setResponsibleDriver(employeeName);
                } else if (positionId == 4) {
                    allFieldsFromTableDto.setResponsibleNavigator(employeeName);
                }
            }

            orderList.add(allFieldsFromTableDto);
        }

        int listSize = userRepository.orderCounter();
        int totalPages = (listSize % size) == 0 ? (listSize / size) : (listSize / size) + 1;

        return new PageableDto<>(
            orderList,
            listSize,
            page,
            totalPages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<BigOrderTableDTO> getOrders(OrderPage orderPage, OrderSearchCriteria searchCriteria, String uuid) {
        Page<Order> orders = bigOrderTableRepository.findAll(orderPage, searchCriteria);
        List<BigOrderTableDTO> orderList = new ArrayList<>();

        orders.forEach(o -> orderList.add(buildBigOrderTableDTO(o)));

        return new PageImpl<>(orderList, orders.getPageable(), orders.getTotalElements());
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
    public Optional<OrderAddressDtoResponse> updateAddress(OrderAddressDtoUpdate dtoUpdate, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(dtoUpdate.getId())
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Address address = order.getUbsUser().getAddress();
        if (address != null) {
            addressRepository.save(updateAddressOrderInfo(address, dtoUpdate));
            eventService.save(OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
            Optional<Address> optionalAddress = addressRepository.findById(address.getId());
            return optionalAddress.map(value -> modelMapper.map(value, OrderAddressDtoResponse.class));
        } else {
            throw new NotFoundOrderAddressException(NOT_FOUND_ADDRESS_BY_ORDER_ID + dtoUpdate.getId());
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<OrderInfoDto> getOrdersForUser(String uuid) {
        List<Order> orders = orderRepository.getAllOrdersOfUser(uuid);
        List<OrderInfoDto> dto = new ArrayList<>();
        orders.forEach(order -> dto.add(modelMapper.map(order, OrderInfoDto.class)));
        dto.forEach(data -> data.setOrderPrice(getPriceDetails(data.getId()).getTotalSumAmount()));
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderStatusPageDto getOrderStatusData(Long orderId, String languageCode) {
        CounterOrderDetailsDto prices = getPriceDetails(orderId);
        Optional<Order> order = orderRepository.findById(orderId);
        List<BagInfoDto> bagInfo = new ArrayList<>();
        List<Bag> bags = bagRepository.findAll();
        Language language = languageRepository.findLanguageByCode(languageCode);
        bags.forEach(bag -> {
            BagInfoDto bagInfoDto = modelMapper.map(bag, BagInfoDto.class);
            bagInfoDto.setName(bagTranslationRepository.findNameByBagId(bag.getId(), language.getId()).toString());
            bagInfo.add(bagInfoDto);
        });
        Address address = order.isPresent() ? order.get().getUbsUser().getAddress() : new Address();
        OrderStatus orderStatus = order.isPresent() ? order.get().getOrderStatus() : OrderStatus.CANCELLED;
        Optional<OrderStatusTranslation> orderStatusTranslation =
            orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(orderStatus.getNumValue(),
                language.getId());
        String statusTranslation =
            orderStatusTranslation.isPresent() ? orderStatusTranslation.get().getName() : "order status not found";
        String value = null;
        if (order.isPresent()) {
            value = orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(
                (long) order.get().getOrderPaymentStatus().getStatusValue(), language.getId());
        }
        UserInfoDto userInfoDto = ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(orderId);
        return OrderStatusPageDto.builder()
            .id(orderId)
            .dateFormed(order.map(Order::getOrderDate).orElse(null))
            .orderStatusesDto(getOrderStatusesTranslation(order.orElse(null), language.getId()))
            .orderPaymentStatusesDto(getOrderPaymentStatusesTranslation(language.getId()))
            .userInfoDto(userInfoDto)
            .addressCity(address.getCity())
            .addressStreet(address.getStreet())
            .addressDistrict(address.getDistrict())
            .addressEntranceNumber(Long.valueOf(address.getEntranceNumber()))
            .addressHouseCorpus(Long.valueOf(address.getHouseCorpus()))
            .addressHouseNumber(Long.valueOf(address.getHouseNumber()))
            .addressComment(address.getAddressComment()).bags(bagInfo)
            .addressRegion(address.getRegion())
            .orderStatus(order.map(Order::getOrderStatus).orElse(null))
            .orderStatusName(statusTranslation)
            .orderPaymentStatus(order.map(Order::getOrderPaymentStatus).orElse(null))
            .orderPaymentStatusName(Optional.of(Objects.requireNonNull(value)).orElse(null))
            .orderFullPrice(prices.getSumAmount())
            .orderDiscountedPrice(prices.getTotalSumAmount())
            .orderExportedPrice(prices.getSumExported()).orderExportedDiscountedPrice(prices.getTotalSumExported())
            .orderBonusDiscount(prices.getBonus()).orderCertificateTotalDiscount(prices.getCertificateBonus())
            .amountOfBagsOrdered(order.map(Order::getAmountOfBagsOrdered).orElse(null))
            .amountOfBagsExported(order.map(Order::getExportedQuantity).orElse(null))
            .amountOfBagsConfirmed(order.map(Order::getConfirmedQuantity).orElse(null))
            .numbersFromShop(order.map(Order::getAdditionalOrders).orElse(null))
            .certificates(prices.getCertificate())
            .paymentTableInfoDto(getPaymentInfo(orderId, prices.getSumAmount().longValue()))
            .exportDetailsDto(getOrderExportDetails(orderId))
            .employeePositionDtoRequest(getAllEmployeesByPosition(orderId))
            .comment(
                order.orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST)).getComment())
            .build();
    }

    /**
     * This is method which is get order translation statuses in two languages like:
     * ua and en.
     *
     * @param order      {@link Long}.
     * @param languageId {@link Long}.
     * @return {@link List}.
     *
     * @author Yuriy Bahlay.
     */
    private List<OrderStatusesTranslationDto> getOrderStatusesTranslation(Order order, Long languageId) {
        List<OrderStatusesTranslationDto> orderStatusesTranslationDtos = new ArrayList<>();
        List<OrderStatusTranslation> orderStatusTranslations =
            orderStatusTranslationRepository.getOrderStatusTranslationsByLanguageId(languageId);
        if (!orderStatusTranslations.isEmpty() && order != null) {
            for (OrderStatusTranslation orderStatusTranslation : orderStatusTranslations) {
                OrderStatusesTranslationDto orderStatusesTranslationDto = new OrderStatusesTranslationDto();
                setValueForOrderStatusIsCancelledOrDoneAsTrue(orderStatusTranslation, orderStatusesTranslationDto);
                orderStatusesTranslationDto.setTranslation(orderStatusTranslation.getName());
                if (!Objects.equals(OrderStatus.getConvertedEnumFromLongToEnum(orderStatusTranslation.getStatusId()),
                    "")) {
                    OrderStatus.getConvertedEnumFromLongToEnum(orderStatusTranslation.getStatusId());
                    orderStatusesTranslationDto
                        .setName(OrderStatus.getConvertedEnumFromLongToEnum(orderStatusTranslation.getStatusId()));
                }
                orderStatusesTranslationDtos.add(orderStatusesTranslationDto);
            }
        }
        return orderStatusesTranslationDtos;
    }

    /**
     * This is method which set value as true for orderStatus Cancelled or Done and
     * false to others order statuses.
     * 
     * @param orderStatusTranslation      {@link OrderStatusTranslation}.
     * @param orderStatusesTranslationDto {@link OrderStatusesTranslationDto}.
     *
     * @author Yuriy Bahlay.
     */
    private void setValueForOrderStatusIsCancelledOrDoneAsTrue(OrderStatusTranslation orderStatusTranslation,
        OrderStatusesTranslationDto orderStatusesTranslationDto) {
        orderStatusesTranslationDto
            .setAbleActualChange(OrderStatus.CANCELLED.getNumValue() == orderStatusTranslation.getStatusId()
                || OrderStatus.DONE.getNumValue() == orderStatusTranslation.getStatusId());
    }

    /**
     * This is method which is get order payment statuses translation.
     *
     * @param languageId {@link Long}.
     * @return {@link List}.
     *
     * @author Yuriy Bahlay.
     */
    private List<OrderPaymentStatusesTranslationDto> getOrderPaymentStatusesTranslation(Long languageId) {
        List<OrderPaymentStatusesTranslationDto> orderStatusesTranslationDtos = new ArrayList<>();
        List<OrderPaymentStatusTranslation> orderStatusPaymentTranslations = orderPaymentStatusTranslationRepository
            .getOrderStatusPaymentTranslationsByLanguageId(languageId);
        if (!orderStatusPaymentTranslations.isEmpty()) {
            for (OrderPaymentStatusTranslation orderStatusPaymentTranslation : orderStatusPaymentTranslations) {
                OrderPaymentStatusesTranslationDto translationDto = new OrderPaymentStatusesTranslationDto();
                translationDto.setTranslation(orderStatusPaymentTranslation.getTranslationValue());
                if (!Objects.equals(OrderPaymentStatus.getConvertedEnumFromLongToEnumAboutOrderPaymentStatus(
                    orderStatusPaymentTranslation.getOrderPaymentStatusId()), "")) {
                    translationDto.setName(OrderPaymentStatus.getConvertedEnumFromLongToEnumAboutOrderPaymentStatus(
                        orderStatusPaymentTranslation.getOrderPaymentStatusId()));
                }
                orderStatusesTranslationDtos.add(translationDto);
            }
        }
        return orderStatusesTranslationDtos;
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
    public List<OrderDetailInfoDto> setOrderDetail(List<UpdateOrderDetailDto> request, String language, String uuid) {
        final User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        collectEventsAboutSetOrderDetails(request, currentUser, language);
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
        orderRepository.save(order);
        return modelMapper.map(dto, new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType());
    }

    private void collectEventsAboutSetOrderDetails(List<UpdateOrderDetailDto> dto, User currentUser, String language) {
        Order order = orderRepository.findById(dto.get(0).getOrderId()).orElseThrow(
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Long languageId = languageRepository.findIdByCode(language);
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < dto.size(); i++) {
            Integer capacity = bagRepository.findCapacityById(dto.get(i).getBagId());
            StringBuilder bagTranslation = bagTranslationRepository.findNameByBagId(dto.get(i).getBagId(), languageId);

            if (order.getOrderStatus() == OrderStatus.ADJUSTMENT
                || order.getOrderStatus() == OrderStatus.CONFIRMED
                || order.getOrderStatus() == OrderStatus.FORMED
                || order.getOrderStatus() == OrderStatus.NOT_TAKEN_OUT) {
                Long confirmWasteWas =
                    updateOrderRepository.getConfirmWaste(dto.get(i).getOrderId(), Long.valueOf(dto.get(i).getBagId()));
                if (!confirmWasteWas.equals(Long.valueOf(dto.get(i).getConfirmedQuantity()))) {
                    if (i == 0) {
                        values.append(OrderHistory.CHANGE_ORDER_DETAILS + " ");
                    }
                    values.append(bagTranslation).append(" ").append(capacity).append(" л: ").append(confirmWasteWas)
                        .append(" шт на ").append(dto.get(i).getConfirmedQuantity()).append(" шт.");
                }
            } else if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE
                || order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF
                || order.getOrderStatus() == OrderStatus.DONE
                || order.getOrderStatus() == OrderStatus.CANCELLED) {
                Long exporterWasteWas = updateOrderRepository.getExporterWaste(dto.get(i).getOrderId(),
                    Long.valueOf(dto.get(i).getBagId()));
                if (!exporterWasteWas.equals(Long.valueOf(dto.get(i).getExportedQuantity()))) {
                    if (i == 0) {
                        values.append(OrderHistory.CHANGE_ORDER_DETAILS + " ");
                    }
                    values.append(bagTranslation).append(" ").append(capacity).append(" л: ").append(exporterWasteWas)
                        .append(" шт на ").append(dto.get(i).getExportedQuantity()).append(" шт.");
                }
            }
        }
        if (!dto.isEmpty()) {
            eventService.save(values.toString(),
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public CounterOrderDetailsDto getOrderSumDetails(Long id) {
        CounterOrderDetailsDto dto = getPriceDetails(id);
        Order order = orderRepository.getOrderDetails(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        final List<Payment> payment = paymentRepository.paymentInfo(id);
        double totalSumConfirmed = dto.getTotalSumConfirmed();
        double totalSumExported = dto.getTotalSumExported();
        updateStatus(payment, order, totalSumConfirmed, totalSumExported);
        return dto;
    }

    private CounterOrderDetailsDto getPriceDetails(Long id) {
        CounterOrderDetailsDto dto = new CounterOrderDetailsDto();
        Order order = orderRepository.getOrderDetails(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Bag> bag = bagRepository.findBagByOrderId(id);
        List<Certificate> currentCertificate = certificateRepository.findCertificate(id);

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
            if (!confirmedValues.isEmpty()) {
                sumConfirmed += confirmedValues.get(i) * bag.get(i).getPrice();
            }
            if (!exportedValues.isEmpty()) {
                sumExported += exportedValues.get(i) * bag.get(i).getPrice();
            }
        }

        if (!currentCertificate.isEmpty()) {
            totalSumAmount =
                (sumAmount - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                    + order.getPointsToUse()));
            totalSumConfirmed =
                (sumConfirmed
                    - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                        + order.getPointsToUse()));
            totalSumExported =
                (sumExported - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                    + order.getPointsToUse()));
            dto.setCertificateBonus(
                currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0).doubleValue());
            dto.setCertificate(
                currentCertificate.stream().map(Certificate::getCode).collect(Collectors.toList()));
        } else {
            dto.setCertificateBonus((double) 0);
            totalSumAmount = sumAmount - order.getPointsToUse();
            totalSumConfirmed = sumConfirmed - order.getPointsToUse();
            totalSumExported = sumExported - order.getPointsToUse();
        }
        if (confirmedValues.isEmpty()) {
            totalSumConfirmed = 0;
        }
        if (exportedValues.isEmpty()) {
            totalSumExported = 0;
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
    public OrderDetailStatusDto updateOrderDetailStatus(Long id, OrderDetailStatusRequestDto dto, String uuid) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Payment> payment = paymentRepository.paymentInfo(id);
        if (payment.isEmpty()) {
            throw new PaymentNotFoundException(PAYMENT_NOT_FOUND + id);
        }
        order.setComment(dto.getOrderComment());
        OrderStatus newStatus = OrderStatus.valueOf(dto.getOrderStatus());
        order.setOrderStatus(newStatus);
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (newStatus == OrderStatus.ADJUSTMENT) {
            notificationService.notifyCourierItineraryFormed(order);
            eventService.save(OrderHistory.ORDER_ADJUSTMENT,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (newStatus == OrderStatus.CONFIRMED) {
            eventService.save(OrderHistory.ORDER_CONFIRMED,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (newStatus == OrderStatus.NOT_TAKEN_OUT) {
            eventService.save(
                OrderHistory.ORDER_NOT_TAKEN_OUT + "  " + order.getComment() + "  "
                    + order.getImageReasonNotTakingBags(),
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (newStatus == OrderStatus.CANCELLED) {
            eventService.save(OrderHistory.ORDER_CANCELLED + "  " + order.getCancellationComment(),
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
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
        User user =
            userRepository.findUserByOrderId(orderId).orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND));
        return violationRepository.findByOrderId(orderId).map(v -> ViolationDetailInfoDto.builder()
            .orderId(orderId)
            .userName(user.getRecipientName())
            .violationLevel(v.getViolationLevel())
            .description(v.getDescription())
            .violationDate(v.getViolationDate())
            .build());
    }

    @Override
    @Transactional
    public void deleteViolation(Long id, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Optional<Violation> violationOptional = violationRepository.findByOrderId(id);
        if (violationOptional.isPresent()) {
            List<String> images = violationOptional.get().getImages();
            if (!images.isEmpty()) {
                for (int i = 0; i < images.size(); i++) {
                    fileService.delete(images.get(i));
                }
            }
            violationRepository.deleteById(violationOptional.get().getId());
            User user = violationOptional.get().getOrder().getUser();
            user.setViolations(userRepository.countTotalUsersViolations(user.getId()));
            userRepository.save(user);
            eventService.save(OrderHistory.DELETE_VIOLATION, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), violationOptional.get().getOrder());
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
    public ExportDetailsDto updateOrderExportDetails(Long id, ExportDetailsDtoRequest dto, String uuid) {
        final User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<ReceivingStation> receivingStation = receivingStationRepository.findAll();
        if (receivingStation.isEmpty()) {
            throw new ReceivingStationNotFoundException(RECEIVING_STATION_NOT_FOUND);
        }
        String str = dto.getExportedDate() + " " + dto.getExportedTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
        final String receivingStationValue = order.getReceivingStation();
        final LocalDateTime deliverFrom = order.getDeliverFrom();
        order.setDeliverFrom(dateTime);
        order.setReceivingStation(dto.getReceivingStation());
        orderRepository.save(order);
        collectEventsAboutOrderExportDetails(receivingStationValue, deliverFrom, order, currentUser);
        return buildExportDto(order, receivingStation);
    }

    /**
     * This is private method which collect's event for order export details.
     *
     * @param receivingStationValue {@link String}.
     * @param deliverFrom           {@link LocalDateTime}.
     * @param order                 {@link Order}.
     * @author Yuriy Bahlay.
     */
    private void collectEventsAboutOrderExportDetails(String receivingStationValue, LocalDateTime deliverFrom,
        Order order, User currentUser) {
        if (receivingStationValue != null || deliverFrom != null) {
            eventService.save(OrderHistory.UPDATE_EXPORT_DETAILS, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
        } else {
            eventService.save(OrderHistory.SET_EXPORT_DETAILS, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
        }
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
        MultipartFile image, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        return buildPaymentResponseDto(
            paymentRepository.save(buildPaymentEntity(order, paymentRequestDto, image, currentUser)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteManualPayment(Long paymentId, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not found"));
        if (payment.getImagePath() != null) {
            fileService.delete(payment.getImagePath());
        }
        paymentRepository.deletePaymentById(paymentId);
        eventService.save(OrderHistory.DELETE_PAYMENT_MANUALLY + paymentId,
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), payment.getOrder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManualPaymentResponseDto updateManualPayment(Long paymentId,
        ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
            () -> new PaymentNotFoundException(PAYMENT_NOT_FOUND + paymentId));
        Payment paymentUpdated = paymentRepository.save(changePaymentEntity(payment, paymentRequestDto, image));
        eventService.save(OrderHistory.UPDATE_PAYMENT_MANUALLY + paymentRequestDto.getPaymentId(),
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), payment.getOrder());
        return buildPaymentResponseDto(paymentUpdated);
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

    private Payment buildPaymentEntity(Order order, ManualPaymentRequestDto paymentRequestDto, MultipartFile image,
        User currentUser) {
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
        eventService.save(OrderHistory.ADD_PAYMENT_MANUALLY + paymentRequestDto.getPaymentId(),
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
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
    public void updatePositions(EmployeePositionDtoResponse dto, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(dto.getOrderId())
            .orElseThrow(
                () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + " " + dto.getOrderId()));
        List<EmployeeOrderPosition> employeeOrderPositions = new ArrayList<>();
        for (EmployeeOrderPositionDTO employeeOrderPositionDTO : dto.getEmployeeOrderPositionDTOS()) {
            String[] dtoFirstAndLastName = new String[0];
            try {
                dtoFirstAndLastName = employeeOrderPositionDTO.getName().split(" ");
            } catch (IndexOutOfBoundsException e) {
                throw new EmployeeNotFoundException(EMPLOYEE_DOESNT_EXIST);
            }
            Position position = positionRepository.findById(employeeOrderPositionDTO.getPositionId())
                .orElseThrow(() -> new PositionNotFoundException(POSITION_NOT_FOUND));
            Employee employee = employeeRepository.findByName(dtoFirstAndLastName[0], dtoFirstAndLastName[1])
                .orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND));
            Long oldEmployeePositionId =
                employeeOrderPositionRepository.findPositionOfEmployeeAssignedForOrder(employee.getId());
            if (nonNull(oldEmployeePositionId) && oldEmployeePositionId != 0 && oldEmployeePositionId != 2) {
                collectEventsAboutUpdatingEmployeesAssignedForOrder(oldEmployeePositionId, order, currentUser);
            }
            employeeOrderPositions.add(EmployeeOrderPosition.builder()
                .employee(employee)
                .position(position)
                .order(order)
                .build());
        }
        List<EmployeeOrderPosition> newList = employeeOrderPositionRepository.findAllByOrderId(dto.getOrderId());
        if (!newList.isEmpty()) {
            employeeOrderPositionRepository.deleteAll(newList);
        }
        employeeOrderPositionRepository.saveAll(employeeOrderPositions);
    }

    /**
     * This is private method which collect's event when managers will update
     * assigned.
     *
     * @param position {@link Long}.
     * @param order    {@link Order}.
     * @author Yuriy Bahlay.
     */
    private void collectEventsAboutUpdatingEmployeesAssignedForOrder(Long position, Order order, User currentUser) {
        if (position == 1) {
            eventService.save(OrderHistory.UPDATE_MANAGER_CALL,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 3) {
            eventService.save(OrderHistory.UPDATE_MANAGER_LOGIEST,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 4) {
            eventService.save(OrderHistory.UPDATE_MANAGER_CALL_PILOT,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 5) {
            eventService.save(OrderHistory.UPDATE_MANAGER_DRIVER,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
    }

    @Override
    public void updateUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Violation violation = violationRepository.findByOrderId(add.getOrderID())
            .orElseThrow(() -> new UnexistingOrderException(ORDER_HAS_NOT_VIOLATION));
        updateViolation(violation, add, multipartFiles);
        violationRepository.save(violation);
        eventService.save(OrderHistory.CHANGES_VIOLATION,
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), violation.getOrder());
    }

    private void updateViolation(Violation violation, AddingViolationsToUserDto add, MultipartFile[] multipartFiles) {
        violation.setViolationLevel(ViolationLevel.valueOf(add.getViolationLevel().toUpperCase()));
        violation.setDescription(add.getViolationDescription());
        if (!violation.getImages().isEmpty()) {
            List<String> images = violation.getImages();
            for (String image : images) {
                fileService.delete(image);
            }
            violation.setImages(null);
            images.clear();
            if (multipartFiles.length > 0) {
                setImages(multipartFiles, images);
                violation.setImages(images);
            }
        } else {
            if (multipartFiles.length > 0) {
                List<String> images = new LinkedList<>();
                setImages(multipartFiles, images);
                violation.setImages(images);
            }
        }
    }

    private List<String> setImages(MultipartFile[] multipartFiles, List<String> images) {
        for (MultipartFile multipartFile : multipartFiles) {
            images.add(fileService.upload(multipartFile));
        }
        return images;
    }

    @Override
    public ReasonNotTakeBagDto saveReason(Long orderId, String description, List<MultipartFile> images) {
        final Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        List<String> pictures = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null) {
                pictures.add(fileService.upload(image));
            } else {
                pictures.add(defaultImagePath);
            }
        }
        ReasonNotTakeBagDto dto = new ReasonNotTakeBagDto();
        dto.setImages(pictures);
        dto.setDescription(description);
        dto.setTime(LocalDate.now());
        dto.setCurrentUser(order.getUser().getRecipientName() + " " + order.getUser().getRecipientSurname());
        order.setImageReasonNotTakingBags(pictures);
        order.setReasonNotTakingBagDescription(description);
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void assignEmployeesWithThePositionsToTheOrder(AssignEmployeesForOrderDto dto, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(dto.getOrderId()).orElseThrow(
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + dto.getOrderId()));
        if (dto.getEmployeesList() != null) {
            for (int i = 0; i < dto.getEmployeesList().size(); i++) {
                AssignForOrderEmployee assignForOrderEmployee = dto.getEmployeesList().get(i);
                boolean isExistEmployee = employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(dto.getOrderId(),
                    assignForOrderEmployee.getEmployeeId());
                if (isExistEmployee) {
                    throw new EmployeeAlreadyAssignedForOrder(
                        EMPLOYEE_ALREADY_ASSIGNED + assignForOrderEmployee.getEmployeeId());
                }
                Employee employeeForAssigning = employeeRepository.findById(assignForOrderEmployee.getEmployeeId())
                    .orElseThrow(() -> new EmployeeNotFoundException(
                        EMPLOYEE_NOT_FOUND + assignForOrderEmployee.getEmployeeId()));
                Long positionForEmployee =
                    employeeRepository.findPositionForEmployee(assignForOrderEmployee.getEmployeeId())
                        .orElseThrow(() -> new PositionNotFoundException(POSITION_NOT_FOUND));
                if (positionForEmployee != 2) {
                    EmployeeOrderPosition employeeOrderPositions = EmployeeOrderPosition.builder()
                        .order(order)
                        .employee(employeeForAssigning)
                        .position(Position.builder().id(positionForEmployee).build())
                        .build();
                    employeeOrderPositionRepository.save(employeeOrderPositions);
                    collectsEventsAboutAssigningEmployees(positionForEmployee, currentUser, order);
                } else {
                    throw new EmployeeIsNotAssigned(ErrorMessage.EMPLOYEE_IS_NOT_ASSIGN);
                }
            }
        }
    }

    /**
     * This is private method which collect's event when managers will assign.
     *
     * @param position    {@link Long}.
     * @param currentUser {@link User}
     * @param order       {@link Order}.
     * @author Yuriy Bahlay.
     */
    private void collectsEventsAboutAssigningEmployees(Long position, User currentUser, Order order) {
        if (position == 1) {
            eventService.save(OrderHistory.ASSIGN_CALL_MANAGER,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 3) {
            eventService.save(OrderHistory.ASSIGN_LOGIEST,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 4) {
            eventService.save(OrderHistory.ASSIGN_CALL_PILOT,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 5) {
            eventService.save(OrderHistory.ASSIGN_DRIVER,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
    }

    private BigOrderTableDTO buildBigOrderTableDTO(Order order) {
        long paymentSum = order.getPayment().stream().mapToLong(Payment::getAmount).sum();
        int certificateSum = order.getCertificates().stream().mapToInt(Certificate::getPoints).sum();
        Address address = nonNull(order.getUbsUser().getAddress()) ? order.getUbsUser().getAddress() : new Address();
        return BigOrderTableDTO.builder()
            .id(order.getId())
            .orderStatus(nonNull(order.getOrderStatus()) ? order.getOrderStatus().name() : "-")
            .paymentStatus(nonNull(order.getOrderPaymentStatus()) ? order.getOrderPaymentStatus().name() : "-")
            .orderDate(getOrderDate(order))
            .paymentDate(getPaymentDate(order))
            .clientName(
                nonNull(order.getUbsUser()) ? order.getUbsUser().getFirstName() + " " + order.getUbsUser().getLastName()
                    : "-")
            .phoneNumber(nonNull(order.getUbsUser()) ? order.getUbsUser().getPhoneNumber() : "-")
            .email(nonNull(order.getUbsUser()) ? order.getUbsUser().getEmail() : "-")
            .senderName(nonNull(order.getUser())
                ? order.getUser().getRecipientName() + " " + order.getUser().getRecipientSurname()
                : "-")
            .senderPhone(nonNull(order.getUser()) ? order.getUser().getRecipientPhone() : "-")
            .senderEmail(nonNull(order.getUser()) ? order.getUser().getRecipientEmail() : "-")
            .violationsAmount(order.getUser().getViolations())
            .district(nonNull(address.getDistrict()) ? address.getDistrict() : "-")
            // need to implement field - область
            // need to implement field - населений пункт
            .address(getAddress(address))
            .commentToAddressForClient(nonNull(address.getAddressComment()) ? address.getAddressComment() : "-")
            .bagsAmount(getBagsAmount(order))
            .totalOrderSum(paymentSum)
            .orderCertificateCode(getCertificateCode(order))
            .orderCertificatePoints(getCertificatePoints(order))
            .amountDue((paymentSum - certificateSum) <= 0 ? 0 : paymentSum - certificateSum)
            .commentForOrderByClient(order.getComment())
            .payment(getPayment(order))
            .dateOfExport(getDateOfExport(order))
            .timeOfExport(getTimeOfExport(order))
            .idOrderFromShop(getIdOrderFromShop(order))
            .receivingStation(getReceivingStation(order))
            .responsibleManager(getEmployeeIdByIdPosition(order, 2L))
            .responsibleLogicMan(getEmployeeIdByIdPosition(order, 3L))
            .responsibleDriver(getEmployeeIdByIdPosition(order, 5L))
            .responsibleCaller(getEmployeeIdByIdPosition(order, 1L))
            .responsibleNavigator(getEmployeeIdByIdPosition(order, 4L))
            .commentsForOrder(getCommentsForOrder(order))
            .isBlocked(order.isBlocked())
            .blockedBy(getBlockedBy(order))
            .build();
    }

    private String getOrderDate(Order order) {
        return nonNull(order.getOrderDate()) ? order.getOrderDate().toString() : "-";
    }

    private String getPaymentDate(Order order) {
        return nonNull(order.getPayment())
            ? order.getPayment().stream().map(Payment::getOrderTime).collect(joining(", "))
            : "-";
    }

    private String getAddress(Address address) {
        if (nonNull(address.getStreet())
            && !address.getStreet().isBlank()) {
            StringBuilder addressInfo = new StringBuilder();
            addressInfo.append(address.getStreet());
            if (nonNull(address.getHouseNumber())
                && !address.getHouseNumber().isBlank()) {
                addressInfo.append(", " + address.getHouseNumber());
            }
            if (nonNull(address.getHouseCorpus())
                && !address.getHouseCorpus().isBlank()) {
                addressInfo.append(", " + address.getHouseCorpus());
            }
            if (nonNull(address.getEntranceNumber())
                && !address.getEntranceNumber().isBlank()) {
                addressInfo.append(", " + address.getEntranceNumber());
            }
            return addressInfo.toString();
        }
        return "-";
    }

    private Integer getBagsAmount(Order order) {
        return order.getAmountOfBagsOrdered().values().stream().reduce(0, Integer::sum);
    }

    private String getCertificateCode(Order order) {
        return nonNull(order.getCertificates()) ? order.getCertificates().stream().map(Certificate::getCode)
            .collect(joining("; ")) : "-";
    }

    private String getCertificatePoints(Order order) {
        return nonNull(order.getCertificates())
            ? order.getCertificates().stream().map(Certificate::getPoints).map(Objects::toString).collect(joining(", "))
            : "-";
    }

    private String getDateOfExport(Order order) {
        return nonNull(order.getDateOfExport()) ? order.getDateOfExport().toString() : "-";
    }

    private String getTimeOfExport(Order order) {
        return nonNull(order.getDeliverFrom()) && nonNull(order.getDeliverTo())
            ? String.format("%s-%s", order.getDeliverFrom().toLocalTime().toString(),
                order.getDeliverTo().toLocalTime().toString())
            : "-";
    }

    private String getReceivingStation(Order order) {
        return nonNull(order.getReceivingStation()) ? getStationId(order.getReceivingStation()) : "-";
    }

    private String getStationId(String receivingStation) {
        return receivingStationRepository.findByName(receivingStation).getId().toString();
    }

    private String getPayment(Order order) {
        return nonNull(order.getPayment()) ? order.getPayment().stream()
            .map(Payment::getAmount)
            .map(Objects::toString)
            .collect(joining(", ")) : "-";
    }

    private String getIdOrderFromShop(Order order) {
        return nonNull(order.getPayment()) ? order.getPayment().stream().map(Payment::getId).map(Objects::toString)
            .collect(joining(", ")) : "-";
    }

    private String getEmployeeIdByIdPosition(Order order, Long idPosition) {
        return nonNull(order.getEmployeeOrderPositions()) ? order.getEmployeeOrderPositions().stream()
            .filter(employeeOrderPosition -> employeeOrderPosition.getPosition().getId().equals(idPosition))
            .map(EmployeeOrderPosition::getEmployee)
            .map(e -> e.getId().toString())
            .reduce("", String::concat) : "-";
    }

    private String getCommentsForOrder(Order order) {
        return nonNull(order.getNote()) ? order.getNote() : "-";
    }

    private String getBlockedBy(Order order) {
        return nonNull(order.getBlockedByEmployee())
            ? String.format("%s %s", order.getBlockedByEmployee().getFirstName(),
                order.getBlockedByEmployee().getLastName())
            : "-";
    }

    /**
     * This is service method which is save adminComment.
     *
     * @param adminCommentDto {@link AdminCommentDto}.
     * @param uuid            {@link String}.
     * @author Yuriy Bahlay.
     */
    @Override
    public void saveAdminCommentToOrder(AdminCommentDto adminCommentDto, String uuid) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(adminCommentDto.getOrderId()).orElseThrow(
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + adminCommentDto.getOrderId()));
        order.setAdminComment(adminCommentDto.getAdminComment());
        orderRepository.save(order);
        eventService.save(OrderHistory.ADD_ADMIN_COMMENT, user.getRecipientName()
            + "  " + user.getRecipientSurname(), order);
    }

    /**
     * This is method updates eco id from the shop for order.
     *
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @param orderId      {@link Long}.
     * @param uuid         {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    @Transactional
    @Override
    public void updateEcoNumberForOrder(List<EcoNumberDto> ecoNumberDto, Long orderId, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        if (ecoNumberDto != null) {
            StringBuilder collectedValue = new StringBuilder();
            for (int i = 0; i < ecoNumberDto.size(); i++) {
                EcoNumberDto ecoNumber = ecoNumberDto.get(i);
                String oldNumber = orderRepository.findEcoNumberFromShop(ecoNumber.getOldEcoNumber(), orderId);
                if (oldNumber != null) {
                    orderRepository.setOrderAdditionalNumber(ecoNumber.getNewEcoNumber(), oldNumber, orderId);
                    collectedValue.append(
                        collectInfoAboutEcoNumberEventHistory(i, oldNumber, ecoNumber.getNewEcoNumber()));
                }
            }
            if (collectedValue.length() > 1) {
                eventService.save(collectedValue.toString(),
                    currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
            }
        }
    }

    /**
     * This is method which collects info about eco number for event history.
     *
     * @author Yuriy Bahlay.
     */
    private String collectInfoAboutEcoNumberEventHistory(int i, String oldNumber, String newEcoNumber) {
        StringBuilder values = new StringBuilder();
        if (i == 0) {
            values.append(OrderHistory.CHANGES_ECO_NUMBER);
        }
        if (i > 0) {
            values.append(";");
        }
        values.append(OrderHistory.FROM);
        values.append(oldNumber);
        values.append(OrderHistory.TO);
        values.append(newEcoNumber);
        return values.toString();
    }
}