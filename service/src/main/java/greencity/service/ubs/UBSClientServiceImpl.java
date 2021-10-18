package greencity.service.ubs;

import com.liqpay.LiqPay;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.*;
import greencity.entity.enums.*;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.service.PhoneNumberFormatterService;
import greencity.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static greencity.constant.ErrorMessage.*;

/**
 * Implementation of {@link UBSClientService}.
 */
@Service
@RequiredArgsConstructor
public class UBSClientServiceImpl implements UBSClientService {
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSuserRepository ubsUserRepository;
    private final BagTranslationRepository bagTranslationRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepo;
    private final RestClient restClient;
    private final PaymentRepository paymentRepository;
    private final PhoneNumberFormatterService phoneNumberFormatterService;
    private final EncryptionUtil encryptionUtil;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final UBSManagementServiceImpl ubsManagementService;
    private final LocationTranslationRepository locationTranslationRepository;
    private final LiqPay liqPay;
    @PersistenceContext
    private final EntityManager entityManager;
    @Value("${fondy.payment.key}")
    private String fondyPaymentKey;
    @Value("${merchant.id}")
    private String merchantId;
    @Value("${liqpay.public.key}")
    private String publicKey;
    @Value("${liqpay.private.key}")
    private String privateKey;
    private final EventService eventService;

    @Override
    @Transactional
    public void validatePayment(PaymentResponseDto dto) {
        if (dto.getResponseStatus().equals("failure")) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
        if (!encryptionUtil.checkIfResponseSignatureIsValid(dto, fondyPaymentKey)) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
        String[] ids = dto.getOrderId().split("_");
        Order order = orderRepository.findById(Long.valueOf(ids[0]))
            .orElseThrow(() -> new PaymentValidationException(PAYMENT_VALIDATION_ERROR));
        if (dto.getOrderStatus().equals("approved")) {
            Payment orderPayment = modelMapper.map(dto, Payment.class);
            orderPayment.setPaymentStatus(PaymentStatus.PAID);
            orderPayment.setOrder(order);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
            eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM + orderPayment.getPaymentId(),
                OrderHistory.SYSTEM, order);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPointsAndAllBagsDto getFirstPageData(String uuid) {
        int currentUserPoints = 0;
        User user = userRepository.findByUuid(uuid);
        Location lastLocation = user.getLastLocation();
        currentUserPoints = user.getCurrentPoints();
        List<BagTranslationDto> btdList = bagTranslationRepository.findAll()
            .stream()
            .map(this::buildBagTranslationDto)
            .collect(Collectors.toList());
        return new UserPointsAndAllBagsDto(btdList, lastLocation.getMinAmountOfBigBags(), currentUserPoints);
    }

    private BagTranslationDto buildBagTranslationDto(BagTranslation bt) {
        return BagTranslationDto.builder()
            .id(bt.getBag().getId())
            .capacity(bt.getBag().getCapacity())
            .price(bt.getBag().getFullPrice())
            .name(bt.getName())
            .code(bt.getLanguage().getCode())
            .locationId(bt.getBag().getLocation().getId())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PersonalDataDto getSecondPageData(String uuid) {
        User currentUser = createUserByUuidIfUserDoesNotExist(uuid);
        return modelMapper.map(currentUser, PersonalDataDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CertificateDto checkCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code)
            .orElseThrow(() -> new CertificateNotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + code));

        return new CertificateDto(certificate.getCertificateStatus().toString(), certificate.getPoints(),
            certificate.getExpirationDate());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @Transactional
    public String saveFullOrderToDB(OrderResponseDto dto, String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        Location location = locationRepository.getOne(currentUser.getLastLocation().getId());
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());

        Map<Integer, Integer> amountOfBagsOrderedMap = new HashMap<>();

        int sumToPay = formBagsToBeSavedAndCalculateOrderSum(amountOfBagsOrderedMap, dto.getBags(),
            location.getMinAmountOfBigBags());

        if (sumToPay < dto.getPointsToUse()) {
            throw new IncorrectValueException(AMOUNT_OF_POINTS_BIGGER_THAN_SUM);
        } else {
            sumToPay -= dto.getPointsToUse();
        }

        Order order = modelMapper.map(dto, Order.class);
        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPay);

        UBSuser userData;
        userData = formUserDataToBeSaved(dto.getPersonalData(), currentUser);

        Address address = addressRepo.findById(dto.getAddressId()).orElseThrow(() -> new NotFoundOrderAddressException(
            ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + dto.getAddressId()));

        checkIfAddressHasBeenDeleted(address);

        checkAddressUser(address, currentUser);
        address.setAddressStatus(AddressStatus.IN_ORDER);

        userData.setAddress(address);

        if (userData.getAddress().getComment() == null) {
            userData.getAddress().setComment(dto.getPersonalData().getAddressComment());
        }

        order = formAndSaveOrder(order, orderCertificates, amountOfBagsOrderedMap, userData, currentUser, sumToPay);

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        PaymentRequestDto paymentRequestDto = formPaymentRequest(order.getId(), sumToPay);
        String html = restClient.getDataFromFondy(paymentRequestDto);

        Document doc = Jsoup.parse(html);

        Elements links = doc.select("a[href]");
        System.out.println(links.attr("href"));
        eventService.save(OrderHistory.ORDER_FORMED, OrderHistory.CLIENT, order);
        return links.attr("href");
    }

