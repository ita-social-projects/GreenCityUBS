package greencity.service.ubs;

import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import greencity.client.UserRemoteClient;
import greencity.client.WayForPayClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.constant.TariffLocation;
import greencity.constant.KyivTariffLocation;
import greencity.dto.AllActiveLocationsDto;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.LocationsDto;
import greencity.dto.LocationsDtos;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.RegionDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.AddressInfoDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.dto.notification.SenderInfoDto;
import greencity.dto.order.EventDto;
import greencity.dto.order.WayForPayOrderResponse;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.PointsForUbsUserDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.locations.City;
import greencity.entity.user.locations.District;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.entity.viber.ViberBot;
import greencity.enums.AddressStatus;
import greencity.enums.BagStatus;
import greencity.enums.BotType;
import greencity.enums.CertificateStatus;
import greencity.enums.CourierLimit;
import greencity.enums.LocationStatus;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.TariffStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.WrongSignatureException;
import greencity.exceptions.certificate.CertificateIsNotActivated;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.mapping.location.LocationToLocationsDtoMapper;
import greencity.repository.AddressRepository;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.CourierRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViberBotRepository;
import greencity.repository.RegionRepository;
import greencity.repository.CityRepository;
import greencity.repository.DistrictRepository;
import greencity.service.DistanceCalculationUtils;
import greencity.service.google.GoogleApiService;
import greencity.service.locations.LocationApiService;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;
import greencity.util.OrderUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import static greencity.constant.AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT_ENG;
import static greencity.constant.AppConstant.USER_WITH_PREFIX;
import static greencity.constant.ErrorMessage.ACTUAL_ADDRESS_NOT_FOUND;
import static greencity.constant.ErrorMessage.ADDRESS_ALREADY_EXISTS;
import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_ORDER_CANCELLATION_REASON;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_PAYMENT_STATUS;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_PERSONAL_INFO;
import static greencity.constant.ErrorMessage.CANNOT_DELETE_ADDRESS;
import static greencity.constant.ErrorMessage.CANNOT_DELETE_ALREADY_DELETED_ADDRESS;
import static greencity.constant.ErrorMessage.CANNOT_MAKE_ACTUAL_DELETED_ADDRESS;
import static greencity.constant.ErrorMessage.CERTIFICATE_EXPIRED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_NOT_ACTIVATED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_USED;
import static greencity.constant.ErrorMessage.CERTIFICATE_NOT_FOUND;
import static greencity.constant.ErrorMessage.CERTIFICATE_NOT_FOUND_BY_CODE;
import static greencity.constant.ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.EMPLOYEE_DOESNT_EXIST;
import static greencity.constant.ErrorMessage.EVENTS_NOT_FOUND_EXCEPTION;
import static greencity.constant.ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.LOCATION_IS_DEACTIVATED_FOR_TARIFF;
import static greencity.constant.ErrorMessage.NOT_ENOUGH_BAGS_EXCEPTION;
import static greencity.constant.ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER;
import static greencity.constant.ErrorMessage.NUMBER_OF_ADDRESSES_EXCEEDED;
import static greencity.constant.ErrorMessage.ORDER_ALREADY_PAID;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.PAYMENT_NOT_FOUND;
import static greencity.constant.ErrorMessage.PAYMENT_VALIDATION_ERROR;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_GREATER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_LOWER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.SOME_CERTIFICATES_ARE_INVALID;
import static greencity.constant.ErrorMessage.TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_ORDER_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND_BY_LOCATION_ID;
import static greencity.constant.ErrorMessage.TARIFF_OR_LOCATION_IS_DEACTIVATED;
import static greencity.constant.ErrorMessage.THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TOO_MANY_CERTIFICATES;
import static greencity.constant.ErrorMessage.TOO_MUCH_POINTS_FOR_ORDER;
import static greencity.constant.ErrorMessage.TO_MUCH_BAG_EXCEPTION;
import static greencity.constant.ErrorMessage.USER_DONT_HAVE_ENOUGH_POINTS;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.joining;

/**
 * Implementation of {@link UBSClientService}.
 */
@Service
@RequiredArgsConstructor
public class UBSClientServiceImpl implements UBSClientService {
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSUserRepository ubsUserRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final EmployeeRepository employeeRepository;
    private final AddressRepository addressRepo;
    private final OrderAddressRepository orderAddressRepository;
    private final UserRemoteClient userRemoteClient;
    private final PaymentRepository paymentRepository;
    private final EncryptionUtil encryptionUtil;
    private final EventRepository eventRepository;
    private final OrdersForUserRepository ordersForUserRepository;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final GoogleApiService googleApiService;
    private final EventService eventService;
    private final TariffLocationRepository tariffLocationRepository;
    private final LocationRepository locationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final TelegramBotRepository telegramBotRepository;
    private final ViberBotRepository viberBotRepository;
    private final LocationApiService locationApiService;
    private final OrderBagRepository orderBagRepository;
    private final OrderBagService orderBagService;
    private final NotificationService notificationService;
    private final WayForPayClient wayForPayClient;
    private final LocationToLocationsDtoMapper locationToLocationsDtoMapper;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    @Value("${greencity.bots.viber-bot-uri}")
    private String viberBotUri;
    @Value("${greencity.bots.ubs-bot-name}")
    private String telegramBotName;
    @Value("${greencity.redirect.result-way-for-pay-url}")
    private String resultWayForPayUrl;
    @Value("${greencity.wayforpay.login}")
    private String merchantAccount;
    @Value("${greencity.wayforpay.secret}")
    private String wayForPaySecret;
    @Value("${greencity.wayforpay.merchant.domain.name}")
    private String merchantDomainName;
    private static final String FAILED_STATUS = "failure";
    private static final String APPROVED_STATUS = "Approved";
    private static final String TELEGRAM_PART_1_OF_LINK = "https://telegram.me/";
    private static final String VIBER_PART_1_OF_LINK = "viber://pa?chatURI=";
    private static final String VIBER_PART_3_OF_LINK = "&context=";
    private static final String TELEGRAM_PART_3_OF_LINK = "?start=";
    private static final Integer MAXIMUM_NUMBER_OF_ADDRESSES = 4;
    private static final String KYIV_REGION_EN = "Kyiv Oblast";
    private static final String KYIV_REGION_UA = "Київська область";
    private static final String KYIV_EN = "Kyiv";
    private static final String KYIV_UA = "місто Київ";
    private static final String LANGUAGE_EN = "en";
    private static final Double KYIV_LATITUDE = 50.4546600;
    private static final Double KYIV_LONGITUDE = 30.5238000;
    private static final Double LOCATION_40_KM_ZONE_VALUE = 40.00;
    private static final String UKRAINE_EN = "Ukraine";
    private static final String LANG_EN = "en";
    private static final String ADDRESS_NOT_FOUND_BY_ID_MESSAGE = "Address not found with id: ";
    private static final String ADDRESS_NOT_WITHIN_LOCATION_AREA_MESSAGE = "Location and Address selected "
        + "does not match, reselect correct data.";
    private static final byte CURRENCY_CONVERSION_RATE = 100;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PaymentResponseWayForPay validatePayment(PaymentResponseDto response) {
        Payment orderPayment = mapPayment(response);
        String[] ids = response.getOrderReference().split("_");
        Order order = orderRepository.findById(Long.valueOf(ids[0]))
            .orElseThrow(() -> new BadRequestException(PAYMENT_VALIDATION_ERROR));
        checkResponseStatusFailure(response, orderPayment, order);
        checkOrderStatusApproved(response, orderPayment, order);
        PaymentResponseWayForPay accept = PaymentResponseWayForPay.builder()
            .orderReference(response.getOrderReference())
            .status("accept")
            .time(response.getCreatedDate()).build();
        accept.setSignature(encryptionUtil.formResponseSignature(accept, wayForPaySecret));
        return accept;
    }

    private Payment mapPayment(PaymentResponseDto response) {
        if (response.getFee() == null) {
            response.setFee("0");
        }
        return Payment.builder()
            .id(Long.valueOf(response.getOrderReference()
                .substring(response.getOrderReference().lastIndexOf("_") + 1)))
            .currency(response.getCurrency())
            .amount(Long.parseLong(response.getAmount()) * 100)
            .orderStatus(response.getTransactionStatus())
            .senderCellPhone(response.getPhone())
            .maskedCard(response.getCardPan())
            .cardType(response.getCardType())
            .orderTime(response.getCreatedDate())
            .settlementDate(parseFondySettlementDate(""))
            .fee(0L)
            .paymentSystem(response.getPaymentSystem())
            .senderEmail(response.getEmail())
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }

