package greencity.service.ubs;

import greencity.client.FondyClient;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.*;
import greencity.dto.address.AddressDto;
import greencity.dto.address.AddressInfoDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagOrderDto;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.notification.SenderInfoDto;
import greencity.dto.order.*;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.*;
import greencity.dto.user.*;
import greencity.entity.enums.*;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.service.UAPhoneNumberUtil;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;
import greencity.util.OrderUtils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static greencity.constant.ErrorMessage.*;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

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
    private final LiqPayService liqPayService;
    private final UserRemoteClient userRemoteClient;
    private final FondyClient fondyClient;
    private final PaymentRepository paymentRepository;
    private final EncryptionUtil encryptionUtil;
    private final EventRepository eventRepository;
    private final OrdersForUserRepository ordersForUserRepository;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final LocationRepository locationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    @Lazy
    @Autowired
    private UBSManagementService ubsManagementService;
    private final LanguageRepository languageRepository;
    @Value("${greencity.payment.fondy-payment-key}")
    private String fondyPaymentKey;
    @Value("${greencity.payment.merchant-id}")
    private String merchantId;
    @Value("${greencity.payment.liq-pay-public-key}")
    private String publicKey;
    @Value("${greencity.payment.liq-pay-private-key}")
    private String privateKey;
    @Value("${greencity.bots.viber-bot-uri}")
    private String viberBotUri;
    @Value("${greencity.bots.ubs-bot-name}")
    private String telegramBotName;
    private static final Integer BAG_CAPACITY = 120;
    public static final String LANG_CODE = "ua";
    private final EventService eventService;
    private static final String FAILED_STATUS = "failure";
    private static final String APPROVED_STATUS = "approved";
    private static final String TELEGRAM_PART_1_OF_LINK = "t.me/";
    private static final String VIBER_PART_1_OF_LINK = "viber://pa?chatURI=";
    private static final String VIBER_PART_3_OF_LINK = "&context=";
    private static final String TELEGRAM_PART_3_OF_LINK = "?start=";
    private static final String RESULT_URL_LIQPAY = "https://greencity-ubs.azurewebsites.net/ubs/receiveLiqPayPayment";
    private static final String RESULT_URL_FOR_PERSONAL_CABINET_OF_USER =
        "https://greencity-ubs.azurewebsites.net/ubs/receivePaymentClient";
    private static final String RESULT_URL_FONDY = "https://greencity-ubs.azurewebsites.net/ubs/receivePayment";

    @Override
    @Transactional
    public void validatePayment(PaymentResponseDto dto) {
        Payment orderPayment = mapPayment(dto);
        String[] ids = dto.getOrder_id().split("_");
        Order order = orderRepository.findById(Long.valueOf(ids[0]))
            .orElseThrow(() -> new PaymentValidationException(PAYMENT_VALIDATION_ERROR));
        checkResponseStatusFailure(dto, orderPayment, order);
        checkResponseValidationSignature(dto);
        checkOrderStatusApproved(dto, orderPayment, order);
    }

    private Payment mapPayment(PaymentResponseDto dto) {
        if (dto.getFee() == null) {
            dto.setFee(0);
        }
        return Payment.builder()
            .id(Long.valueOf(dto.getOrder_id().substring(dto.getOrder_id().indexOf("_") + 1)))
            .currency(dto.getCurrency())
            .amount(Long.valueOf(dto.getAmount()))
            .orderStatus(dto.getOrder_status())
            .responseStatus(dto.getResponse_status())
            .senderCellPhone(dto.getSender_cell_phone())
            .senderAccount(dto.getSender_account())
            .maskedCard(dto.getMasked_card())
            .cardType(dto.getCard_type())
            .responseCode(dto.getResponse_code())
            .responseDescription(dto.getResponse_description())
            .orderTime(dto.getOrder_time())
            .settlementDate(dto.getSettlement_date().isEmpty() ? LocalDate.now().toString() : dto.getSettlement_date())
            .fee(Long.valueOf(dto.getFee()))
            .paymentSystem(dto.getPayment_system())
            .senderEmail(dto.getSender_email())
            .paymentId(String.valueOf(dto.getPayment_id()))
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPointsAndAllBagsDto getFirstPageData(String uuid) {
        int currentUserPoints = 0;
        User user = userRepository.findByUuid(uuid);
        currentUserPoints = user.getCurrentPoints();
        List<BagTranslationDto> btdList = bagTranslationRepository.findAll()
            .stream()
            .map(this::buildBagTranslationDto)
            .collect(Collectors.toList());
        return new UserPointsAndAllBagsDto(btdList, currentUserPoints);
    }

    private BagTranslationDto buildBagTranslationDto(BagTranslation bt) {
        return BagTranslationDto.builder()
            .id(bt.getBag().getId())
            .capacity(bt.getBag().getCapacity())
            .price(bt.getBag().getFullPrice())
            .name(bt.getName())
            .nameEng(bt.getNameEng())
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
        List<UBSuser> ubsUser = ubsUserRepository.findUBSuserByUser(currentUser);
        if (ubsUser.isEmpty()) {
            ubsUser.add(UBSuser.builder().id(null).build());
        }
        PersonalDataDto dto = modelMapper.map(currentUser, PersonalDataDto.class);
        dto.setUbsUserId(ubsUser.get(0).getId());
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CertificateDto checkCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code)
            .orElseThrow(() -> new CertificateNotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + code));

        if (certificate.getCertificateStatus().toString().equals("USED")) {
            return new CertificateDto(certificate.getCertificateStatus().toString(), certificate.getPoints(),
                certificate.getDateOfUse(), certificate.getCode());
        }
        return new CertificateDto(certificate.getCertificateStatus().toString(), certificate.getPoints(),
            certificate.getExpirationDate(), certificate.getCode());
    }

    private void checkSumIfCourierLimitBySumOfOrder(TariffsInfo courierLocation, Integer sumWithoutDiscount) {
        if (CourierLimit.LIMIT_BY_SUM_OF_ORDER.equals(courierLocation.getCourierLimit())
            && sumWithoutDiscount < courierLocation.getMinPriceOfOrder()) {
            throw new SumOfOrderException(PRICE_OF_ORDER_LOWER_THAN_LIMIT + courierLocation.getMinPriceOfOrder());
        } else if (CourierLimit.LIMIT_BY_SUM_OF_ORDER.equals(courierLocation.getCourierLimit())
            && sumWithoutDiscount > courierLocation.getMaxPriceOfOrder()) {
            throw new SumOfOrderException(
                ErrorMessage.PRICE_OF_ORDER_GREATER_THAN_LIMIT + courierLocation.getMaxPriceOfOrder());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @Transactional
    public FondyOrderResponse saveFullOrderToDB(OrderResponseDto dto, String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        TariffsInfo tariffsInfo =
            tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(1L, dto.getLocationId())
                .orElseThrow(() -> new TariffNotFoundException("Tariff for courier with id " + 1L
                    + " and location with id " + dto.getLocationId() + " does not exist"));
        Map<Integer, Integer> amountOfBagsOrderedMap = new HashMap<>();

        int sumToPayWithoutDiscount = formBagsToBeSavedAndCalculateOrderSum(amountOfBagsOrderedMap, dto.getBags(),
            tariffsInfo);
        checkSumIfCourierLimitBySumOfOrder(tariffsInfo, sumToPayWithoutDiscount);
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        int sumToPay = reduceOrderSumDueToUsedPoints(sumToPayWithoutDiscount, dto.getPointsToUse());

        Order order = modelMapper.map(dto, Order.class);
        order.setTariffsInfo(tariffsInfo);
        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPay);

        UBSuser userData;
        userData = formUserDataToBeSaved(dto.getPersonalData(), currentUser);

        getOrder(dto, currentUser, amountOfBagsOrderedMap, sumToPay, order, orderCertificates, userData);

        if (sumToPay <= 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        }
        eventService.save(OrderHistory.ORDER_FORMED, OrderHistory.CLIENT, order);
        if (sumToPay == 0 || !dto.isShouldBePaid()) {
            return getPaymentRequestDto(order, null);
        } else {
            PaymentRequestDto paymentRequestDto = formPaymentRequest(order.getId(), sumToPay);
            String link = getLinkFromFondyCheckoutResponse(fondyClient.getCheckoutResponse(paymentRequestDto));
            return getPaymentRequestDto(order, link);
        }
    }

    private FondyOrderResponse getPaymentRequestDto(Order order, String link) {
        return FondyOrderResponse.builder()
            .orderId(order.getId())
            .link(link)
            .build();
    }

    @Override
    public FondyPaymentResponse getPaymentResponseFromFondy(Long id, String uuid) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_PAYMENT_STATUS);
        }
        if (order.getPayment().isEmpty()) {
            throw new PaymentNotFoundException(PAYMENT_NOT_FOUND + id);
        }
        return getFondyPaymentResponse(order);
    }

    private FondyPaymentResponse getFondyPaymentResponse(Order order) {
        Payment payment = order.getPayment().get(order.getPayment().size() - 1);
        return FondyPaymentResponse.builder()
            .paymentStatus(payment.getResponseStatus())
            .build();
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
        User currentUser = userRepository.findByUuid(uuid);
        List<Address> addresses = addressRepo.findAllByUserId(currentUser.getId());
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

        Address address = addressRepo.findById(dtoRequest.getId()).orElse(null);
        if (address != null && AddressStatus.DELETED.equals(address.getAddressStatus())) {
            address = null;
        }

        final AddressStatus addressStatus = address != null ? address.getAddressStatus() : AddressStatus.NEW;
        final User addressUser = address != null ? address.getUser() : currentUser;

        address = modelMapper.map(dtoRequest, Address.class);

        if (!addressUser.equals(currentUser)) {
            address.setId(null);
        }

        address.setUser(addressUser);
        address.setActual(true);
        address.setAddressStatus(addressStatus);
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
        if (AddressStatus.DELETED.equals(address.getAddressStatus())) {
            throw new NotFoundOrderAddressException(ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId);
        }
        if (!address.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_DELETE_ADDRESS);
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
    @Transactional
    public MakeOrderAgainDto makeOrderAgain(Locale locale, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE
            || order.getOrderStatus() == OrderStatus.CONFIRMED
            || order.getOrderStatus() == OrderStatus.DONE) {
            List<BagTranslation> bags = bagTranslationRepository.findAllByOrder(orderId);
            return buildOrderBagDto(order, bags);
        } else {
            throw new BadOrderStatusRequestException(ErrorMessage.BAD_ORDER_STATUS_REQUEST + order.getOrderStatus());
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public PageableDto<OrdersDataForUserDto> getOrdersForUser(String uuid, Pageable page) {
        PageRequest pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize());
        Page<Order> orderPages = ordersForUserRepository.findAllOrdersByUserUuid(pageRequest, uuid);
        List<Order> orders = orderPages.getContent();

        List<OrdersDataForUserDto> dtos = new ArrayList<>();

        orders.forEach(order -> dtos.add(getOrdersData(order)));

        return new PageableDto<>(
            dtos,
            orderPages.getTotalElements(),
            orderPages.getPageable().getPageNumber(),
            orderPages.getTotalPages());
    }

    private OrdersDataForUserDto getOrdersData(Order order) {
        List<Payment> payments = order.getPayment();
        List<BagForUserDto> bagForUserDtos = bagForUserDtosBuilder(order);
        OrderStatusTranslation orderStatusTranslation = orderStatusTranslationRepository
            .getOrderStatusTranslationById(order.getOrderStatus().getNumValue())
            .orElse(orderStatusTranslationRepository.getOne(1L));
        OrderPaymentStatusTranslation paymentStatusTranslation = orderPaymentStatusTranslationRepository
            .findByOrderPaymentStatusIdAndTranslationValue(
                (long) order.getOrderPaymentStatus().getStatusValue());

        Double fullPrice = Double.valueOf(bagForUserDtos.stream()
            .map(BagForUserDto::getTotalPrice)
            .reduce(0, Integer::sum));

        List<CertificateDto> certificateDtos = order.getCertificates().stream()
            .map(certificate -> {
                return modelMapper.map(certificate, CertificateDto.class);
            })
            .collect(Collectors.toList());

        Double amountBeforePayment =
            fullPrice - order.getPointsToUse() - countPaidAmount(payments) - countCertificatesBonuses(certificateDtos);

        return OrdersDataForUserDto.builder()
            .id(order.getId())
            .dateForm(order.getOrderDate())
            .datePaid(order.getOrderDate())
            .orderStatus(orderStatusTranslation.getName())
            .orderStatusEng(orderStatusTranslation.getNameEng())
            .orderComment(order.getComment())
            .bags(bagForUserDtos)
            .additionalOrders(order.getAdditionalOrders())
            .amountBeforePayment(amountBeforePayment)
            .paidAmount(countPaidAmount(payments).doubleValue())
            .orderFullPrice(fullPrice)
            .certificate(certificateDtos)
            .bonuses(order.getPointsToUse().doubleValue())
            .sender(senderInfoDtoBuilder(order))
            .address(addressInfoDtoBuilder(order))
            .paymentStatus(paymentStatusTranslation.getTranslationValue())
            .paymentStatusEng(paymentStatusTranslation.getTranslationsValueEng())
            .build();
    }

    private Integer countCertificatesBonuses(List<CertificateDto> certificateDtos) {
        return certificateDtos.stream()
            .map(CertificateDto::getPoints)
            .reduce(0, Integer::sum);
    }

    private SenderInfoDto senderInfoDtoBuilder(Order order) {
        UBSuser sender = order.getUbsUser();
        return SenderInfoDto.builder()
            .senderName(sender.getFirstName())
            .senderSurname(sender.getLastName())
            .senderEmail(sender.getEmail())
            .senderPhone(sender.getPhoneNumber())
            .build();
    }

    private AddressInfoDto addressInfoDtoBuilder(Order order) {
        Address address = order.getUbsUser().getAddress();
        return AddressInfoDto.builder()
            .addressCity(address.getCity())
            .addressCityEng(address.getCityEn())
            .addressComment(address.getAddressComment())
            .addressDistinct(address.getDistrict())
            .addressDistinctEng(address.getDistrictEn())
            .addressRegion(address.getRegion())
            .addressRegionEng(address.getRegionEn())
            .addressStreet(address.getStreet())
            .addressStreetEng(address.getStreetEn())
            .build();
    }

    private List<BagForUserDto> bagForUserDtosBuilder(Order order) {
        List<Bag> bags = bagRepository.findBagByOrderId(order.getId());
        Map<Integer, Integer> amountOfBags = order.getAmountOfBagsOrdered();
        List<BagForUserDto> bagForUserDtos = new ArrayList<>();
        bags.forEach(bag -> {
            BagForUserDto bagDto = modelMapper.map(bag, BagForUserDto.class);
            BagTranslation bagTranslation = bagTranslationRepository.findBagTranslationByBag(bag);
            bagDto.setService(bagTranslation.getName());
            bagDto.setServiceEng(bagTranslation.getNameEng());
            bagDto.setCount(amountOfBags.get(bag.getId()));
            bagDto.setTotalPrice(amountOfBags.get(bag.getId()) * bag.getFullPrice());
            bagForUserDtos.add(bagDto);
        });
        return bagForUserDtos;
    }

    private Long countPaidAmount(List<Payment> payments) {
        return payments.stream()
            .map(Payment::getAmount)
            .map(amount -> amount / 100)
            .reduce(0L, Long::sum);
    }

    private MakeOrderAgainDto buildOrderBagDto(Order order, List<BagTranslation> bags) {
        List<BagOrderDto> bagOrderDtoList = new ArrayList<>();
        for (BagTranslation bag : bags) {
            bagOrderDtoList.add(BagOrderDto.builder()
                .bagId(bag.getBag().getId())
                .name(bag.getName())
                .nameEng(bag.getNameEng())
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
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId, String uuid) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        User user = userRepository.findByUuid(uuid);
        if (!order.getUser().equals(user)) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }
        return UserInfoDto.builder()
            .customerName(order.getUser().getRecipientName())
            .customerSurName(order.getUser().getRecipientSurname())
            .customerPhoneNumber(order.getUser().getRecipientPhone())
            .customerEmail(order.getUser().getRecipientEmail())
            .totalUserViolations(userRepository.countTotalUsersViolations(order.getUser().getId()))
            .recipientId(order.getUbsUser().getId())
            .recipientName(order.getUbsUser().getFirstName())
            .recipientSurName(order.getUbsUser().getLastName())
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
    public UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Optional<UBSuser> optionalUbsUser = ubsUserRepository.findById(dtoUpdate.getRecipientId());
        if (optionalUbsUser.isEmpty()) {
            throw new UBSuserNotFoundException(RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST + dtoUpdate.getRecipientId());
        }
        UBSuser user = optionalUbsUser.get();
        ubsUserRepository.save(updateRecipientDataInOrder(user, dtoUpdate));
        eventService.save(OrderHistory.CHANGED_SENDER, currentUser.getRecipientName()
            + "  " + currentUser.getRecipientSurname(), optionalUbsUser.get().getOrders().get(0));
        return UbsCustomersDto.builder()
            .name(user.getFirstName() + " " + user.getLastName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

    private UBSuser updateRecipientDataInOrder(UBSuser ubSuser, UbsCustomersDtoUpdate dto) {
        if (nonNull(dto.getRecipientEmail())) {
            ubSuser.setEmail(dto.getRecipientEmail());
        }
        if (nonNull(dto.getRecipientName())) {
            ubSuser.setFirstName(dto.getRecipientName());
        }
        if (nonNull(dto.getRecipientSurName())) {
            ubSuser.setLastName(dto.getRecipientSurName());
        }
        if (nonNull(dto.getRecipientPhoneNumber())) {
            ubSuser.setPhoneNumber(dto.getRecipientPhoneNumber());
        }

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
        order.setSumTotalAmountWithoutDiscounts(
            (long) formBagsToBeSavedAndCalculateOrderSumClient(amountOfBagsOrderedMap));

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
            .amount(sumToPay * 100)
            .responseUrl(RESULT_URL_FONDY)
            .build();

        paymentRequestDto.setSignature(encryptionUtil
            .formRequestSignature(paymentRequestDto, fondyPaymentKey, merchantId));

        return paymentRequestDto;
    }

    private UBSuser formUserDataToBeSaved(PersonalDataDto dto, User currentUser) {
        UBSuser ubsUserFromDatabaseById = null;
        if (dto.getUbsUserId() != null) {
            ubsUserFromDatabaseById =
                ubsUserRepository.findById(dto.getUbsUserId())
                    .orElseThrow(() -> new IncorrectValueException(THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST
                        + dto.getUbsUserId()));
        }
        UBSuser mappedFromDtoUser = modelMapper.map(dto, UBSuser.class);
        mappedFromDtoUser.setUser(currentUser);
        mappedFromDtoUser.setPhoneNumber(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(mappedFromDtoUser.getPhoneNumber()));
        if (mappedFromDtoUser.getId() == null || !mappedFromDtoUser.equals(ubsUserFromDatabaseById)) {
            mappedFromDtoUser.setId(null);
            ubsUserRepository.save(mappedFromDtoUser);
            currentUser.getUbsUsers().add(mappedFromDtoUser);

            currentUser.setRecipientSurname(dto.getLastName());
            currentUser.setRecipientName(dto.getFirstName());
            currentUser.setRecipientPhone(dto.getPhoneNumber());

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
        if (sumToPay != 0 && dto.getCertificates() != null) {
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
                certificate.setCertificateStatus(CertificateStatus.USED);
                certificate.setDateOfUse(LocalDate.now());
                if (dontSendLinkToFondyIf(sumToPay, certificate)) {
                    sumToPay = 0;
                    tooManyCertificates = true;
                }
            }
        }
        return sumToPay;
    }

    private boolean dontSendLinkToFondyIf(int sumToPay, Certificate certificate) {
        if (sumToPay <= 0) {
            certificate.setCertificateStatus(CertificateStatus.USED);
            return true;
        }
        return false;
    }

    private void checkAmountOfBagsIfCourierLimitByAmountOfBag(TariffsInfo courierLocation, Integer countOfBigBag) {
        if (CourierLimit.LIMIT_BY_AMOUNT_OF_BAG.equals(courierLocation.getCourierLimit())
            && courierLocation.getMinAmountOfBigBags() > countOfBigBag) {
            throw new NotEnoughBagsException(
                NOT_ENOUGH_BIG_BAGS_EXCEPTION + courierLocation.getMinAmountOfBigBags());
        } else if (CourierLimit.LIMIT_BY_AMOUNT_OF_BAG.equals(courierLocation.getCourierLimit())
            && courierLocation.getMaxAmountOfBigBags() < countOfBigBag) {
            throw new NotEnoughBagsException(TO_MUCH_BIG_BAG_EXCEPTION + courierLocation.getMaxAmountOfBigBags());
        }
    }

    private int formBagsToBeSavedAndCalculateOrderSumClient(
        Map<Integer, Integer> getOrderBagsAndQuantity) {
        int sumToPay = 0;

        for (Map.Entry<Integer, Integer> temp : getOrderBagsAndQuantity.entrySet()) {
            Integer amount = getOrderBagsAndQuantity.get(temp.getKey());
            Bag bag = bagRepository.findById(temp.getKey())
                .orElseThrow(() -> new BagNotFoundException(BAG_NOT_FOUND + temp.getKey()));
            sumToPay += bag.getFullPrice() * amount;
        }
        return sumToPay;
    }

    private int formBagsToBeSavedAndCalculateOrderSum(
        Map<Integer, Integer> map, List<BagDto> bags, TariffsInfo courierLocation) {
        int sumToPay = 0;
        int bigBagCounter = 0;

        for (BagDto temp : bags) {
            Bag bag = bagRepository.findById(temp.getId())
                .orElseThrow(() -> new BagNotFoundException(BAG_NOT_FOUND + temp.getId()));
            if (bag.getCapacity() >= BAG_CAPACITY) {
                bigBagCounter += temp.getAmount();
            }
            sumToPay += bag.getFullPrice() * temp.getAmount();
            map.put(temp.getId(), temp.getAmount());
        }

        checkAmountOfBagsIfCourierLimitByAmountOfBag(courierLocation, bigBagCounter);
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
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Integer userBonuses = currentUser.getCurrentPoints();
        if (userBonuses == null) {
            userBonuses = 0;
        }
        List<ChangeOfPoints> changeOfPointsList = currentUser.getChangeOfPointsList();
        List<PointsForUbsUserDto> bonusForUbsUser = new ArrayList<>();
        if (nonNull(changeOfPointsList)) {
            bonusForUbsUser = changeOfPointsList.stream()
                .sorted(Comparator.comparing(ChangeOfPoints::getDate).reversed())
                .map(m -> modelMapper.map(m, PointsForUbsUserDto.class))
                .collect(Collectors.toList());
        }
        AllPointsUserDto allBonusesForUserDto = new AllPointsUserDto();
        allBonusesForUserDto.setUserBonuses(userBonuses);
        allBonusesForUserDto.setUbsUserBonuses(bonusForUbsUser);
        return allBonusesForUserDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EventDto> getAllEventsForOrder(Long orderId, String uuid) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfileUpdateDto updateProfileData(String uuid, UserProfileUpdateDto userProfileUpdateDto) {
        createUserByUuidIfUserDoesNotExist(uuid);
        User user = userRepository.findByUuid(uuid);
        setUserData(user, userProfileUpdateDto);

        List<Address> addressList =
            userProfileUpdateDto.getAddressDto().stream().map(a -> modelMapper.map(a, Address.class))
                .collect(Collectors.toList());

        for (Address address : addressList) {
            address.setUser(user);
            addressRepo.save(address);
        }
        User savedUser = userRepository.save(user);
        List<AddressDto> mapperAddressDto =
            addressList.stream().map(a -> modelMapper.map(a, AddressDto.class)).collect(Collectors.toList());
        UserProfileUpdateDto mappedUserProfileDto = modelMapper.map(savedUser, UserProfileUpdateDto.class);
        UserProfileUpdateDto.builder().addressDto(mapperAddressDto).build();
        return mappedUserProfileDto;
    }

    @Override
    public UserProfileDto getProfileData(String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        User user = userRepository.findByUuid(uuid);
        List<Address> allAddress = addressRepo.findAllByUserId(user.getId());
        UserProfileDto userProfileDto = modelMapper.map(user, UserProfileDto.class);
        List<Bot> botList = getListOfBots(user.getUuid());
        List<AddressDto> addressDto =
            allAddress.stream()
                .filter(a -> a.getAddressStatus() != AddressStatus.DELETED)
                .map(a -> modelMapper.map(a, AddressDto.class))
                .collect(Collectors.toList());
        userProfileDto.setAddressDto(addressDto);
        userProfileDto.setBotList(botList);
        userProfileDto.setHasPassword(userRemoteClient.getPasswordStatus().isHasPassword());
        return userProfileDto;
    }

    private User setUserData(User user, UserProfileUpdateDto userProfileUpdateDto) {
        user.setRecipientName(userProfileUpdateDto.getRecipientName());
        user.setRecipientSurname(userProfileUpdateDto.getRecipientSurname());
        user.setRecipientPhone(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(userProfileUpdateDto.getRecipientPhone()));
        return user;
    }

    private User createUserByUuidIfUserDoesNotExist(String uuid) {
        User user = userRepository.findByUuid(uuid);
        if (user == null) {
            UbsCustomersDto ubsCustomersDto = userRemoteClient.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Such UUID have not been found"));
            return userRepository.save(User.builder().currentPoints(0).violations(0).uuid(uuid)
                .recipientEmail(ubsCustomersDto.getEmail()).recipientName("")
                .dateOfRegistration(LocalDate.now()).build());
        }
        return user;
    }

    @Override
    public void markUserAsDeactivated(Long id) {
        User user =
            userRepository.findById(id).orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        userRemoteClient.markUserDeactivated(user.getUuid());
    }

    @Override
    public OrderCancellationReasonDto getOrderCancellationReason(final Long orderId, String uuid) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_ORDER_CANCELLATION_REASON);
        }
        return OrderCancellationReasonDto.builder()
            .cancellationReason(order.getCancellationReason())
            .cancellationComment(order.getCancellationComment())
            .build();
    }

    @Override
    public OrderCancellationReasonDto updateOrderCancellationReason(
        long id, OrderCancellationReasonDto dto, String uuid) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_ORDER_CANCELLATION_REASON);
        }
        order.setCancellationReason(dto.getCancellationReason());
        order.setCancellationComment(dto.getCancellationComment());
        order.setId(id);
        orderRepository.save(order);
        return dto;
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
    public LiqPayOrderResponse saveFullOrderToDBFromLiqPay(OrderResponseDto dto, String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        TariffsInfo tariffsInfo =
            tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(1L, dto.getLocationId())
                .orElseThrow(() -> new TariffNotFoundException(
                    "Tariff for location with id " + dto.getLocationId() + " does not exist"));
        Map<Integer, Integer> amountOfBagsOrderedMap = new HashMap<>();

        int sumToPayWithoutDiscount = formBagsToBeSavedAndCalculateOrderSum(amountOfBagsOrderedMap, dto.getBags(),
            tariffsInfo);
        checkSumIfCourierLimitBySumOfOrder(tariffsInfo, sumToPayWithoutDiscount);
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        int sumToPay = reduceOrderSumDueToUsedPoints(sumToPayWithoutDiscount, dto.getPointsToUse());

        Order order = modelMapper.map(dto, Order.class);
        order.setTariffsInfo(tariffsInfo);
        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPay);

        final UBSuser userData = formUserDataToBeSaved(dto.getPersonalData(), currentUser);

        getOrder(dto, currentUser, amountOfBagsOrderedMap, sumToPay, order, orderCertificates, userData);

        eventService.save(OrderHistory.ORDER_FORMED, OrderHistory.CLIENT, order);
        if (sumToPay == 0 || !dto.isShouldBePaid()) {
            return buildOrderResponseWithoutButton(order);
        } else {
            PaymentRequestDtoLiqPay paymentRequestDto = formLiqPayPaymentRequest(order.getId(), sumToPay);
            String liqPayData = liqPayService.getCheckoutResponse(paymentRequestDto);
            return buildOrderResponse(order, liqPayData
                .replace("\"", "")
                .replace("\n", ""));
        }
    }

    private int reduceOrderSumDueToUsedPoints(int sumToPay, int pointsToUse) {
        if (sumToPay >= pointsToUse) {
            sumToPay -= pointsToUse;
        }
        return sumToPay;
    }

    private void getOrder(OrderResponseDto dto, User currentUser, Map<Integer, Integer> amountOfBagsOrderedMap,
        int sumToPay, Order order, Set<Certificate> orderCertificates, UBSuser userData) {
        Address address = addressRepo.findById(dto.getAddressId()).orElseThrow(() -> new NotFoundOrderAddressException(
            ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + dto.getAddressId()));

        checkIfAddressHasBeenDeleted(address);

        checkAddressUser(address, currentUser);
        address.setAddressStatus(AddressStatus.IN_ORDER);

        userData.setAddress(address);

        if (userData.getAddress().getAddressComment() == null) {
            userData.getAddress().setAddressComment(dto.getPersonalData().getAddressComment());
        }

        formAndSaveOrder(order, orderCertificates, amountOfBagsOrderedMap, userData, currentUser, sumToPay);

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);
    }

    private LiqPayOrderResponse buildOrderResponse(Order order, String button) {
        return LiqPayOrderResponse.builder()
            .orderId(order.getId())
            .liqPayButton(button)
            .build();
    }

    private LiqPayOrderResponse buildOrderResponseWithoutButton(Order order) {
        return LiqPayOrderResponse.builder()
            .orderId(order.getId())
            .build();
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
            .resultUrl(RESULT_URL_LIQPAY)
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
    public OrderStatusPageDto getOrderInfoForSurcharge(Long orderId) {
        OrderStatusPageDto orderStatusPageDto = ubsManagementService.getOrderStatusData(orderId);
        Map<Integer, Integer> amountBagsOrder = orderStatusPageDto.getAmountOfBagsOrdered();
        Map<Integer, Integer> amountBagsOrderExported = orderStatusPageDto.getAmountOfBagsExported();
        amountBagsOrderExported.replaceAll((id, quantity) -> quantity = quantity - amountBagsOrder.get(id));
        orderStatusPageDto.setAmountOfBagsExported(amountBagsOrderExported);
        Double exportedPrice = orderStatusPageDto.getOrderExportedDiscountedPrice();
        Double initialPrice = orderStatusPageDto.getOrderDiscountedPrice();
        orderStatusPageDto.setOrderExportedDiscountedPrice(exportedPrice - initialPrice);
        return orderStatusPageDto;
    }

    private StatusRequestDtoLiqPay getStatusFromLiqPay(Order order) {
        Long orderId = order.getId();

        Long paymentId = 0L;
        try {
            paymentId = order.getPayment().get(order.getPayment().size() - 1).getId();
        } catch (IndexOutOfBoundsException e) {
            throw new LiqPayPaymentException(ORDER_WITH_CURRENT_ID_NOT_FOUND);
        }

        return StatusRequestDtoLiqPay.builder()
            .publicKey(publicKey)
            .action("status")
            .orderId(orderId + "_" + paymentId.toString())
            .version(3)
            .build();
    }

    @Override
    public Map<String, Object> getLiqPayStatus(Long orderId, String uuid) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_PAYMENT_STATUS);
        }
        StatusRequestDtoLiqPay dto = getStatusFromLiqPay(order);
        Map<String, Object> response = liqPayService.getPaymentStatus(dto);
        @Nullable
        Payment payment = converterMapToEntity(response, order);
        if (payment == null) {
            throw new LiqPayPaymentException(LIQPAY_PAYMENT_WITH_SELECTED_ID_NOT_FOUND);
        }
        paymentRepository.save(payment);
        orderRepository.save(order);
        return response;
    }

    private Payment converterMapToEntity(Map<String, Object> map, Order order) {
        if (map == null) {
            return null;
        }
        List<Payment> payments = paymentRepository.findAllByOrder(order);
        Payment payment = payments.get(payments.size() - 1);
        String status = (String) map.get("status");
        if (status.equals("success")) {
            payment.setResponseStatus(status);
            payment.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            setPaymentInfo(map, payment);
            eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
            eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM + payment.getPaymentId(),
                OrderHistory.SYSTEM, order);
        } else if (status.equals(FAILED_STATUS)) {
            payment.setResponseStatus(status);
            payment.setPaymentStatus(PaymentStatus.UNPAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
            setPaymentInfo(map, payment);
        }
        return payment;
    }

    private void setPaymentInfo(Map<String, Object> map, Payment payment) {
        payment.setMaskedCard((String) map.get("sender_card_mask2"));
        payment.setCurrency((String) map.get("currency"));
        payment.setPaymentSystem("LiqPay");
        payment.setCardType((String) map.get("sender_card_type"));
        payment.setResponseDescription((String) map.get("err_description"));
        payment.setPaymentId((String) map.get("payment_id"));
        payment.setComment((String) map.get("description"));
        payment.setPaymentType(PaymentType.AUTO);
        payment.setSenderCellPhone((String) map.get("sender_phone"));
        String orderTime = convertMillisecondToLocalDateTime((long) map.get("create_date"));
        payment.setOrderTime(orderTime);
        String endDate = convertMillisecondToLocalDate((Long) map.get("end_date"));
        payment.setSettlementDate(endDate);
        double fees = (double) map.get("sender_commission");
        payment.setFee((long) fees);
        double amount = (double) map.get("amount");
        payment.setAmount((long) amount * 100);
    }

    private String convertMillisecondToLocalDateTime(long millis) {
        LocalDateTime date =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(date);
        return new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(timestamp);
    }

    private String convertMillisecondToLocalDate(long millis) {
        LocalDate date =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dateFormatter.format(date);
    }

    @Override
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        orderRepository.delete(order);
    }

    @Override
    public FondyOrderResponse processOrderFondyClient(OrderFondyClientDto dto, String uuid) {
        Order order = findByIdOrderForClient(dto);
        User currentUser = findByIdUserForClient(uuid);
        Map<Integer, Integer> amountOfBagsOrderedMap = order.getAmountOfBagsOrdered();
        checkForNullCounter(order);
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        Integer sumToPay = formBagsToBeSavedAndCalculateOrderSumClient(amountOfBagsOrderedMap);
        sumToPay = reduceOrderSumDueToUsedPoints(sumToPay, dto.getPointsToUse());

        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSumClient(dto, orderCertificates, order, sumToPay);

        currentUser.setCurrentPoints(currentUser.getCurrentPoints() - dto.getPointsToUse());
        userRepository.save(currentUser);

        paymentVerification(sumToPay, order);

        if (sumToPay == 0) {
            return getPaymentRequestDto(order, null);
        } else {
            String link = formedLink(order, sumToPay);
            return getPaymentRequestDto(order, link);
        }
    }

    private void paymentVerification(Integer sumToPay, Order order) {
        if (sumToPay <= 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_CONFIRMED, OrderHistory.SYSTEM, order);
        }
    }

    private void checkForNullCounter(Order order) {
        if (order.getCounterOrderPaymentId() == null) {
            order.setCounterOrderPaymentId(0L);
        }
    }

    private Order findByIdOrderForClient(OrderFondyClientDto dto) {
        return orderRepository.findById(dto.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
    }

    private User findByIdUserForClient(String uuid) {
        return userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
    }

    private String formedLink(Order order, Integer sumToPay) {
        Order increment = incrementCounter(order);
        PaymentRequestDto paymentRequestDto = formPayment(increment.getId(), sumToPay);
        return getLinkFromFondyCheckoutResponse(fondyClient.getCheckoutResponse(paymentRequestDto));
    }

    private String getLinkFromFondyCheckoutResponse(String fondyResponse) {
        JSONObject json = new JSONObject(fondyResponse);
        if (!json.has("response")) {
            throw new PaymentValidationException("Wrong response");
        }
        JSONObject response = json.getJSONObject("response");
        if ("success".equals(response.getString("response_status"))) {
            return response.getString("checkout_url");
        }
        throw new PaymentValidationException(response.getString("error_message"));
    }

    private Order incrementCounter(Order order) {
        order.setCounterOrderPaymentId(order.getCounterOrderPaymentId() + 1);
        orderRepository.save(order);
        return order;
    }

    private PaymentRequestDto formPayment(Long orderId, int sumToPay) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));

        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
            .merchantId(Integer.parseInt(merchantId))
            .orderId(OrderUtils.generateOrderIdForPayment(orderId, order))
            .orderDescription("courier")
            .currency("UAH")
            .amount(sumToPay * 100)
            .responseUrl(RESULT_URL_FOR_PERSONAL_CABINET_OF_USER)
            .build();
        paymentRequestDto.setSignature(encryptionUtil
            .formRequestSignature(paymentRequestDto, fondyPaymentKey, merchantId));
        return paymentRequestDto;
    }

    private int formCertificatesToBeSavedAndCalculateOrderSumClient(OrderFondyClientDto dto,
        Set<Certificate> orderCertificates,
        Order order, int sumToPay) {
        if (sumToPay != 0 && dto.getCertificates() != null) {
            Set<Certificate> certificates =
                certificateRepository.getAllByListId(new ArrayList<>(dto.getCertificates()));
            if (certificates.isEmpty()) {
                throw new CertificateNotFoundException(ErrorMessage.CERTIFICATE_NOT_FOUND);
            }
            checkValidationCertificates(certificates, dto);
            for (Certificate temp : certificates) {
                Certificate certificate = getCertificateForClient(orderCertificates, temp, order);
                sumToPay -= certificate.getPoints();

                if (dontSendLinkToFondyIfClient(sumToPay)) {
                    certificate.setCertificateStatus(CertificateStatus.USED);
                    sumToPay = 0;
                }
            }
        }
        return sumToPay;
    }

    private void checkValidationCertificates(Set<Certificate> certificates, OrderFondyClientDto dto) {
        if (certificates.size() != dto.getCertificates().size()) {
            String validCertification = certificates.stream().map(Certificate::getCode).collect(joining(", "));
            throw new CertificateNotFoundException(ErrorMessage.SOME_CERTIFICATES_ARE_INVALID + validCertification);
        }
    }

    private Certificate getCertificateForClient(Set<Certificate> orderCertificates, Certificate certificate,
        Order order) {
        certificate.setOrder(order);
        orderCertificates.add(certificate);
        certificate.setCertificateStatus(CertificateStatus.USED);
        certificate.setDateOfUse(LocalDate.now());
        return certificate;
    }

    private boolean dontSendLinkToFondyIfClient(int sumToPay) {
        return sumToPay <= 0;
    }

    @Override
    public LiqPayOrderResponse proccessOrderLiqpayClient(OrderFondyClientDto dto, String uuid) {
        Order order = orderRepository.findById(dto.getOrderId()).orElseThrow();
        User currentUser = findByIdUserForClient(uuid);

        Map<Integer, Integer> amountOfBagsOrderedMap = order.getAmountOfBagsOrdered();
        checkForNullCounter(order);
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());

        Integer sumToPay = formBagsToBeSavedAndCalculateOrderSumClient(amountOfBagsOrderedMap);
        sumToPay = reduceOrderSumDueToUsedPoints(sumToPay, dto.getPointsToUse());

        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSumClient(dto, orderCertificates, order, sumToPay);

        currentUser.setCurrentPoints(currentUser.getCurrentPoints() - dto.getPointsToUse());
        userRepository.save(currentUser);

        paymentVerification(sumToPay, order);

        if (sumToPay == 0) {
            return buildOrderResponse(order, null);
        } else {
            return linkButton(order, sumToPay);
        }
    }

    private LiqPayOrderResponse linkButton(Order order, Integer sumToPay) {
        Order order1 = incrementCounter(order);
        PaymentRequestDtoLiqPay paymentRequestDtoLiqPay = formLiqPayPayment(order1.getId(), sumToPay);
        return buildOrderResponse(order1, liqPayService.getCheckoutResponse(paymentRequestDtoLiqPay)
            .replace("\"", "")
            .replace("\n", ""));
    }

    private PaymentRequestDtoLiqPay formLiqPayPayment(Long orderId, int sumToPay) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return PaymentRequestDtoLiqPay.builder()
            .publicKey(publicKey)
            .version(3)
            .action("pay")
            .amount(sumToPay)
            .currency("UAH")
            .description("сourier")
            .orderId(
                orderId + "_" + order.getCounterOrderPaymentId().toString() + "_" + order.getPayment().get(0).getId())
            .language("en")
            .paytypes("card")
            .resultUrl(RESULT_URL_LIQPAY)
            .build();
    }

    @Override
    @Transactional
    public void validatePaymentClient(PaymentResponseDto dto) {
        Payment orderPayment = mapPaymentClient(dto);
        String[] id = orderIdInfo(dto);
        Order order = orderRepository.findById(Long.valueOf(id[0]))
            .orElseThrow(() -> new PaymentValidationException(PAYMENT_VALIDATION_ERROR));

        checkResponseStatusFailure(dto, orderPayment, order);
        checkResponseValidationSignature(dto);
        checkOrderStatusApproved(dto, orderPayment, order);
    }

    private Payment mapPaymentClient(PaymentResponseDto dto) {
        String[] idClient = orderIdInfo(dto);
        Order order = orderRepository.findById(Long.valueOf(idClient[0]))
            .orElseThrow(() -> new PaymentValidationException(PAYMENT_VALIDATION_ERROR));
        int lastNumber = order.getPayment().size() - 1;
        checkDtoFee(dto);

        return Payment.builder()
            .id(Long.valueOf(idClient[0] + order.getCounterOrderPaymentId()
                + order.getPayment().get(lastNumber).getId()))
            .currency(dto.getCurrency())
            .amount(Long.valueOf(dto.getAmount()))
            .orderStatus(dto.getOrder_status())
            .responseStatus(dto.getResponse_status())
            .senderCellPhone(dto.getSender_cell_phone())
            .senderAccount(dto.getSender_account())
            .maskedCard(dto.getMasked_card())
            .cardType(dto.getCard_type())
            .responseCode(dto.getResponse_code())
            .responseDescription(dto.getResponse_description())
            .orderTime(dto.getOrder_time())
            .settlementDate(dto.getSettlement_date().isEmpty() ? LocalDate.now().toString() : dto.getSettlement_date())
            .fee(Optional.ofNullable(dto.getFee()).map(Long::valueOf).orElse(0L))
            .paymentSystem(dto.getPayment_system())
            .senderEmail(dto.getSender_email())
            .paymentId(String.valueOf(dto.getPayment_id()))
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }

    private String[] orderIdInfo(PaymentResponseDto dto) {
        return dto.getOrder_id().split("_");
    }

    private void checkResponseStatusFailure(PaymentResponseDto dto, Payment orderPayment, Order order) {
        if (dto.getResponse_status().equals(FAILED_STATUS)) {
            orderPayment.setPaymentStatus(PaymentStatus.UNPAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
        }
    }

    private void checkResponseValidationSignature(PaymentResponseDto dto) {
        if (!encryptionUtil.checkIfResponseSignatureIsValid(dto, fondyPaymentKey)) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
    }

    private void checkOrderStatusApproved(PaymentResponseDto dto, Payment orderPayment, Order order) {
        if (dto.getOrder_status().equals(APPROVED_STATUS)) {
            orderPayment.setPaymentId(String.valueOf(dto.getPayment_id()));
            orderPayment.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            orderPayment.setOrder(order);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
            eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM + orderPayment.getPaymentId(),
                OrderHistory.SYSTEM, order);
        }
    }

    private void checkDtoFee(PaymentResponseDto dto) {
        if (dto.getFee() == null) {
            dto.setFee(0);
        }
    }

    @Override
    public UserPointDto getUserPoint(String uuid) {
        User user = userRepository.findByUuid(uuid);
        int currentUserPoints = user.getCurrentPoints();

        return new UserPointDto(currentUserPoints);
    }

    private List<Bot> getListOfBots(String uuid) {
        return EnumSet.allOf(BotType.class)
            .stream()
            .map(type -> new Bot(type.name(), createLink(type, uuid)))
            .collect(Collectors.toList());
    }

    private String createLink(BotType type, String uuid) {
        String linkTemplate = null;
        if ("TELEGRAM".equals(type.name())) {
            linkTemplate = String.format("%s%s%s%s",
                TELEGRAM_PART_1_OF_LINK, telegramBotName, TELEGRAM_PART_3_OF_LINK, uuid);
        }
        if ("VIBER".equals(type.name())) {
            linkTemplate = String.format("%s%s%s%s",
                VIBER_PART_1_OF_LINK, viberBotUri, VIBER_PART_3_OF_LINK, uuid);
        }
        return linkTemplate;
    }

    private List<AllActiveLocationsDto> getAllActiveLocations() {
        List<Location> locations = locationRepository.findAllActive();
        Map<RegionDto, List<LocationsDtos>> map = locations.stream()
            .collect(Collectors.toMap(x -> modelMapper.map(x, RegionDto.class),
                x -> new ArrayList<>(List.of(modelMapper.map(x, LocationsDtos.class))),
                (x, y) -> {
                    x.addAll(y);
                    return new ArrayList<>(x);
                }));

        return map.entrySet().stream()
            .map(x -> AllActiveLocationsDto.builder()
                .regionId(x.getKey().getRegionId())
                .nameEn(x.getKey().getNameEn())
                .nameUk(x.getKey().getNameUk())
                .locations(x.getValue())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public OrderCourierPopUpDto getInfoForCourierOrdering(String uuid, Optional<String> changeLoc) {
        OrderCourierPopUpDto orderCourierPopUpDto = new OrderCourierPopUpDto();
        if (changeLoc.isPresent()) {
            orderCourierPopUpDto.setOrderIsPresent(false);
            orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocations());
            return orderCourierPopUpDto;
        }
        Optional<Order> lastOrder = orderRepository.getLastOrderOfUserByUUIDIfExists(uuid);
        if (lastOrder.isPresent()) {
            orderCourierPopUpDto.setOrderIsPresent(true);
            orderCourierPopUpDto.setTariffsForLocationDto(
                modelMapper.map(tariffsInfoRepository.findTariffsInfoByOrder(lastOrder.get().getId()),
                    TariffsForLocationDto.class));
        } else {
            orderCourierPopUpDto.setOrderIsPresent(false);
            orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocations());
        }
        return orderCourierPopUpDto;
    }

    private TariffsInfo findTariffsInfoByCourierAndLocationId(Long courierId, Long locationId) {
        return tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(courierId, locationId)
            .orElseThrow(
                () -> new TariffNotFoundException("Tariff for location with id " + locationId + " does not exist"));
    }

    @Override
    public OrderCourierPopUpDto getTariffInfoForLocation(Long locationId) {
        return OrderCourierPopUpDto.builder()
            .orderIsPresent(true)
            .tariffsForLocationDto(modelMapper.map(
                findTariffsInfoByCourierAndLocationId(1L, locationId), TariffsForLocationDto.class))
            .build();
    }
}