    private void checkIfAddressHasBeenDeleted(Address address) {
        if (address.getAddressStatus().equals(AddressStatus.DELETED)) {
            throw new NotFoundOrderAddressException(
                ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkAddressUser(Address address, User user) {
        if (!address.getUser().equals(user)) {
            throw new NotFoundOrderAddressException(
                ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkIfUserHaveEnoughPoints(Integer i1, Integer i2) {
        if (i1 < i2) {
            throw new IncorrectValueException(USER_DONT_HAVE_ENOUGH_POINTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto findAllAddressesForCurrentOrder(String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        Long id = userRepository.findByUuid(uuid).getId();
        List<AddressDto> addressDtoList = addressRepo.findAllByUserId(id)
            .stream()
            .sorted(Comparator.comparing(Address::getId))
            .filter(u -> u.getAddressStatus() != AddressStatus.DELETED)
            .map(u -> modelMapper.map(u, AddressDto.class))
            .collect(Collectors.toList());
        return new OrderWithAddressesResponseDto(addressDtoList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto saveCurrentAddressForOrder(OrderAddressDtoRequest dtoRequest, String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        List<Address> addresses = addressRepo.findAllByUserId(userRepository.findByUuid(uuid).getId());
        if (addresses != null) {
            boolean exist = addresses.stream()
                .filter(a -> !a.getAddressStatus().equals(AddressStatus.DELETED))
                .map(a -> modelMapper.map(a, OrderAddressDtoRequest.class))
                .anyMatch(d -> d.equals(dtoRequest));

            if (exist) {
                throw new AddressAlreadyExistException(ADDRESS_ALREADY_EXISTS);
            }

            addresses.forEach(u -> {
                u.setActual(false);
                addressRepo.save(u);
            });
        }
        Address address;
        Address forOrderAfterUpdate;
        if (dtoRequest.getId() != 0) {
            address = addressRepo.findById(dtoRequest.getId()).orElse(null);
            forOrderAfterUpdate = modelMapper.map(dtoRequest, Address.class);
            if (address != null && address.getAddressStatus().equals(AddressStatus.DELETED)) {
                address = null;
            }
        } else {
            forOrderAfterUpdate = null;
            address = null;
        }

        if (address == null || !address.getUser().equals(userRepository.findByUuid(uuid))) {
            address = modelMapper.map(dtoRequest, Address.class);
            address.setId(null);
            address.setUser(userRepository.findByUuid(uuid));
            address.setActual(true);
            address.setAddressStatus(AddressStatus.NEW);
        } else {
            if (address.getAddressStatus().equals(AddressStatus.IN_ORDER)) {
                forOrderAfterUpdate.setId(null);
                forOrderAfterUpdate.setActual(true);
                forOrderAfterUpdate.setUser(address.getUser());
                forOrderAfterUpdate.setAddressStatus(address.getAddressStatus());
                forOrderAfterUpdate.setComment(address.getComment());

                address.getUbsUsers().forEach(u -> u.setAddress(addressRepo.save(forOrderAfterUpdate)));
            }
            address = modelMapper.map(dtoRequest, Address.class);
            address.setUser(userRepository.findByUuid(uuid));
            address.setActual(true);
            address.setAddressStatus(AddressStatus.NEW);
        }
        addressRepo.save(address);
        return findAllAddressesForCurrentOrder(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto deleteCurrentAddressForOrder(Long addressId, String uuid) {
        Address address = addressRepo.findById(addressId).orElseThrow(
            () -> new NotFoundOrderAddressException(ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));
        if (!address.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new NotFoundOrderAddressException(ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId);
        }
        address.setAddressStatus(AddressStatus.DELETED);
        addressRepo.save(address);
        return findAllAddressesForCurrentOrder(uuid);
    }

    private void formAndSaveUser(User currentUser, int pointsToUse, Order order) {
        currentUser.getOrders().add(order);
        if (pointsToUse != 0) {
            currentUser.setCurrentPoints(currentUser.getCurrentPoints() - pointsToUse);
            currentUser.getChangeOfPointsList().add(ChangeOfPoints.builder()
                .amount(-pointsToUse)
                .date(order.getOrderDate())
                .user(currentUser)
                .order(order)
                .build());
        }
        userRepository.save(currentUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderClientDto> getAllOrdersDoneByUser(String uuid) {
        return orderRepository.getAllOrdersOfUser(uuid).stream()
            .sorted(Comparator.comparing(Order::getOrderDate))
            .map(order -> modelMapper.map(order, OrderClientDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderClientDto cancelFormedOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new OrderNotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (order.getOrderStatus() == OrderStatus.FORMED) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.getUser().setCurrentPoints(order.getUser().getCurrentPoints() + order.getPointsToUse());
            order.setPointsToUse(0);
            order.setAmountOfBagsOrdered(Collections.emptyMap());
            order.getPayment().forEach(p -> p.setAmount(0L));
            order = orderRepository.save(order);
            return modelMapper.map(order, OrderClientDto.class);
        } else {
            throw new BadOrderStatusRequestException(ErrorMessage.BAD_ORDER_STATUS_REQUEST + order.getOrderStatus());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MakeOrderAgainDto makeOrderAgain(Locale locale, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE
            || order.getOrderStatus() == OrderStatus.CONFIRMED
            || order.getOrderStatus() == OrderStatus.DONE) {
            List<BagTranslation> bags = bagTranslationRepository.findAllByLanguageOrder(locale.getLanguage(), orderId);
            return buildOrderBagDto(order, bags);
        } else {
            throw new BadOrderStatusRequestException(ErrorMessage.BAD_ORDER_STATUS_REQUEST + order.getOrderStatus());
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<OrderStatusPageDto> getOrdersForUser(String uuid, Long languageId) {
        List<Order> orders = orderRepository.getAllOrdersOfUser(uuid);
        List<OrderStatusPageDto> dto = new ArrayList<>();
        orders.forEach(order -> dto.add(ubsManagementService.getOrderStatusData(order.getId(), languageId)));
        return dto;
    }

    private MakeOrderAgainDto buildOrderBagDto(Order order, List<BagTranslation> bags) {
        List<BagOrderDto> bagOrderDtoList = new ArrayList<>();
        for (BagTranslation bag : bags) {
            bagOrderDtoList.add(BagOrderDto.builder()
                .bagId(bag.getBag().getId())
                .name(bag.getName())
                .capacity(bag.getBag().getCapacity())
                .price(bag.getBag().getPrice())
                .bagAmount(order.getAmountOfBagsOrdered().get(bag.getBag().getId()))
                .build());
        }
        return MakeOrderAgainDto.builder()
            .orderId(order.getId())
            .orderAmount(order.getPayment().stream()
                .flatMapToLong(p -> LongStream.of(p.getAmount()))
                .reduce(Long::sum).orElse(0L))
            .bagOrderDtoList(bagOrderDtoList)
            .build();
    }

    /**
     * Method returns info about user, ubsUser and user violations by order orderId.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link UserInfoDto};
     * @author Rusanovscaia Nadejda
     */
    @Override
    @Transactional
    public UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return UserInfoDto.builder()
            .customerName(order.getUser().getRecipientName())
            .customerPhoneNumber(order.getUser().getRecipientPhone())
            .customerEmail(order.getUser().getRecipientEmail())
            .totalUserViolations(userRepository.countTotalUsersViolations(order.getUser().getId()))
            .recipientName(order.getUbsUser().getFirstName() + " " + order.getUbsUser().getLastName())
            .recipientPhoneNumber(order.getUbsUser().getPhoneNumber())
            .recipientEmail(order.getUbsUser().getEmail())
            .userViolationForCurrentOrder(
                userRepository.checkIfUserHasViolationForCurrentOrder(order.getUser().getId(), order.getId()))
            .build();
    }

    /**
     * Method updates ubs_user information order in order.
     *
     * @param dtoUpdate of {@link UbsCustomersDtoUpdate} ubs_user_id;
     * @return {@link UbsCustomersDto};
     * @author Rusanovscaia Nadejda
     */
    @Override
    public UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate) {
        Optional<UBSuser> optionalUbsUser = ubsUserRepository.findById(dtoUpdate.getId());
        if (optionalUbsUser.isEmpty()) {
            throw new UBSuserNotFoundException(RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST + dtoUpdate.getId());
        }
        UBSuser user = optionalUbsUser.get();
        ubsUserRepository.save(updateRecipientDataInOrder(user, dtoUpdate));
        return UbsCustomersDto.builder()
            .name(user.getFirstName() + " " + user.getLastName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

    private UBSuser updateRecipientDataInOrder(UBSuser ubSuser, UbsCustomersDtoUpdate dto) {
        ubSuser.setFirstName(dto.getRecipientName().split(" ")[0]);
        ubSuser.setLastName(dto.getRecipientName().split(" ")[1]);
        ubSuser.setPhoneNumber(dto.getRecipientPhoneNumber());
        ubSuser.setEmail(dto.getRecipientEmail());
        return ubSuser;
    }

    private Order formAndSaveOrder(Order order, Set<Certificate> orderCertificates,
        Map<Integer, Integer> amountOfBagsOrderedMap, UBSuser userData,
        User currentUser, int sumToPay) {
        order.setOrderStatus(OrderStatus.FORMED);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        order.setCertificates(orderCertificates);
        order.setAmountOfBagsOrdered(amountOfBagsOrderedMap);
        order.setUbsUser(userData);
        order.setUser(currentUser);

        Payment payment = Payment.builder()
            .amount((long) (sumToPay * 100))
            .orderStatus("created")
            .currency("UAH")
            .paymentStatus(PaymentStatus.UNPAID)
            .order(order).build();
        if (order.getPayment() != null) {
            order.getPayment().add(payment);
        } else {
            ArrayList<Payment> arrayOfPayments = new ArrayList<>();
            arrayOfPayments.add(payment);
            order.setPayment(arrayOfPayments);
        }
        orderRepository.save(order);
        return order;
    }

    private PaymentRequestDto formPaymentRequest(Long orderId, int sumToPay) {
        Order testOrder = orderRepository.findById(orderId).orElseThrow(null);
        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
            .merchantId(Integer.parseInt(merchantId))
            .orderId(orderId + "_"
                + testOrder.getPayment().get(testOrder.getPayment().size() - 1).getId().toString())
            .orderDescription("ubs courier")
            .currency("UAH")
            .amount(sumToPay * 100).build();

        paymentRequestDto.setSignature(encryptionUtil
            .formRequestSignature(paymentRequestDto, fondyPaymentKey, merchantId));

        return paymentRequestDto;
    }

    private UBSuser formUserDataToBeSaved(PersonalDataDto dto, User currentUser) {
        UBSuser ubsUserFromDatabaseById = null;
        if (dto.getId() != null) {
            ubsUserFromDatabaseById =
                ubsUserRepository.findById(dto.getId())
                    .orElseThrow(() -> new IncorrectValueException(THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST
                        + dto.getId()));
        }
        UBSuser mappedFromDtoUser = modelMapper.map(dto, UBSuser.class);
        mappedFromDtoUser.setUser(currentUser);
        mappedFromDtoUser.setPhoneNumber(
            phoneNumberFormatterService.getE164PhoneNumberFormat(mappedFromDtoUser.getPhoneNumber()));
        if (mappedFromDtoUser.getId() == null || !mappedFromDtoUser.equals(ubsUserFromDatabaseById)) {
            mappedFromDtoUser.setId(null);
            ubsUserRepository.save(mappedFromDtoUser);
            currentUser.getUbsUsers().add(mappedFromDtoUser);
            if (dto.getEmail() != null && dto.getEmail().equals(currentUser.getRecipientEmail())) {
                if (currentUser.getRecipientSurname() == null) {
                    currentUser.setRecipientSurname(dto.getLastName());
                }
                if (currentUser.getRecipientPhone() == null) {
                    currentUser.setRecipientPhone(dto.getPhoneNumber());
                }
            }
            userRepository.save(currentUser);
            return mappedFromDtoUser;
        } else {
            return ubsUserFromDatabaseById;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderPaymentDetailDto getOrderPaymentDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return buildOrderPaymentDetailDto(order);
    }

    private OrderPaymentDetailDto buildOrderPaymentDetailDto(Order order) {
        int certificatePoints = order.getCertificates().stream()
            .flatMapToInt(c -> IntStream.of(c.getPoints()))
            .reduce(Integer::sum).orElse(0) * 100;
        int pointsToUse = order.getPointsToUse() * 100;
        long amount = order.getPayment().stream()
            .flatMapToLong(p -> LongStream.of(p.getAmount()))
            .reduce(Long::sum).orElse(0);
        String currency = order.getPayment().isEmpty() ? "UAH" : order.getPayment().get(0).getCurrency();
        return OrderPaymentDetailDto.builder()
            .amount(amount != 0 ? amount + certificatePoints + pointsToUse : 0)
            .certificates(-certificatePoints)
            .pointsToUse(-pointsToUse)
            .amountToPay(amount)
            .currency(currency)
            .build();
    }

    private int formCertificatesToBeSavedAndCalculateOrderSum(OrderResponseDto dto, Set<Certificate> orderCertificates,
        Order order, int sumToPay) {
        if (dto.getCertificates() != null) {
            boolean tooManyCertificates = false;
            for (String temp : dto.getCertificates()) {
                if (tooManyCertificates) {
                    throw new TooManyCertificatesEntered(TOO_MANY_CERTIFICATES);
                }
                Certificate certificate = certificateRepository.findById(temp).orElseThrow(
                    () -> new CertificateNotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + temp));
                validateCertificate(certificate);
                certificate.setOrder(order);
                orderCertificates.add(certificate);
                sumToPay -= certificate.getPoints();
                if (dontSendLinkToFondyIf(sumToPay, certificate, dto)) {
                    sumToPay = 0;
                    tooManyCertificates = true;
                }
            }
        }
        return sumToPay;
    }

    private boolean dontSendLinkToFondyIf(int sumToPay, Certificate certificate, OrderResponseDto orderResponseDto) {
        if (sumToPay <= 0) {
            certificate.setCertificateStatus(CertificateStatus.USED);
            if (orderResponseDto.getPointsToUse() > 0) {
                throw new IncorrectValueException(SUM_IS_COVERED_BY_CERTIFICATES);
            }
            return true;
        }
        return false;
    }

    private int formBagsToBeSavedAndCalculateOrderSum(
        Map<Integer, Integer> map, List<BagDto> bags, Long minAmountOfBigBags) {
        int sumToPay = 0;
        int bigBagCounter = 0;
        for (BagDto temp : bags) {
            Bag bag = bagRepository.findById(temp.getId())
                .orElseThrow(() -> new BagNotFoundException(BAG_NOT_FOUND + temp.getId()));
            if (bag.getCapacity() >= 120) {
                bigBagCounter += temp.getAmount();
            }
            sumToPay += bag.getPrice() * temp.getAmount();
            map.put(temp.getId(), temp.getAmount());
        }
        if (minAmountOfBigBags > bigBagCounter) {
            throw new NotEnoughBagsException(NOT_ENOUGH_BIG_BAGS_EXCEPTION + minAmountOfBigBags);
        }
        return sumToPay;
    }

    private void validateCertificate(Certificate certificate) {
        if (certificate.getCertificateStatus() == CertificateStatus.NEW) {
            throw new CertificateIsNotActivated(CERTIFICATE_IS_NOT_ACTIVATED + certificate.getCode());
        } else if (certificate.getCertificateStatus() == CertificateStatus.USED) {
            throw new CertificateIsUsedException(CERTIFICATE_IS_USED + certificate.getCode());
        } else {
            if (LocalDate.now().isAfter(certificate.getExpirationDate())) {
                throw new CertificateExpiredException(CERTIFICATE_EXPIRED + certificate.getCode());
            }
        }
    }

    @Override
    public AllPointsUserDto findAllCurrentPointsForUser(String uuid) {
        List<Order> allByUserId = orderRepository.findAllOrdersByUserUuid(uuid);
        if (allByUserId.isEmpty()) {
            throw new OrderNotFoundException(ErrorMessage.ORDERS_FOR_UUID_NOT_EXIST);
        }
        List<PointsForUbsUserDto> bonusForUbsUser = allByUserId.stream()
            .filter(a -> a.getPointsToUse() != 0)
            .map(u -> modelMapper.map(u, PointsForUbsUserDto.class))
            .collect(Collectors.toList());
        AllPointsUserDto allBonusesForUserDto = new AllPointsUserDto();
        allBonusesForUserDto.setUserBonuses(sumUserPoints(allByUserId));
        allBonusesForUserDto.setUbsUserBonuses(bonusForUbsUser);
        return allBonusesForUserDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EventDto> getAllEventsForOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }
        List<Event> orderEvents = eventRepository.findAllEventsByOrderId(orderId);
        if (orderEvents.isEmpty()) {
            throw new EventsNotFoundException(ErrorMessage.EVENTS_NOT_FOUND_EXCEPTION + orderId);
        }
        return orderEvents
            .stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(Collectors.toList());
    }

    private Integer sumUserPoints(List<Order> allByUserId) {
        return allByUserId.stream().map(Order::getPointsToUse).reduce(0, (x, y) -> x + y);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public UserProfileDto saveProfileData(String uuid, UserProfileDto userProfileDto) {
        createUserByUuidIfUserDoesNotExist(uuid);
        User user = userRepository.findByUuid(uuid);
        setUserData(user, userProfileDto);
        AddressDto addressDto = userProfileDto.getAddressDto();
        Address address = modelMapper.map(addressDto, Address.class);
        address.setUser(user);
        Address savedAddress = addressRepo.save(address);
        User savedUser = userRepository.save(user);
        createUbsUserBasedUserProfileData(userProfileDto, savedUser, savedAddress);
        AddressDto mapperAddressDto = modelMapper.map(savedAddress, AddressDto.class);
        UserProfileDto mappedUserProfileDto = modelMapper.map(savedUser, UserProfileDto.class);
        mappedUserProfileDto.setAddressDto(mapperAddressDto);
        return mappedUserProfileDto;
    }

    @Override
    public UserProfileDto getProfileData(String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        User user = userRepository.findByUuid(uuid);
        List<Address> allAddress = addressRepo.findAllByUserId(user.getId());
        UserProfileDto userProfileDto = modelMapper.map(user, UserProfileDto.class);
        for (Address address : allAddress) {
            AddressDto addressDto = modelMapper.map(address, AddressDto.class);
            setAddressData(address, addressDto);
            userProfileDto.setAddressDto(addressDto);
        }
        return userProfileDto;
    }

    private User setUserData(User user, UserProfileDto userProfileDto) {
        user.setRecipientName(userProfileDto.getRecipientName());
        user.setRecipientSurname(userProfileDto.getRecipientSurname());
        user.setRecipientPhone(
            phoneNumberFormatterService.getE164PhoneNumberFormat(userProfileDto.getRecipientPhone()));
        user.setRecipientEmail(userProfileDto.getRecipientEmail());
        return user;
    }

    private Address setAddressData(Address address, AddressDto addressDto) {
        address.setCity(addressDto.getCity());
        address.setStreet(addressDto.getStreet());
        address.setDistrict(addressDto.getDistrict());
        address.setHouseNumber(addressDto.getHouseNumber());
        address.setEntranceNumber(addressDto.getEntranceNumber());
        address.setHouseCorpus(addressDto.getHouseCorpus());

        return address;
    }

    private User createUserByUuidIfUserDoesNotExist(String uuid) {
        User user = userRepository.findByUuid(uuid);
        if (user == null) {
            UbsCustomersDto ubsCustomersDto = restClient.findUserByUUid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Such UUID have not been found"));
            return userRepository.save(User.builder().currentPoints(0).violations(0).uuid(uuid)
                .recipientEmail(ubsCustomersDto.getEmail()).recipientName(ubsCustomersDto.getName()).build());
        }
        return user;
    }

    @Override
    public void markUserAsDeactivated(Long id) {
        User user =
            userRepository.findById(id).orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        restClient.markUserDeactivated(user.getUuid());
    }

    @Override
    public OrderCancellationReasonDto getOrderCancellationReason(final Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return OrderCancellationReasonDto.builder()
            .cancellationReason(order.getCancellationReason())
            .cancellationComment(order.getCancellationComment())
            .build();
    }

    @Override
    public OrderCancellationReasonDto updateOrderCancellationReason(long id, OrderCancellationReasonDto dto) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        order.setCancellationReason(dto.getCancellationReason());
        order.setCancellationComment(dto.getCancellationComment());
        order.setId(id);
        orderRepository.save(order);
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LocationResponseDto> getAllLocations(String userUuid) {
        User user = userRepository.findByUuid(userUuid);
        List<Location> locations = locationRepository.findAll();
        List<LocationTranslation> locationTranslations = locationTranslationRepository.findAll();
        Location lastOrderLocation = user.getLastLocation();

        if (lastOrderLocation != null) {
            locations.remove(lastOrderLocation);
            locations.add(0, lastOrderLocation);
        }
        return buildLocationResponseList(locationTranslations);
    }

    private List<LocationResponseDto> buildLocationResponseList(List<LocationTranslation> locations) {
        return locations.stream()
            .map(a -> buildLocationResponseDto(a))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNewLastOrderLocation(String userUuid, LocationIdDto locationIdDto) {
        User currentUser = userRepository.findByUuid(userUuid);
        Location location = locationRepository.findById(locationIdDto.getLocationId())
            .orElseThrow(() -> new LocationNotFoundException(LOCATION_DOESNT_FOUND));
        currentUser.setLastLocation(location);

        userRepository.save(currentUser);
    }

    private LocationResponseDto buildLocationResponseDto(LocationTranslation locationTranslation) {
        return LocationResponseDto.builder()
            .id(locationTranslation.getLocation().getId())
            .name(locationTranslation.getLocationName())
            .languageCode(locationTranslation.getLanguage().getCode())
            .build();
    }

    @Override
    public PersonalDataDto convertUserProfileDtoToPersonalDataDto(UserProfileDto userProfileDto) {
        PersonalDataDto personalDataDto = PersonalDataDto.builder().firstName(userProfileDto.getRecipientName())
            .lastName(userProfileDto.getRecipientSurname())
            .email(userProfileDto.getRecipientEmail()).phoneNumber(userProfileDto.getRecipientPhone()).build();

        Long ubsUserId = ubsUserRepository.findByEmail(userProfileDto.getRecipientEmail())
            .map(UBSuser::getId).orElse(null);

        personalDataDto.setId(ubsUserId);
        return personalDataDto;
    }

    @Override
    public UBSuser createUbsUserBasedUserProfileData(UserProfileDto userProfileDto, User savedUser,
        Address savedAddress) {
        UBSuser ubSuser = formUserDataToBeSaved(convertUserProfileDtoToPersonalDataDto(userProfileDto), savedUser);
        ubSuser.setAddress(savedAddress);
        return ubsUserRepository.save(ubSuser);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @Transactional
    public String saveFullOrderToDBFromLiqPay(OrderResponseDto dto, String uuid) {
        User currentUser = userRepository.findByUuid(uuid);

        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());

        Map<Integer, Integer> amountOfBagsOrderedMap = new HashMap<>();

        int sumToPay = formBagsToBeSavedAndCalculateOrderSum(amountOfBagsOrderedMap, dto.getBags(),
            currentUser.getLastLocation().getMinAmountOfBigBags());

        if (sumToPay < dto.getPointsToUse()) {
            throw new IncorrectValueException(AMOUNT_OF_POINTS_BIGGER_THAN_SUM);
        } else {
            sumToPay -= dto.getPointsToUse();
        }

        Order order = modelMapper.map(dto, Order.class);
        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPay);

        UBSuser userData = formUserDataToBeSaved(dto.getPersonalData(), currentUser);

        Address address = addressRepo.findById(dto.getAddressId()).orElseThrow(() -> new NotFoundOrderAddressException(
            ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + dto.getAddressId()));

        checkIfAddressHasBeenDeleted(address);

        checkAddressUser(address, currentUser);
        address.setAddressStatus(AddressStatus.IN_ORDER);

        userData.setAddress(address);

        if (userData.getAddress().getComment() == null) {
            userData.getAddress().setComment(dto.getPersonalData().getAddressComment());
        }

        order = formAndSaveOrder(order, orderCertificates, amountOfBagsOrderedMap, userData, currentUser, sumToPay);

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        PaymentRequestDtoLiqPay paymentRequestDto = formLiqPayPaymentRequest(order.getId(), sumToPay);

        eventService.save(OrderHistory.ORDER_FORMED, OrderHistory.CLIENT, order);

        return liqPay.cnb_form(restClient.getDataFromLiqPay(paymentRequestDto));
    }

    private PaymentRequestDtoLiqPay formLiqPayPaymentRequest(Long orderId, int sumToPay) {
        Order order = orderRepository.findById(orderId).orElseThrow(null);

        return PaymentRequestDtoLiqPay.builder()
            .publicKey(publicKey)
            .version(3)
            .action("pay")
            .amount(sumToPay)
            .currency("UAH")
            .description("ubs courier")
            .orderId(orderId + "_" + order.getPayment()
                .get(order.getPayment().size() - 1).getId().toString())
            .language("en")
            .paytypes("card")
            .resultUrl("http://localhost:8050/ubs/receiveLiqPayPayment")
            .build();
    }

    @Override
    public void validateLiqPayPayment(PaymentResponseDtoLiqPay dto) {
        if (!encryptionUtil.formingResponseSignatureLiqPay(dto.getData(), privateKey)
            .equals(dto.getSignature())) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
    }

    @Override
    public OrderStatusPageDto getOrderInfoForSurcharge(Long orderId, Long languageId) {
        OrderStatusPageDto orderStatusPageDto = ubsManagementService.getOrderStatusData(orderId, languageId);
        Map<Integer, Integer> amountBagsOrder = orderStatusPageDto.getAmountOfBagsOrdered();
        Map<Integer, Integer> amountBagsOrderExported = orderStatusPageDto.getAmountOfBagsExported();
        amountBagsOrderExported.replaceAll((id, quantity) -> quantity = quantity - amountBagsOrder.get(id));
        orderStatusPageDto.setAmountOfBagsExported(amountBagsOrderExported);
        Double exportedPrice = orderStatusPageDto.getOrderExportedDiscountedPrice();
        Double initialPrice = orderStatusPageDto.getOrderDiscountedPrice();
        orderStatusPageDto.setOrderExportedDiscountedPrice(exportedPrice - initialPrice);
        return orderStatusPageDto;
    }
}