    private String parseFondySettlementDate(String settlementDate) {
        return settlementDate.isEmpty()
            ? LocalDate.now().toString()
            : LocalDate.parse(settlementDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString();
    }

    /**
     * This method is used to validate the signature of the payment received from
     * LiqPay. It concatenates the private key and data, generates a SHA-1 hash,
     * encodes it in Base64, and compares it with the received signature. If the
     * generated signature doesn't match the received one, it throws a
     * `WrongSignatureException`.
     *
     * @param privateKey        The private key used for signature validation.
     * @param data              The data received from LiqPay.
     * @param receivedSignature The signature received from LiqPay.
     */
    protected void checkSignature(String privateKey, String data, String receivedSignature) {
        String message = privateKey + data + privateKey;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1"); // NOSONAR
            byte[] messageDigest = md.digest(message.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(messageDigest);
            if (!receivedSignature.equals(signature)) {
                throw new WrongSignatureException(ErrorMessage.WRONG_SIGNATURE_USED);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new WrongSignatureException(ErrorMessage.WRONG_SIGNATURE_USED);
        }
    }

    /**
     * This method is used to extract the order ID from the provided data. The data
     * is a Base64 encoded string, which is first decoded into a regular string. The
     * decoded string is then converted into a JSON object, from which the order ID
     * is extracted.
     *
     * @param data The Base64 encoded string containing the order ID.
     * @return The order ID extracted from the data.
     */
    protected Long extractOrderIdFromData(String data) {
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(decodedString);
        return Long.valueOf(jsonObject.getString("order_id"));
    }

    /**
     * This method is used to extract the status from the provided data. The data is
     * a Base64 encoded string, which is first decoded into a regular string. The
     * decoded string is then converted into a JSON object, from which the status is
     * extracted.
     *
     * @param data The Base64 encoded string containing the status.
     * @return The status extracted from the data.
     * @note This method is not intended for use in a test environment.
     */
    protected String extractStatusFromData(String data) { // Don`t use in test env
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(decodedString);
        return jsonObject.getString("status");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPointsAndAllBagsDto getFirstPageDataByTariffAndLocationId(Long tariffId, Long locationId) {
        var tariffsInfo = tariffsInfoRepository.findById(tariffId)
            .orElseThrow(() -> new NotFoundException(TARIFF_NOT_FOUND + tariffId));

        var location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException(LOCATION_DOESNT_FOUND_BY_ID + locationId));

        checkIfTariffIsAvailableForCurrentLocation(tariffsInfo, location);

        return getUserPointsAndAllBagsDtoByTariffIdAndUserPoints(tariffsInfo.getId(), 0);
    }

    @Override
    public UserPointsAndAllBagsDto getFirstPageDataByOrderId(String uuid, Long orderId) {
        var user = userRepository.findUserByUuid(uuid).orElseThrow(
            () -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        var order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));

        checkIsOrderOfCurrentUser(user, order);

        var tariffsInfo = order.getTariffsInfo();

        var location = getLocationByOrderIdThroughLazyInitialization(order);

        checkIfTariffIsAvailableForCurrentLocation(tariffsInfo, location);

        return getUserPointsAndAllBagsDtoByTariffIdAndOrderIdAndUserPoints(tariffsInfo.getId(), user.getCurrentPoints(),
            orderId);
    }

    private void checkIsOrderOfCurrentUser(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER);
        }
    }

    private void checkIfTariffIsAvailableForCurrentLocation(TariffsInfo tariffsInfo, Location location) {
        if (tariffsInfo.getTariffStatus() == TariffStatus.DEACTIVATED
            || location.getLocationStatus() == LocationStatus.DEACTIVATED) {
            throw new BadRequestException(TARIFF_OR_LOCATION_IS_DEACTIVATED);
        } else {
            var isAvailable = isTariffAvailableForCurrentLocation(tariffsInfo, location);
            if (!isAvailable) {
                throw new BadRequestException(LOCATION_IS_DEACTIVATED_FOR_TARIFF + tariffsInfo.getId());
            }
        }
    }

    private boolean isTariffAvailableForCurrentLocation(TariffsInfo tariffsInfo, Location location) {
        return tariffLocationRepository
            .findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location)
            .orElseThrow(() -> new NotFoundException(TARIFF_FOR_LOCATION_NOT_EXIST + location.getId()))
            .getLocationStatus() != LocationStatus.DEACTIVATED;
    }

    private UserPointsAndAllBagsDto getUserPointsAndAllBagsDtoByTariffIdAndOrderIdAndUserPoints(Long tariffId,
        Integer userPoints, Long orderId) {
        var bagTranslationDtoList = bagRepository.findAllActiveBagsByTariffsInfoId(tariffId).stream()
            .map(bag -> buildBagTranslationDto(orderId, bag))
            .collect(toList());
        return new UserPointsAndAllBagsDto(bagTranslationDtoList, userPoints);
    }

    private UserPointsAndAllBagsDto getUserPointsAndAllBagsDtoByTariffIdAndUserPoints(Long tariffId,
        Integer userPoints) {
        var bagTranslationDtoList = bagRepository.findAllActiveBagsByTariffsInfoId(tariffId).stream()
            .map(bag -> modelMapper.map(bag, BagTranslationDto.class))
            .collect(toList());
        return new UserPointsAndAllBagsDto(bagTranslationDtoList, userPoints);
    }

    private BagTranslationDto buildBagTranslationDto(Long orderId, Bag source) {
        return BagTranslationDto.builder()
            .id(source.getId())
            .capacity(source.getCapacity())
            .price(BigDecimal.valueOf(source.getFullPrice())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .limitedIncluded(source.getLimitIncluded())
            .quantity(getQuantityOfBagsByBagIdAndOrderId(orderId, source.getId()))
            .build();
    }

    private Integer getQuantityOfBagsByBagIdAndOrderId(Long orderId, Integer bagId) {
        return orderBagRepository.getAmountOfOrderBagsByOrderIdAndBagId(orderId, bagId)
            .orElse(0);
    }

    private Location getLocationByOrderIdThroughLazyInitialization(Order order) {
        return order
            .getUbsUser()
            .getOrderAddress()
            .getLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PersonalDataDto getSecondPageData(String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        List<UBSuser> ubsUser = ubsUserRepository.findUBSuserByUser(currentUser);

        if (ubsUser.isEmpty()) {
            ubsUser = Collections.singletonList(UBSuser.builder().id(null).build());
        }
        PersonalDataDto dto = modelMapper.map(currentUser, PersonalDataDto.class);
        dto.setUbsUserId(ubsUser.getFirst().getId());
        if (currentUser.getAlternateEmail() != null
            && !currentUser.getAlternateEmail().isEmpty()) {
            dto.setEmail(currentUser.getAlternateEmail());
        }
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CertificateDto checkCertificate(String code, String userUuid) {
        Certificate certificate = certificateRepository.findById(code)
            .orElseThrow(() -> new NotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + code));

        if (certificate.getCertificateStatus().equals(CertificateStatus.USED)
            && !certificate.getOrder().getUser().getUuid().equals(userUuid)) {
            return CertificateDto.builder()
                .code(certificate.getCode())
                .creationDate(certificate.getCreationDate())
                .expirationDate(certificate.getExpirationDate())
                .certificateStatus(certificate.getCertificateStatus().toString())
                .build();
        }
        return modelMapper.map(certificate, CertificateDto.class);
    }

    private void checkSumIfCourierLimitBySumOfOrder(TariffsInfo tariffsInfo, Long sumWithoutDiscountInCoins) {
        if (CourierLimit.LIMIT_BY_SUM_OF_ORDER.equals(tariffsInfo.getCourierLimit())) {
            if (sumWithoutDiscountInCoins < tariffsInfo.getMin() * 100) {
                throw new BadRequestException(PRICE_OF_ORDER_LOWER_THAN_LIMIT + tariffsInfo.getMin());
            }
            if (tariffsInfo.getMax() != null && sumWithoutDiscountInCoins > tariffsInfo.getMax() * 100) {
                throw new BadRequestException(PRICE_OF_ORDER_GREATER_THAN_LIMIT + tariffsInfo.getMax());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public WayForPayOrderResponse saveFullOrderToDB(OrderResponseDto dto, String uuid, Long orderId) {
        final User currentUser = userRepository.findByUuid(uuid);
        if (!checkIfAddressMatchLocationArea(dto.getLocationId(), dto.getAddressId())) {
            throw new AddressNotWithinLocationAreaException(ADDRESS_NOT_WITHIN_LOCATION_AREA_MESSAGE);
        }

        TariffsInfo tariffsInfo = tryToFindTariffsInfoByBagIds(getBagIds(dto.getBags()), dto.getLocationId());
        List<OrderBag> bagsOrdered = new ArrayList<>();

        if (!dto.isShouldBePaid()) {
            dto.setCertificates(Collections.emptySet());
            dto.setPointsToUse(0);
        }

        long sumToPayWithoutDiscountInCoins = formBagsToBeSavedAndCalculateOrderSum(bagsOrdered,
            dto.getBags(), tariffsInfo);
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        long sumToPayInCoins = reduceOrderSumDueToUsedPoints(sumToPayWithoutDiscountInCoins, dto.getPointsToUse());

        Order order = isExistOrder(dto, orderId);
        if (orderId != null) {
            checkIsOrderOfCurrentUser(currentUser, order);
        }
        order.setTariffsInfo(tariffsInfo);
        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPayInCoins = formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPayInCoins);

        UBSuser userData =
            formUserDataToBeSaved(dto.getPersonalData(), dto.getAddressId(), dto.getLocationId(), currentUser);

        getOrder(dto, currentUser, bagsOrdered, sumToPayInCoins, order, orderCertificates, userData);
        eventService.save(OrderHistory.ORDER_FORMED, OrderHistory.CLIENT, order);

        notificationService.notifyCreatedOrder(order);

        if (sumToPayInCoins <= 0 || !dto.isShouldBePaid()) {
            return getPaymentRequestDto(order, "");
        } else {
            PaymentRequestDto requestDto = formPaymentRequest(order.getId(), sumToPayInCoins);
            String link = getLinkFromWayForPayCheckoutResponse(wayForPayClient.getCheckOutResponse(requestDto));
            return getPaymentRequestDto(order, link);
        }
    }

    private boolean checkIfAddressMatchLocationArea(long locationId, long addressId) {
        Address address = addressRepo.findById(addressId)
            .orElseThrow(() -> new EntityNotFoundException(ADDRESS_NOT_FOUND_BY_ID_MESSAGE + addressId));

        boolean isKyivTariff = checkIfCityBelongsToKyivTariff(address.getCityEn());

        if (locationId == TariffLocation.KYIV_TARIFF.getLocationId()) {
            return isKyivTariff;
        } else if (locationId == TariffLocation.KYIV_REGION_20_KM_TARIFF.getLocationId()) {
            checkAndCalculateAddressCoordinatesIfEmpty(address);

            double addressLatitude = address.getCoordinates().getLatitude();
            double addressLongitude = address.getCoordinates().getLongitude();

            double distanceInKm =
                DistanceCalculationUtils.calculateDistanceInKmByHaversineFormula(KYIV_LATITUDE, KYIV_LONGITUDE,
                    addressLatitude, addressLongitude);

            return distanceInKm <= LOCATION_40_KM_ZONE_VALUE && !isKyivTariff;
        } else {
            return locationRepository.findAddressAndLocationNamesMatch(locationId, addressId).isPresent();
        }
    }

    private boolean checkIfCityBelongsToKyivTariff(String cityName) {
        return Arrays.stream(KyivTariffLocation.values())
            .anyMatch(kyivTariffLocation -> kyivTariffLocation.getLocationName().equalsIgnoreCase(cityName));
    }

    private void checkAndCalculateAddressCoordinatesIfEmpty(Address address) {
        if (address.getCoordinates().getLatitude() == 0.0 && address.getCoordinates().getLongitude() == 0.0) {
            LatLng latLng = googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(UKRAINE_EN, address.getCityEn(),
                    LANG_EN).geometry.location;
            Coordinates addressCoordinates = Coordinates.builder().latitude(latLng.lat).longitude(latLng.lng).build();
            address.setCoordinates(addressCoordinates);
            addressRepo.save(address);
        }
    }

    private List<Integer> getBagIds(List<BagDto> dto) {
        return dto.stream()
            .map(BagDto::getId)
            .collect(Collectors.toList());
    }

    private Bag findActiveBagById(Integer id) {
        return bagRepository.findActiveBagById(id)
            .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + id));
    }

    private TariffsInfo tryToFindTariffsInfoByBagIds(List<Integer> bagIds, Long locationId) {
        return tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(bagIds, locationId)
            .orElseThrow(() -> new NotFoundException(TARIFF_FOR_LOCATION_NOT_EXIST + locationId));
    }

    private Order isExistOrder(OrderResponseDto dto, Long orderId) {
        if (orderId != null) {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
            checkIsOrderPaid(order.getOrderPaymentStatus());
            order.setPointsToUse(dto.getPointsToUse())
                .setAdditionalOrders(dto.getAdditionalOrders())
                .setComment(dto.getOrderComment());
            return order;
        } else {
            return modelMapper.map(dto, Order.class);
        }
    }

    private WayForPayOrderResponse getPaymentRequestDto(Order order, String link) {
        return WayForPayOrderResponse.builder()
            .orderId(order.getId())
            .link(link)
            .build();
    }

    @Override
    public FondyPaymentResponse getPaymentResponseFromFondy(Long id, String uuid) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_PAYMENT_STATUS);
        }
        if (order.getPayment().isEmpty()) {
            throw new NotFoundException(PAYMENT_NOT_FOUND + id);
        }
        return getFondyPaymentResponse(order);
    }

    private FondyPaymentResponse getFondyPaymentResponse(Order order) {
        Payment payment = order.getPayment().getLast();
        return FondyPaymentResponse.builder()
            .paymentStatus(payment.getPaymentStatus().name().equals("PAID") ? "success" : null)
            .build();
    }

    private void checkIfAddressHasBeenDeleted(Address address) {
        if (address.getAddressStatus().equals(AddressStatus.DELETED)) {
            throw new NotFoundException(
                NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkAddressUser(Address address, User user) {
        if (!address.getUser().equals(user)) {
            throw new NotFoundException(
                NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkIfUserHaveEnoughPoints(Integer i1, Integer i2) {
        if (i1 < i2) {
            throw new BadRequestException(USER_DONT_HAVE_ENOUGH_POINTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto findAllAddressesForCurrentOrder(String uuid) {
        Long id = userRepository.findByUuid(uuid).getId();
        List<AddressDto> addressDtoList = addressRepo.findAllNonDeletedAddressesByUserId(id)
            .stream()
            .sorted(Comparator.comparing(Address::getId))
            .map(u -> modelMapper.map(u, AddressDto.class))
            .collect(toList());
        return new OrderWithAddressesResponseDto(addressDtoList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderWithAddressesResponseDto saveCurrentAddressForOrder(CreateAddressRequestDto addressRequestDto,
        String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        List<Address> addresses = addressRepo.findAllNonDeletedAddressesByUserId(currentUser.getId());

        if (addresses.size() == MAXIMUM_NUMBER_OF_ADDRESSES) {
            throw new BadRequestException(NUMBER_OF_ADDRESSES_EXCEEDED);
        }

        if (addressRequestDto.getPlaceId().isEmpty()) {
            checkIfAddressExistIgnorePlaceId(addresses, addressRequestDto);
            saveAddressWithoutPlaceId(addresses, addressRequestDto, currentUser);
            return findAllAddressesForCurrentOrder(uuid);
        }

        checkIfAddressExistIgnorePlaceId(addresses, addressRequestDto);

        Address address = modelMapper.map(addressRequestDto, Address.class);
        address.setHouseNumber(addressRequestDto.getHouseNumber());
        address.setHouseCorpus(addressRequestDto.getHouseCorpus());

        setLocations(addressRequestDto, address);

        address.setCoordinates(setCoordinates(addressRequestDto.getPlaceId()));
        address.setAddressStatus(AddressStatus.NEW);
        address.setUser(currentUser);
        address.setActual(addresses.isEmpty());
        address.setAddressComment(addressRequestDto.getAddressComment());

        addressRepo.save(address);

        return findAllAddressesForCurrentOrder(uuid);
    }

    private Coordinates setCoordinates(String placeId) {
        GeocodingResult result = googleApiService.getResultFromGeoCode(placeId, 1);
        return Coordinates.builder()
            .latitude(result.geometry.location.lat)
            .longitude(result.geometry.location.lng)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto updateCurrentAddressForOrder(OrderAddressDtoRequest addressRequestDto,
        String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        Address address = addressRepo.findById(addressRequestDto.getId())
            .orElseThrow(() -> new NotFoundException(
                NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressRequestDto.getId()));

        if (Objects.isNull(currentUser)) {
            throw new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST);
        }

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }

        List<Address> addresses = addressRepo.findAllNonDeletedAddressesByUserId(currentUser.getId());

        if (addressRequestDto.getPlaceId() == null) {
            Address addressWithNullPlaceId = modelMapper.map(addressRequestDto, Address.class);
            addressWithNullPlaceId.setUser(currentUser);
            addressWithNullPlaceId.setAddressStatus(address.getAddressStatus());
            addressRepo.save(addressWithNullPlaceId);
            return findAllAddressesForCurrentOrder(uuid);
        }

        OrderAddressDtoRequest dtoRequest = getLocationDto(addressRequestDto.getPlaceId());
        checkNullFieldsOnGoogleResponse(dtoRequest, addressRequestDto);

        checkIfAddressExist(addresses, dtoRequest);

        Address newAddress = modelMapper.map(dtoRequest, Address.class);

        newAddress.setUser(address.getUser());
        newAddress.setAddressStatus(address.getAddressStatus());
        newAddress.setActual(address.getActual());

        newAddress.setDistrict(addressRequestDto.getDistrict());
        newAddress.setDistrictEn(addressRequestDto.getDistrictEn());

        addressRepo.save(newAddress);

        return findAllAddressesForCurrentOrder(uuid);
    }

    private void saveAddressWithoutPlaceId(List<Address> addresses, CreateAddressRequestDto addressRequestDto,
        User currentUser) {
        Address address = modelMapper.map(addressRequestDto, Address.class);

        setLocations(addressRequestDto, address);

        address.setCoordinates(Coordinates.builder().latitude(0.0).longitude(0.0).build());

        address.setUser(currentUser);
        address.setActual(addresses.isEmpty());
        address.setAddressStatus(AddressStatus.NEW);

        addressRepo.save(address);
    }

    private void setLocations(CreateAddressRequestDto addressRequestDto, Address address) {
        Optional<Region> optionalRegion =
            regionRepository.findRegionByNameEnOrNameUk(address.getRegionEn(), address.getRegion());

        if (optionalRegion.isPresent()) {
            address.setRegionId(optionalRegion.get());

            checkIfAddressBelongToKyiv(optionalRegion.get(), address);

            Optional<City> optionalCity = cityRepository
                .findCityByRegionIdAndNameUkAndNameEn(optionalRegion.get().getId(), address.getCity(),
                    address.getCityEn());

            City city;
            if (optionalCity.isPresent()) {
                city = optionalCity.get();
            } else {
                city = modelMapper.map(addressRequestDto, City.class);
                city.setRegion(optionalRegion.get());
                checkIfAddressBelongToKyiv(optionalRegion.get(), address);
                city = cityRepository.save(city);
            }

            address.setCityId(city);

            Optional<District> optionalDistrict = districtRepository
                .findDistrictByCityIdAndNameEnOrNameUk(city.getId(), address.getDistrictEn(), address.getDistrict());

            if (optionalDistrict.isPresent()) {
                address.setDistrictId(optionalDistrict.get());
            } else {
                District district = modelMapper.map(addressRequestDto, District.class);
                district.setCity(city);
                District savedDistrict = districtRepository.save(district);
                address.setDistrictId(savedDistrict);
            }
        } else {
            throw new BadRequestException(ErrorMessage.REGION_NOT_FOUND);
        }
    }

    private void checkIfAddressExist(List<Address> addresses, OrderAddressDtoRequest dtoRequest) {
        boolean exist = addresses.stream()
            .map(address -> modelMapper.map(address, OrderAddressDtoRequest.class))
            .anyMatch(addressDto -> addressDto.equals(dtoRequest));

        if (exist) {
            throw new BadRequestException(ADDRESS_ALREADY_EXISTS);
        }
    }

    private void checkIfAddressExistIgnorePlaceId(List<Address> addresses,
        CreateAddressRequestDto addressRequestDto) {
        boolean exist = addresses.stream()
            .map(address -> modelMapper.map(address, CreateAddressRequestDto.class))
            .anyMatch(addressDto -> addressDto.equals(addressRequestDto));

        if (exist) {
            throw new BadRequestException(ADDRESS_ALREADY_EXISTS);
        }
    }

    private Map<AddressComponentType, Consumer<String>> initializeUkrainianGeoCodingResult(
        OrderAddressDtoRequest dtoRequest) {
        return Map.of(
            AddressComponentType.LOCALITY, dtoRequest::setCity,
            AddressComponentType.ROUTE, dtoRequest::setStreet,
            AddressComponentType.STREET_NUMBER, dtoRequest::setHouseNumber,
            AddressComponentType.SUBLOCALITY, dtoRequest::setDistrict,
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1, dtoRequest::setRegion);
    }

    private Map<AddressComponentType, Consumer<String>> initializeEnglishGeoCodingResult(
        OrderAddressDtoRequest dtoRequest) {
        return Map.of(
            AddressComponentType.LOCALITY, dtoRequest::setCityEn,
            AddressComponentType.ROUTE, dtoRequest::setStreetEn,
            AddressComponentType.SUBLOCALITY, dtoRequest::setDistrictEn,
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1, dtoRequest::setRegionEn);
    }

    private void initializeGeoCodingResults(Map<AddressComponentType, Consumer<String>> initializedMap,
        GeocodingResult geocodingResult) {
        initializedMap
            .forEach((key, value) -> Arrays.stream(geocodingResult.addressComponents)
                .forEach(addressComponent -> Arrays.stream(addressComponent.types)
                    .filter(componentType -> componentType.equals(key))
                    .forEach(componentType -> value.accept(addressComponent.longName))));
    }

    private OrderAddressDtoRequest getLocationDto(String placeId) {
        GeocodingResult resultsUa = googleApiService.getResultFromGeoCode(placeId, 0);
        GeocodingResult resultsEn = googleApiService.getResultFromGeoCode(placeId, 1);

        OrderAddressDtoRequest orderAddressDtoRequest = new OrderAddressDtoRequest();
        initializeGeoCodingResults(initializeUkrainianGeoCodingResult(orderAddressDtoRequest), resultsUa);
        initializeGeoCodingResults(initializeEnglishGeoCodingResult(orderAddressDtoRequest), resultsEn);

        double latitude = resultsEn.geometry.location.lat;
        double longitude = resultsEn.geometry.location.lng;
        orderAddressDtoRequest.setCoordinates(new Coordinates(latitude, longitude));

        checkIfAddressBelongToKyiv(orderAddressDtoRequest);

        return orderAddressDtoRequest;
    }

    /**
     * When Google API sets region name, there's a special case for Kyiv, the
     * capital of Ukraine. Google API sets the 'Kyiv' as the name of region for
     * addresses from Kyiv instead of Kyiv Region. Therefore, a separate logic is
     * implemented to set 'Kyiv Region' name for such addresses instead of 'Kyiv'.
     *
     * @param request OrderAddressDtoRequest.
     */
    private void checkIfAddressBelongToKyiv(OrderAddressDtoRequest request) {
        if (request.getRegion().equalsIgnoreCase(KYIV_UA)) {
            request.setRegion(KYIV_REGION_UA);
        }

        if (request.getRegionEn().equalsIgnoreCase(KYIV_EN)) {
            request.setRegionEn(KYIV_REGION_EN);
        }
    }

    private void checkIfAddressBelongToKyiv(Region region, Address address) {
        if (region.getNameUk().equalsIgnoreCase(KYIV_UA)) {
            address.setRegion(KYIV_REGION_UA);
            // todo: set Kyiv oblast region id
        }

        if (region.getNameEn().equalsIgnoreCase(KYIV_EN)) {
            address.setRegionEn(KYIV_REGION_EN);
            // todo: set Kyiv oblast region id
        }
    }

    private void checkNullFieldsOnGoogleResponse(OrderAddressDtoRequest dtoRequest,
        OrderAddressDtoRequest addressRequestDto) {
        dtoRequest.setRegion(
            Objects.isNull(dtoRequest.getRegion()) ? addressRequestDto.getRegion() : dtoRequest.getRegion());

        dtoRequest.setRegionEn(
            Objects.isNull(dtoRequest.getRegionEn()) ? addressRequestDto.getRegionEn() : dtoRequest.getRegionEn());

        dtoRequest.setDistrict(
            Objects.isNull(dtoRequest.getDistrict()) ? addressRequestDto.getDistrict() : dtoRequest.getDistrict());

        dtoRequest.setDistrictEn(
            Objects.isNull(dtoRequest.getDistrictEn()) ? addressRequestDto.getDistrictEn()
                : dtoRequest.getDistrictEn());

        dtoRequest.setHouseNumber(
            Objects.isNull(dtoRequest.getHouseNumber()) ? addressRequestDto.getHouseNumber()
                : dtoRequest.getHouseNumber());

        dtoRequest.setEntranceNumber(addressRequestDto.getEntranceNumber());
        dtoRequest.setHouseCorpus(addressRequestDto.getHouseCorpus());
        dtoRequest.setAddressComment(addressRequestDto.getAddressComment());
        dtoRequest.setId(addressRequestDto.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderWithAddressesResponseDto deleteCurrentAddressForOrder(Long addressId, String uuid) {
        Address address = addressRepo.findById(addressId).orElseThrow(
            () -> new NotFoundException(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));
        if (!Objects.equals(address.getUser().getUuid(), uuid)) {
            throw new AccessDeniedException(CANNOT_DELETE_ADDRESS);
        }
        if (address.getAddressStatus() == AddressStatus.DELETED) {
            throw new BadRequestException(CANNOT_DELETE_ALREADY_DELETED_ADDRESS);
        }
        address.setAddressStatus(AddressStatus.DELETED);

        if (Boolean.TRUE.equals(address.getActual())) {
            address.setActual(false);
            addressRepo.findAnyByUserIdAndAddressStatusNotDeleted(address.getUser().getId())
                .ifPresent(newActualAddress -> newActualAddress.setActual(true));
        }

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
    public PageableDto<OrdersDataForUserDto> getOrdersForUser(String uuid, Pageable page, List<OrderStatus> statuses) {
        Page<Order> orderPages = nonNull(statuses)
            ? ordersForUserRepository.getAllByUserUuidAndOrderStatusIn(page, uuid, statuses)
            : ordersForUserRepository.getAllByUserUuid(page, uuid);
        List<Order> orders = orderPages.getContent();
        List<OrdersDataForUserDto> dtos = new ArrayList<>();
        orders.forEach(order -> dtos.add(getOrdersData(order)));

        return new PageableDto<>(
            dtos,
            orderPages.getTotalElements(),
            orderPages.getPageable().getPageNumber(),
            orderPages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public OrdersDataForUserDto getOrderForUser(String uuid, Long id) {
        Order order = ordersForUserRepository.getAllByUserUuidAndId(uuid, id);
        if (order == null) {
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }

        return getOrdersData(order);
    }

    private OrdersDataForUserDto getOrdersData(Order order) {
        List<Payment> payments = order.getPayment();
        List<BagForUserDto> bagForUserDtos = bagForUserDtosBuilder(order);
        OrderStatusTranslation orderStatusTranslation = orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue())
            .orElse(orderStatusTranslationRepository.getReferenceById(1L));
        OrderPaymentStatusTranslation paymentStatusTranslation = orderPaymentStatusTranslationRepository
            .getById((long) order.getOrderPaymentStatus().getStatusValue());

        Long fullPriceInCoins = bagForUserDtos.stream()
            .map(b -> convertBillsIntoCoins(b.getTotalPrice()))
            .reduce(0L, Long::sum);

        List<CertificateDto> certificateDtos = order.getCertificates().stream()
            .map(certificate -> modelMapper.map(certificate, CertificateDto.class))
            .collect(toList());

        Long amountWithDiscountInCoins = fullPriceInCoins
            - 100L * (order.getPointsToUse() + countCertificatesBonuses(certificateDtos));

        Long paidAmountInCoins = countPaidAmount(payments);

        Double amountBeforePayment = convertCoinsIntoBills(amountWithDiscountInCoins - paidAmountInCoins);

        double refundedBonuses = order.getPayment().stream()
            .filter(payment -> ENROLLMENT_TO_THE_BONUS_ACCOUNT_ENG.equals(payment.getReceiptLink()))
            .map(payment -> payment.getAmount().doubleValue())
            .reduce(0.0, Double::sum);

        refundedBonuses /= -CURRENCY_CONVERSION_RATE;

        Double refundedMoney =
            order.getRefund() == null ? 0.0 : order.getRefund().getAmount().doubleValue() / CURRENCY_CONVERSION_RATE;

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
            .refundedBonuses(refundedBonuses)
            .refundedMoney(refundedMoney)
            .paidAmount(convertCoinsIntoBills(paidAmountInCoins))
            .orderFullPrice(convertCoinsIntoBills(fullPriceInCoins))
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
        if (sender.getSenderFirstName() != null && !sender.getSenderFirstName().isEmpty()
            && sender.getSenderLastName() != null && !sender.getSenderLastName().isEmpty()
            && sender.getSenderPhoneNumber() != null && !sender.getSenderPhoneNumber().isEmpty()) {
            return SenderInfoDto.builder()
                .senderName(sender.getSenderFirstName())
                .senderSurname(sender.getSenderLastName())
                .senderEmail(sender.getSenderEmail())
                .senderPhone(sender.getSenderPhoneNumber())
                .build();
        } else {
            return SenderInfoDto.builder()
                .senderName(sender.getFirstName())
                .senderSurname(sender.getLastName())
                .senderEmail(sender.getEmail())
                .senderPhone(sender.getPhoneNumber())
                .build();
        }
    }

    private AddressInfoDto addressInfoDtoBuilder(Order order) {
        var address = order.getUbsUser().getOrderAddress();
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
            .houseCorpus(address.getHouseCorpus())
            .houseNumber(address.getHouseNumber())
            .entranceNumber(address.getEntranceNumber())
            .build();
    }

    private List<BagForUserDto> bagForUserDtosBuilder(Order order) {
        List<OrderBag> bagsAmountInOrder = order.getOrderBags();
        Map<Integer, Integer> actualBagsAmount = orderBagService.getActualBagsAmountForOrder(bagsAmountInOrder);
        return bagsAmountInOrder.stream()
            .map(orderBag -> buildBagForUserDto(orderBag, actualBagsAmount.get(orderBag.getBag().getId())))
            .collect(toList());
    }

    private BagForUserDto buildBagForUserDto(OrderBag orderBag, int count) {
        BagForUserDto bagDto = modelMapper.map(orderBag, BagForUserDto.class);
        bagDto.setCount(count);
        bagDto.setTotalPrice(convertCoinsIntoBills(count * orderBag.getPrice()));
        return bagDto;
    }

    private Long countPaidAmount(List<Payment> payments) {
        return payments.stream()
            .filter(payment -> PaymentStatus.PAID.equals(payment.getPaymentStatus()))
            .map(Payment::getAmount)
            .reduce(0L, Long::sum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId, String uuid) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        User user = userRepository.findByUuid(uuid);
        if (!order.getUser().equals(user)) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }
        UserInfoDto userInfoDto = UserInfoDto.builder()
            .customerName(order.getUser().getRecipientName())
            .customerSurName(order.getUser().getRecipientSurname())
            .customerPhoneNumber(order.getUser().getRecipientPhone())
            .customerEmail(order.getUser().getRecipientEmail())
            .totalUserViolations(userRepository.countTotalUsersViolations(order.getUser().getId()))
            .recipientId(order.getUbsUser().getId())
            .userViolationForCurrentOrder(
                userRepository.checkIfUserHasViolationForCurrentOrder(order.getUser().getId(), order.getId()))
            .build();
        if (order.getUbsUser().getSenderFirstName() != null && !order.getUbsUser().getSenderFirstName().isEmpty()
            && order.getUbsUser().getSenderLastName() != null && !order.getUbsUser().getSenderLastName().isEmpty()
            && order.getUbsUser().getSenderPhoneNumber() != null
            && !order.getUbsUser().getSenderPhoneNumber().isEmpty()) {
            return userInfoDto.setRecipientName(order.getUbsUser().getSenderFirstName())
                .setRecipientSurName(order.getUbsUser().getSenderLastName())
                .setRecipientEmail(order.getUbsUser().getSenderEmail())
                .setRecipientPhoneNumber(order.getUbsUser().getSenderPhoneNumber());
        } else {
            return userInfoDto.setRecipientName(order.getUbsUser().getFirstName())
                .setRecipientSurName(order.getUbsUser().getLastName())
                .setRecipientEmail(order.getUbsUser().getEmail())
                .setRecipientPhoneNumber(order.getUbsUser().getPhoneNumber());
        }
    }

    /**
     * Method updates ubs_user information order in order.
     *
     * @param dtoUpdate of {@link UbsCustomersDtoUpdate} ubs_user_id;
     * @return {@link UbsCustomersDto};
     * @author Rusanovscaia Nadejda
     */
    @Override
    public UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String userUuid) {
        var ubsUser = getUbsUserById(dtoUpdate.getRecipientId());
        checkUserHasAccessToUpdateData(ubsUser, userUuid);

        ubsUserRepository.save(updateRecipientDataInOrder(ubsUser, dtoUpdate));
        eventService.save(OrderHistory.CHANGED_SENDER, ubsUser.getUser().getRecipientEmail(),
            ubsUser.getOrders().getFirst());

        return UbsCustomersDto.builder()
            .name(ubsUser.getSenderFirstName() + " " + ubsUser.getSenderLastName())
            .email(ubsUser.getSenderEmail())
            .phoneNumber(ubsUser.getSenderPhoneNumber())
            .build();
    }

    private UBSuser getUbsUserById(Long recipientId) {
        return ubsUserRepository.findById(recipientId)
            .orElseThrow(() -> new UBSuserNotFoundException(RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST + recipientId));
    }

    private void checkUserHasAccessToUpdateData(UBSuser ubsUser, String userUuid) {
        var uuid = ubsUser.getUser().getUuid();
        if (checkUserRoleIsUser() && !(uuid.equals(userUuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }
    }

    private boolean checkUserRoleIsUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(USER_WITH_PREFIX));
    }

    @Override
    public Long createUserProfile(UserProfileCreateDto userProfileCreateDto) {
        if (!userRemoteClient.checkIfUserExistsByUuid(userProfileCreateDto.getUuid())) {
            throw new NotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST);
        }
        User user = userRepository.findByUuid(userProfileCreateDto.getUuid());
        if (user == null) {
            user = userRepository.save(User.builder()
                .uuid(userProfileCreateDto.getUuid())
                .recipientEmail(userProfileCreateDto.getEmail())
                .recipientName(userProfileCreateDto.getName())
                .currentPoints(0)
                .violations(0)
                .dateOfRegistration(LocalDate.now()).build());
        }
        return user.getId();
    }

    private UBSuser updateRecipientDataInOrder(UBSuser ubsUser, UbsCustomersDtoUpdate dto) {
        if (nonNull(dto.getRecipientEmail())) {
            ubsUser.setSenderEmail(dto.getRecipientEmail());
        }
        if (nonNull(dto.getRecipientName())) {
            ubsUser.setSenderFirstName(dto.getRecipientName());
        }
        if (nonNull(dto.getRecipientSurName())) {
            ubsUser.setSenderLastName(dto.getRecipientSurName());
        }
        if (nonNull(dto.getRecipientPhoneNumber())) {
            ubsUser.setSenderPhoneNumber(dto.getRecipientPhoneNumber());
        }

        return ubsUser;
    }

    private void formAndSaveOrder(Order order, Set<Certificate> orderCertificates,
        List<OrderBag> bagsOrdered, UBSuser userData,
        User currentUser, long sumToPayInCoins) {
        order.setOrderStatus(OrderStatus.FORMED);
        order.setCertificates(orderCertificates);
        order.updateWithNewOrderBags(bagsOrdered);
        order.setUbsUser(userData);
        order.setUser(currentUser);
        order.setSumTotalAmountWithoutDiscounts(
            calculateOrderSumWithoutDiscounts(bagsOrdered));
        setOrderPaymentStatus(order, sumToPayInCoins);

        Payment payment = Payment.builder()
            .amount(sumToPayInCoins)
            .orderStatus("created")
            .currency("UAH")
            .paymentStatus(PaymentStatus.UNPAID)
            .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .order(order).build();

        if (order.getPayment() == null) {
            order.setPayment(new ArrayList<>());
        }
        order.getPayment().add(payment);
        orderRepository.save(order);
    }

    private void setOrderPaymentStatus(Order order, long sumToPay) {
        if (sumToPay <= 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        } else {
            order.setOrderPaymentStatus(
                order.getPointsToUse() > 0 || CollectionUtils.isNotEmpty(order.getCertificates())
                    ? OrderPaymentStatus.HALF_PAID
                    : OrderPaymentStatus.UNPAID);
        }
    }

    private PaymentRequestDto formPaymentRequest(Long orderId, long sumToPayInCoins) {
        Instant instant = Instant.now();
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
            .transactionType("CREATE_INVOICE")
            .merchantAccount(merchantAccount)
            .merchantDomainName(merchantDomainName)
            .apiVersion(1)
            .serviceUrl(resultWayForPayUrl)
            .orderReference(OrderUtils.generateOrderIdForPayment(orderId, order))
            .orderDate(instant.getEpochSecond())
            .amount(convertCoinsIntoBills(sumToPayInCoins).intValue())
            .currency("UAH")
            .productName(order.getOrderBags().stream()
                .filter(bag -> bag.getAmount() != 0)
                .map(orderBag -> orderBag.getName().trim())
                .flatMap(name -> Arrays.stream(name.split(",")))
                .toList())
            .productPrice(order.getOrderBags().stream()
                .filter(bag -> bag.getAmount() != 0)
                .map(product -> convertCoinsIntoBills(product.getPrice()).intValue())
                .toList())
            .productCount(order.getOrderBags().stream()
                .map(OrderBag::getAmount)
                .filter(amount -> amount != 0)
                .toList())
            .build();

        paymentRequestDto.setSignature(encryptionUtil
            .formRequestSignature(paymentRequestDto, wayForPaySecret));

        return paymentRequestDto;
    }

    private Double convertCoinsIntoBills(Long coins) {
        return BigDecimal.valueOf(coins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private UBSuser formUserDataToBeSaved(PersonalDataDto dto, Long addressId, Long locationId, User currentUser) {
        UBSuser ubsUserFromDatabaseById = null;
        if (dto.getUbsUserId() != null) {
            ubsUserFromDatabaseById =
                ubsUserRepository.findById(dto.getUbsUserId())
                    .orElseThrow(() -> new BadRequestException(THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST
                        + dto.getUbsUserId()));
        }
        UBSuser mappedFromDtoUser = modelMapper.map(dto, UBSuser.class);
        mappedFromDtoUser.setUser(currentUser);
        mappedFromDtoUser.setPhoneNumber(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(mappedFromDtoUser.getPhoneNumber()));
        if (mappedFromDtoUser.getId() == null || !mappedFromDtoUser.equals(ubsUserFromDatabaseById)) {
            mappedFromDtoUser.setId(null);
            mappedFromDtoUser.setOrderAddress(saveOrderAddressWithLocation(addressId, locationId, currentUser));
            if (mappedFromDtoUser.getOrderAddress().getAddressComment() == null) {
                mappedFromDtoUser.getOrderAddress().setAddressComment(dto.getAddressComment());
            }
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
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return buildOrderPaymentDetailDto(order);
    }

    private OrderPaymentDetailDto buildOrderPaymentDetailDto(Order order) {
        int certificatePointsInCoins = order.getCertificates().stream()
            .flatMapToInt(c -> IntStream.of(c.getPoints()))
            .reduce(Integer::sum).orElse(0) * 100;
        int pointsToUseInCoins = order.getPointsToUse() * 100;
        long amountInCoins = order.getPayment().stream()
            .flatMapToLong(p -> LongStream.of(p.getAmount()))
            .reduce(Long::sum).orElse(0);
        String currency = order.getPayment().isEmpty() ? "UAH" : order.getPayment().getFirst().getCurrency();
        return OrderPaymentDetailDto.builder()
            .amount(amountInCoins != 0L ? amountInCoins + certificatePointsInCoins + pointsToUseInCoins : 0L)
            .certificates(-certificatePointsInCoins)
            .pointsToUse(-pointsToUseInCoins)
            .amountToPay(amountInCoins)
            .currency(currency)
            .build();
    }

    private long formCertificatesToBeSavedAndCalculateOrderSum(OrderResponseDto dto, Set<Certificate> orderCertificates,
        Order order, long sumToPayInCoins) {
        if (sumToPayInCoins != 0 && dto.getCertificates() != null) {
            for (String temp : dto.getCertificates()) {
                if (dto.getCertificates().size() > 5) {
                    throw new BadRequestException(TOO_MANY_CERTIFICATES);
                }
                Certificate certificate = certificateRepository.findById(temp).orElseThrow(
                    () -> new NotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + temp));
                validateCertificate(certificate);
                certificate.setOrder(order);
                orderCertificates.add(certificate);
                sumToPayInCoins -= certificate.getPoints() * 100L;
                certificate.setCertificateStatus(CertificateStatus.USED);
                certificate.setDateOfUse(LocalDate.now());
                if (dontSendLinkToFondyIf(sumToPayInCoins, certificate)) {
                    sumToPayInCoins = 0L;
                }
            }
        }
        return sumToPayInCoins;
    }

    private boolean dontSendLinkToFondyIf(long sumToPayInCoins, Certificate certificate) {
        if (sumToPayInCoins <= 0) {
            certificate.setCertificateStatus(CertificateStatus.USED);
            certificate.setPoints(certificate.getPoints()
                + BigDecimal.valueOf(sumToPayInCoins)
                    .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                    .setScale(0, RoundingMode.UP).intValue());
            return true;
        }
        return false;
    }

    private void checkAmountOfBagsIfCourierLimitByAmountOfBag(TariffsInfo courierLocation, Integer countOfBigBag) {
        if (CourierLimit.LIMIT_BY_AMOUNT_OF_BAG.equals(courierLocation.getCourierLimit())) {
            if (courierLocation.getMin() > countOfBigBag) {
                throw new BadRequestException(NOT_ENOUGH_BAGS_EXCEPTION + courierLocation.getMin());
            }
            if (courierLocation.getMax() != null && courierLocation.getMax() < countOfBigBag) {
                throw new BadRequestException(TO_MUCH_BAG_EXCEPTION + courierLocation.getMax());
            }
        }
    }

    private long calculateOrderSumWithoutDiscounts(List<OrderBag> getOrderBagsAndQuantity) {
        return getOrderBagsAndQuantity.stream()
            .map(orderBag -> orderBag.getPrice() * orderBag.getAmount())
            .reduce(0L, Long::sum);
    }

    private long formBagsToBeSavedAndCalculateOrderSum(List<OrderBag> orderBagList, List<BagDto> bags,
        TariffsInfo tariffsInfo) {
        long totalSumToPayInCoins = 0L;
        long limitedSumToPayInCoins = 0L;
        int limitedBags = 0;
        final List<Integer> bagIds = bags.stream().map(BagDto::getId).toList();
        for (BagDto temp : bags) {
            Bag bag = findActiveBagById(temp.getId());
            if (Boolean.TRUE.equals(bag.getLimitIncluded())) {
                limitedSumToPayInCoins += bag.getFullPrice() * temp.getAmount();
                limitedBags += temp.getAmount();
            } else {
                totalSumToPayInCoins += bag.getFullPrice() * temp.getAmount();
            }
            OrderBag orderBag = createOrderBag(bag);
            orderBag.setAmount(temp.getAmount());
            orderBagList.add(orderBag);
        }
        checkSumIfCourierLimitBySumOfOrder(tariffsInfo, limitedSumToPayInCoins);
        checkAmountOfBagsIfCourierLimitByAmountOfBag(tariffsInfo, limitedBags);
        totalSumToPayInCoins += limitedSumToPayInCoins;
        List<OrderBag> notOrderedBags = tariffsInfo.getBags().stream()
            .filter(orderBag -> orderBag.getStatus() == BagStatus.ACTIVE && !bagIds.contains(orderBag.getId()))
            .map(this::createOrderBag)
            .toList();
        orderBagList.addAll(notOrderedBags.stream()
            .map(this::setAmountToOrderBag)
            .toList());
        return totalSumToPayInCoins;
    }

    private OrderBag setAmountToOrderBag(OrderBag orderBag) {
        orderBag.setAmount(0);
        return orderBag;
    }

    private OrderBag createOrderBag(Bag bag) {
        return OrderBag.builder()
            .bag(bag)
            .capacity(bag.getCapacity())
            .price(bag.getFullPrice())
            .name(bag.getName())
            .nameEng(bag.getNameEng())
            .build();
    }

    private void validateCertificate(Certificate certificate) {
        if (certificate.getCertificateStatus() == CertificateStatus.NEW) {
            throw new CertificateIsNotActivated(CERTIFICATE_IS_NOT_ACTIVATED + certificate.getCode());
        } else if (certificate.getCertificateStatus() == CertificateStatus.USED) {
            throw new BadRequestException(CERTIFICATE_IS_USED + certificate.getCode());
        } else {
            if (LocalDate.now().isAfter(certificate.getExpirationDate())) {
                throw new BadRequestException(CERTIFICATE_EXPIRED + certificate.getCode());
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
                .collect(toList());
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
    public List<EventDto> getAllEventsForOrder(Long orderId, String email, String language) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }
        List<Event> orderEvents = eventRepository.findAllEventsByOrderId(orderId);
        if (orderEvents.isEmpty()) {
            throw new NotFoundException(EVENTS_NOT_FOUND_EXCEPTION + orderId);
        }
        if (LANGUAGE_EN.equals(language)) {
            return orderEvents
                .stream()
                .peek(event -> {
                    event.setEventName(event.getEventNameEng());
                    event.setAuthorName(event.getAuthorNameEng());
                })
                .map(event -> modelMapper.map(event, EventDto.class))
                .collect(toList());
        }
        return orderEvents
            .stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserProfileUpdateDto updateProfileData(String uuid, UserProfileUpdateDto userProfileUpdateDto) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        setUserData(user, userProfileUpdateDto);
        setTelegramAndViberBots(user, userProfileUpdateDto.getTelegramIsNotify(),
            userProfileUpdateDto.getViberIsNotify());
        userProfileUpdateDto.getAddressDto().stream()
            .map(a -> modelMapper.map(a, OrderAddressDtoRequest.class))
            .forEach(addressRequestDto -> updateCurrentAddressForOrder(addressRequestDto, uuid));
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserProfileUpdateDto.class);
    }

    @Override
    public UserProfileDto getProfileData(String uuid) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        List<Address> allAddress = addressRepo.findAllNonDeletedAddressesByUserId(user.getId());
        UserProfileDto userProfileDto = modelMapper.map(user, UserProfileDto.class);
        List<Bot> botList = getListOfBots(user.getUuid());
        List<AddressDto> addressDto =
            allAddress.stream()
                .map(a -> modelMapper.map(a, AddressDto.class))
                .collect(toList());
        userProfileDto.setAddressDto(addressDto);
        userProfileDto.setBotList(botList);
        userProfileDto.setHasPassword(userRemoteClient.getPasswordStatus().isHasPassword());
        return userProfileDto;
    }

    private void setUserData(User user, UserProfileUpdateDto userProfileUpdateDto) {
        user.setRecipientName(userProfileUpdateDto.getRecipientName());
        user.setRecipientSurname(userProfileUpdateDto.getRecipientSurname());
        user.setAlternateEmail(userProfileUpdateDto.getAlternateEmail());
        user.setRecipientPhone(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(userProfileUpdateDto.getRecipientPhone()));
    }

    private void setTelegramAndViberBots(User user, Boolean telegramIsNotify, Boolean viberIsNotify) {
        TelegramBot telegramBot = telegramBotRepository.findByUser(user).orElse(null);
        ViberBot viberBot = viberBotRepository.findByUser(user).orElse(null);
        if (telegramBot != null) {
            telegramBot.setIsNotify(telegramIsNotify);
            user.setTelegramBot(telegramBot);
        }
        if (viberBot != null) {
            viberBot.setIsNotify(viberIsNotify);
            user.setViberBot(viberBot);
        }
    }

    @Override
    public void markUserAsDeactivated(String uuid, DeactivateUserRequestDto request) {
        User currentUser = userRepository.findByUuid(uuid);
        if (currentUser == null) {
            throw new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST);
        }
        userRemoteClient.markUserDeactivated(currentUser.getUuid(), request);
    }

    @Override
    public OrderCancellationReasonDto getOrderCancellationReason(final Long orderId, String uuid) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_ORDER_CANCELLATION_REASON);
        }
        return OrderCancellationReasonDto.builder()
            .cancellationReason(order.getCancellationReason())
            .cancellationComment(order.getCancellationComment())
            .build();
    }

    private long reduceOrderSumDueToUsedPoints(long sumToPayInCoins, int pointsToUse) {
        if (sumToPayInCoins >= pointsToUse * 100L) {
            sumToPayInCoins -= pointsToUse * 100L;
        }
        return sumToPayInCoins;
    }

    private void getOrder(OrderResponseDto dto, User currentUser, List<OrderBag> amountOfBagsOrdered,
        long sumToPayInCoins, Order order, Set<Certificate> orderCertificates, UBSuser userData) {
        formAndSaveOrder(order, orderCertificates, amountOfBagsOrdered, userData, currentUser, sumToPayInCoins);

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);
    }

    private OrderAddress saveOrderAddressWithLocation(Long addressId, Long locationId, User currentUser) {
        var address = addressRepo.findById(addressId)
            .orElseThrow(() -> new NotFoundException(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));

        var location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException(LOCATION_DOESNT_FOUND_BY_ID + locationId));

        checkIfAddressHasBeenDeleted(address);

        checkAddressUser(address, currentUser);

        var orderAddress = modelMapper.map(address, OrderAddress.class);

        location.addOrderAddress(orderAddress);

        orderAddressRepository.save(orderAddress);

        return orderAddress;
    }

    @Override
    public void deleteOrder(String uuid, Long id) {
        Order order = ordersForUserRepository.getAllByUserUuidAndId(uuid, id);
        if (order == null) {
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }
        order.updateWithNewOrderBags(Collections.emptyList());
        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    private Long convertBillsIntoCoins(Double bills) {
        return BigDecimal.valueOf(bills)
            .movePointRight(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(AppConstant.NO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
            .longValue();
    }

    private void checkIsOrderPaid(OrderPaymentStatus orderPaymentStatus) {
        if (OrderPaymentStatus.PAID.equals(orderPaymentStatus)) {
            throw new BadRequestException(ORDER_ALREADY_PAID);
        }
    }

    private String getLinkFromWayForPayCheckoutResponse(String wayForPayResponse) {
        JSONObject json = new JSONObject(wayForPayResponse);
        return json.getString("invoiceUrl");
    }

    private void checkResponseStatusFailure(PaymentResponseDto dto, Payment orderPayment, Order order) {
        if (dto.getTransactionStatus().equals(FAILED_STATUS)) {
            orderPayment.setPaymentStatus(PaymentStatus.UNPAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
        }
    }

    protected void checkOrderStatusApproved(PaymentResponseDto dto, Payment orderPayment, Order order) {
        if (dto.getTransactionStatus().equals(APPROVED_STATUS)) {
            orderPayment.setPaymentId(String.valueOf(dto.getOrderReference().split("_")[1]));
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
            .collect(toList());
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

    private List<AllActiveLocationsDto> getAllActiveLocationsByCourierId(Long courierId) {
        List<Location> locations = locationRepository.findAllActiveLocationsByCourierId(courierId);
        return getAllActiveLocationsDtos(locations);
    }

    private List<AllActiveLocationsDto> getAllActiveLocationsDtos(List<Location> locations) {
        Map<RegionDto, List<LocationsDtos>> map = locations.stream()
            .collect(toMap(x -> modelMapper.map(x, RegionDto.class),
                x -> new ArrayList<>(List.of(modelMapper.map(x, LocationsDtos.class))),
                (x, y) -> {
                    x.addAll(y);
                    return new ArrayList<>(x).stream().distinct().collect(toList());
                }));

        return map.entrySet().stream()
            .map(x -> AllActiveLocationsDto.builder()
                .regionId(x.getKey().getRegionId())
                .nameEn(x.getKey().getNameEn())
                .nameUk(x.getKey().getNameUk())
                .locations(x.getValue())
                .build())
            .collect(toList());
    }

    @Override
    public OrderCourierPopUpDto getInfoForCourierOrderingByCourierId(String uuid, Optional<String> changeLoc,
        Long courierId) {
        if (!courierRepository.existsCourierById(courierId)) {
            throw new NotFoundException(COURIER_IS_NOT_FOUND_BY_ID + courierId);
        }

        OrderCourierPopUpDto orderCourierPopUpDto = new OrderCourierPopUpDto();
        if (changeLoc.isPresent()) {
            orderCourierPopUpDto.setOrderIsPresent(false);
            orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocationsByCourierId(courierId));
            return orderCourierPopUpDto;
        }
        Optional<Order> lastOrder = orderRepository.getLastOrderOfUserByUUIDIfExists(uuid);
        if (lastOrder.isPresent()) {
            orderCourierPopUpDto.setOrderIsPresent(true);
            orderCourierPopUpDto.setTariffsForLocationDto(
                modelMapper.map(tariffsInfoRepository.findTariffsInfoByOrdersId(lastOrder.get().getId()),
                    TariffsForLocationDto.class));
        } else {
            orderCourierPopUpDto.setOrderIsPresent(false);
            orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocationsByCourierId(courierId));
        }
        return orderCourierPopUpDto;
    }

    @Override
    public List<CourierDto> getAllActiveCouriers() {
        return courierRepository.getAllActiveCouriers().stream()
            .map(courier -> modelMapper.map(courier, CourierDto.class))
            .toList();
    }

    private TariffsInfo findTariffsInfoByCourierAndLocationId(Long courierId, Long locationId) {
        return tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(courierId, locationId)
            .orElseThrow(
                () -> new NotFoundException(
                    String.format(TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST, courierId, locationId)));
    }

    @Override
    public OrderCourierPopUpDto getTariffInfoForLocation(Long courierId, Long locationId) {
        if (!courierRepository.existsCourierById(courierId)) {
            throw new NotFoundException(COURIER_IS_NOT_FOUND_BY_ID + courierId);
        }

        return OrderCourierPopUpDto.builder()
            .orderIsPresent(true)
            .tariffsForLocationDto(modelMapper.map(
                findTariffsInfoByCourierAndLocationId(courierId, locationId), TariffsForLocationDto.class))
            .build();
    }

    @Override
    public TariffsForLocationDto getTariffForOrder(Long id) {
        Optional<TariffsInfo> tariffsInfo = tariffsInfoRepository.findByOrdersId(id);
        if (tariffsInfo.isPresent()) {
            return modelMapper.map(tariffsInfo.get(), TariffsForLocationDto.class);
        } else {
            throw new EntityNotFoundException(TARIFF_FOR_ORDER_NOT_EXIST + id);
        }
    }

    @Override
    @Cacheable(value = "positionsAndAuthorities", key = "#email")
    public PositionAuthoritiesDto getPositionsAndRelatedAuthorities(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_DOESNT_EXIST + email));
        return userRemoteClient.getPositionsAndRelatedAuthorities(employee.getEmail());
    }

    @Override
    public Set<String> getAllAuthorities(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_DOESNT_EXIST + email));
        return userRemoteClient.getAllAuthorities(employee.getEmail());
    }

    @Override
    public void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto) {
        userRemoteClient.updateEmployeesAuthorities(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AddressDto makeAddressActual(Long addressId, String uuid) {
        Address currentAddress = addressRepo.findById(addressId).orElseThrow(
            () -> new NotFoundException(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));

        if (!currentAddress.getUser().getUuid().equals(uuid)) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }

        if (currentAddress.getAddressStatus() == AddressStatus.DELETED) {
            throw new BadRequestException(CANNOT_MAKE_ACTUAL_DELETED_ADDRESS);
        }

        if (Boolean.FALSE.equals(currentAddress.getActual())) {
            Address address = addressRepo.findByUserIdAndActualTrue(currentAddress.getUser().getId()).orElseThrow(
                () -> new NotFoundException(ACTUAL_ADDRESS_NOT_FOUND));
            address.setActual(false);
            currentAddress.setActual(true);
        }

        return modelMapper.map(currentAddress, AddressDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DistrictDto> getAllDistricts(String region, String city) {
        List<LocationDto> locationDtos = locationApiService.getAllDistrictsInCityByNames(region, city);
        return locationDtos.stream().map(p -> modelMapper.map(p, DistrictDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WayForPayOrderResponse processOrder(String userUuid, OrderWayForPayClientDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + dto.getOrderId()));
        checkOrderIsPaid(order.getOrderPaymentStatus());
        User currentUser = userRepository.findUserByUuid(userUuid)
            .orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST + userUuid));
        checkForNullCounter(order);
        long sumToPayInCoins = calculateSumToPay(dto, order, currentUser);

        transferUserPointsToOrder(order, dto.getPointsToUse());
        paymentVerification(sumToPayInCoins, order);

        if (sumToPayInCoins <= 0) {
            return getPaymentRequestDto(order, null);
        } else {
            String link = formedLink(order, sumToPayInCoins);
            return getPaymentRequestDto(order, link);
        }
    }

    private String formedLink(Order order, long sumToPayInCoins) {
        Order increment = incrementCounter(order);
        PaymentRequestDto paymentRequestDto = formPaymentRequest(increment.getId(), sumToPayInCoins);
        paymentRequestDto.setOrderReference(OrderUtils.generateOrderIdForPayment(increment.getId(), order));
        return getLinkFromWayForPayCheckoutResponse(wayForPayClient.getCheckOutResponse(paymentRequestDto));
    }

    private Order incrementCounter(Order order) {
        order.setCounterOrderPaymentId(order.getCounterOrderPaymentId() + 1);
        orderRepository.save(order);
        return order;
    }

    private void paymentVerification(long sumToPayInCoins, Order order) {
        if (sumToPayInCoins <= 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_CONFIRMED, OrderHistory.SYSTEM, order);
        }
    }

    private void transferUserPointsToOrder(Order order, Integer pointsToUse) {
        if (pointsToUse <= 0) {
            return;
        }

        User user = order.getUser();
        checkIfUserHaveEnoughPoints(user.getCurrentPoints(), pointsToUse);

        int maxPointsToTransfer = countAmountToPayForOrder(order);
        if (pointsToUse > maxPointsToTransfer) {
            throw new BadRequestException(TOO_MUCH_POINTS_FOR_ORDER + maxPointsToTransfer);
        }

        order.setPointsToUse(order.getPointsToUse() + pointsToUse);
        user.setCurrentPoints(user.getCurrentPoints() - pointsToUse);
        user.getChangeOfPointsList()
            .add(ChangeOfPoints.builder()
                .user(user)
                .amount(-pointsToUse)
                .date(LocalDateTime.now())
                .order(order)
                .build());

        orderRepository.save(order);
    }

    private int countAmountToPayForOrder(Order order) {
        int certificatesAmount = nonNull(order.getCertificates())
            ? order.getCertificates().stream()
                .map(Certificate::getPoints)
                .reduce(0, Integer::sum)
            : 0;
        return -order.getPointsToUse() - certificatesAmount
            + BigDecimal.valueOf(order.getSumTotalAmountWithoutDiscounts())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(0, RoundingMode.UP).intValue();
    }

    private long calculateSumToPay(OrderWayForPayClientDto dto, Order order, User currentUser) {
        List<BagForUserDto> bagForUserDtos = bagForUserDtosBuilder(order);
        long sumToPayInCoins = bagForUserDtos.stream()
            .map(b -> convertBillsIntoCoins(b.getTotalPrice()))
            .reduce(0L, Long::sum);

        List<CertificateDto> certificateDtos = order.getCertificates().stream()
            .map(certificate -> modelMapper.map(certificate, CertificateDto.class))
            .collect(toList());

        sumToPayInCoins = sumToPayInCoins - 100L * (order.getPointsToUse() + countCertificatesBonuses(certificateDtos));

        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        sumToPayInCoins = reduceOrderSumDueToUsedPoints(sumToPayInCoins, dto.getPointsToUse());
        sumToPayInCoins = formCertificatesToBeSavedAndCalculateOrderSumClient(dto, order, sumToPayInCoins);

        return sumToPayInCoins - countPaidAmount(order.getPayment());
    }

    private long formCertificatesToBeSavedAndCalculateOrderSumClient(OrderWayForPayClientDto dto, Order order,
        long sumToPayInCoins) {
        if (sumToPayInCoins != 0 && dto.getCertificates() != null) {
            Set<Certificate> certificates =
                certificateRepository.findByCodeInAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
                    CertificateStatus.ACTIVE);
            if (certificates.isEmpty()) {
                throw new NotFoundException(CERTIFICATE_NOT_FOUND);
            }
            checkValidationCertificates(certificates, dto);
            for (Certificate temp : certificates) {
                Certificate certificate = getCertificateForClient(temp, order);
                sumToPayInCoins -= certificate.getPoints() * 100L;

                if (dontSendLinkToWFPIfClient(sumToPayInCoins)) {
                    certificate.setCertificateStatus(CertificateStatus.USED);
                    certificate.setPoints(certificate.getPoints()
                        + BigDecimal.valueOf(sumToPayInCoins)
                            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                            .setScale(0, RoundingMode.UP).intValue());
                    sumToPayInCoins = 0L;
                }
            }
        }
        return sumToPayInCoins;
    }

    private boolean dontSendLinkToWFPIfClient(long sumToPayInCoins) {
        return sumToPayInCoins <= 0;
    }

    private Certificate getCertificateForClient(Certificate certificate, Order order) {
        certificate.setOrder(order);
        certificate.setCertificateStatus(CertificateStatus.USED);
        certificate.setDateOfUse(LocalDate.now());
        return certificate;
    }

    private void checkValidationCertificates(Set<Certificate> certificates, OrderWayForPayClientDto dto) {
        if (certificates.size() != dto.getCertificates().size()) {
            String validCertification = certificates.stream().map(Certificate::getCode).collect(joining(", "));
            throw new NotFoundException(SOME_CERTIFICATES_ARE_INVALID + validCertification);
        }
    }

    private void checkOrderIsPaid(OrderPaymentStatus orderPaymentStatus) {
        if (OrderPaymentStatus.PAID.equals(orderPaymentStatus)) {
            throw new BadRequestException(ORDER_ALREADY_PAID);
        }
    }

    private void checkForNullCounter(Order order) {
        if (order.getCounterOrderPaymentId() == null) {
            order.setCounterOrderPaymentId(0L);
        }
    }

    /**
     * Checks if a tariff exists by its ID.
     *
     * @param tariffInfoId The ID of the tariff to check.
     * @return {@code true} if the tariff exists, {@code false} otherwise.
     */
    @Override
    public boolean checkIfTariffExistsById(Long tariffInfoId) {
        return tariffsInfoRepository.existsById(tariffInfoId);
    }

    /**
     * Retrieves all active locations and converts them to DTOs.
     *
     * @return List of DTOs representing all active locations.
     */
    @Override
    public List<LocationsDto> getAllLocations() {
        List<Location> allActiveLocations = locationRepository.findAllActiveLocations();
        return allActiveLocations.stream().map(locationToLocationsDtoMapper::convert).collect(toList());
    }

    /**
     * Retrieves the tariff ID associated with the specified location ID.
     *
     * @param locationId The ID of the location to retrieve the tariff ID for.
     * @return The tariff ID if found.
     * @throws NotFoundException if the tariff ID is not found for the given
     *                           location ID.
     */
    @Override
    public List<Long> getTariffIdByLocationId(Long locationId) {
        return tariffsInfoRepository.findTariffIdByLocationId(locationId)
            .orElseThrow(() -> new NotFoundException(String.format(TARIFF_NOT_FOUND_BY_LOCATION_ID, locationId)));
    }

    @Override
    public List<LocationsDto> getAllLocationsByCourierId(Long courierId) {
        List<Location> locations = locationRepository.findAllActiveLocationsByCourierId(courierId);
        return locations.stream()
            .map(locationToLocationsDtoMapper::convert)
            .map(locationsDto -> locationsDto.setTariffsId(
                tariffsInfoRepository.findTariffIdByLocationIdAndCourierId(locationsDto.getId(), courierId)
                    .orElseThrow(() -> new NotFoundException(
                        String.format(TARIFF_NOT_FOUND_BY_LOCATION_ID, locationsDto.getId())))))
            .collect(toList());
    }
}
