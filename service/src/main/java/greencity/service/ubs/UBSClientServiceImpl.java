package greencity.service.ubs;

import static greencity.constant.AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT_EN;
import static greencity.constant.AppConstant.UBS_EMPLOYEE_WITH_PREFIX;
import static greencity.constant.AppConstant.USER_WITH_PREFIX;
import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_ORDER_CANCELLATION_REASON;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_PERSONAL_INFO;
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
import static greencity.constant.ErrorMessage.ORDER_ALREADY_PAID;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.PAYMENT_VALIDATION_ERROR;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_GREATER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.PRICE_OF_ORDER_LOWER_THAN_LIMIT;
import static greencity.constant.ErrorMessage.RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.SOME_CERTIFICATES_ARE_INVALID;
import static greencity.constant.ErrorMessage.TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_ORDER_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND_BY_LOCATION_ID;
import static greencity.constant.ErrorMessage.TARIFF_OR_LOCATION_IS_DEACTIVATED;
import static greencity.constant.ErrorMessage.TOO_MANY_BAGS_EXCEPTION;
import static greencity.constant.ErrorMessage.TOO_MANY_CERTIFICATES;
import static greencity.constant.ErrorMessage.TOO_MUCH_POINTS_FOR_ORDER;
import static greencity.constant.ErrorMessage.USER_DONT_HAVE_ENOUGH_POINTS;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static greencity.util.OrderUtils.getLastPayment;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import greencity.client.MonoBankClient;
import greencity.client.UserRemoteClient;
import greencity.client.WayForPayClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.KyivTariffLocation;
import greencity.constant.OrderHistory;
import greencity.constant.TariffLocation;
import greencity.dto.AllActiveLocationsDto;
import greencity.dto.LocationWithTariffInfoDto;
import greencity.dto.LocationsDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.RegionDto;
import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffInfoDto;
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
import greencity.dto.notification.SenderInfoDto;
import greencity.dto.order.EventDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.dto.payment.monobank.BasketOrder;
import greencity.dto.payment.monobank.CheckoutResponseFromMonoBank;
import greencity.dto.payment.monobank.MerchantPaymentInfo;
import greencity.dto.payment.monobank.MonoBankPaymentRequestDto;
import greencity.dto.payment.monobank.MonoBankPaymentResponseDto;
import greencity.dto.payment.monobank.PaymentInfo;
import greencity.dto.position.PositionAuthoritiesDto;
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
import greencity.entity.notifications.UserNotification;
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
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.AddressStatus;
import greencity.enums.BagStatus;
import greencity.enums.BonusReason;
import greencity.enums.BotType;
import greencity.enums.CertificateStatus;
import greencity.enums.CourierLimit;
import greencity.enums.LocationStatus;
import greencity.enums.MonoBankStatuses;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.PaymentType;
import greencity.enums.TariffStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.exceptions.certificate.CertificateIsNotActivated;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.mapping.location.LocationToLocationsDtoMapper;
import greencity.repository.AddressRepository;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.CourierRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import greencity.repository.LocationRepository;
import greencity.repository.NotificationParameterRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserNotificationRepository;
import greencity.repository.UserRepository;
import greencity.service.DistanceCalculationUtils;
import greencity.service.google.GoogleApiService;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;
import greencity.util.OrderUtils;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UBSClientService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UBSClientServiceImpl implements UBSClientService {
    private static final Integer VALIDITY_DURATION_TEN_DAYS = 864000;
    private static final String PAY_BUTTON = "payButton";
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSUserRepository ubsUserRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
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
    private final TelegramChatRepository telegramBotRepository;
    private final OrderBagRepository orderBagRepository;
    private final OrderBagService orderBagService;
    private final NotificationService notificationService;
    private final WayForPayClient wayForPayClient;
    private final LocationToLocationsDtoMapper locationToLocationsDtoMapper;
    private final MonoBankClient monoBankClient;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationParameterRepository notificationParameterRepository;
    private final AddressService addressService;

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
    @Value("${greencity.monobank.token}")
    private String token;
    @Value("${greencity.monobank.redirect}")
    private String monoBankRedirectionUrl;
    @Value("${greencity.monobank.redirect.result-monobank-url}")
    private String monoBankPaymentRedirectUrl;
    @Value("${greencity.redirect.green-city-client}")
    private String greenCityClientUrl;
    private static final String FAILED_STATUS = "failure";
    private static final String APPROVED_STATUS = "Approved";
    private static final String TELEGRAM_PART_1_OF_LINK = "https://telegram.me/";
    private static final String TELEGRAM_PART_3_OF_LINK = "?start=";
    private static final String LANGUAGE_EN = "en";
    private static final String LANGUAGE_UK = "uk";
    private static final Double KYIV_LATITUDE = 50.4546600;
    private static final Double KYIV_LONGITUDE = 30.5238000;
    private static final Double LOCATION_40_KM_ZONE_VALUE = 40.00;
    private static final String UKRAINE_EN = "Ukraine";
    private static final String LANG_EN = "en";
    private static final String ADDRESS_NOT_FOUND_BY_ID_MESSAGE = "Address not found with id: ";
    private static final String ADDRESS_NOT_WITHIN_LOCATION_AREA_MESSAGE = "Location and Address selected "
        + "does not match, reselect correct data.";
    private static final byte CURRENCY_CONVERSION_RATE = 100;
    private static final String DEFAULT_VALUE_FOR_PAYMENT = "Unknown";

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PaymentResponseWayForPay validatePayment(PaymentResponseDto response) {
        String decodedOrderReference = OrderUtils.decodeOrderReference(response.getOrderReference());
        Payment orderPayment = mapPayment(response, decodedOrderReference);
        String[] ids = decodedOrderReference.split("_");
        Order order = orderRepository.findById(Long.valueOf(ids[0]))
            .orElseThrow(() -> new BadRequestException(PAYMENT_VALIDATION_ERROR));
        checkResponseStatusFailure(response, orderPayment, order);
        checkOrderStatusApproved(response, orderPayment, order, decodedOrderReference);
        PaymentResponseWayForPay accept = PaymentResponseWayForPay.builder()
            .orderReference(response.getOrderReference())
            .status("accept")
            .time(response.getCreatedDate()).build();
        accept.setSignature(encryptionUtil.formResponseSignature(accept, wayForPaySecret));
        return accept;
    }

    @Override
    @Transactional
    public PaymentResponseWayForPay convertMapIntoPaymentResponseDto(Map<String, String> formParams) {
        if (formParams == null || formParams.isEmpty()) {
            return buildErrorResponse("No form params received");
        }
        log.debug("Received {} form param(s) from WayForPay", formParams.size());

        String jsonKey = formParams.keySet().iterator().next();
        log.debug("Extracted JSON from param key: {}", jsonKey);

        PaymentResponseDto dto;
        try {
            dto = getPaymentResponseDto(jsonKey);
        } catch (JsonProcessingException e) {
            return buildErrorResponse("Invalid JSON format");
        }

        if (isInvalidSignature(dto)) {
            return buildErrorResponse("Invalid signature");
        }

        log.info("Valid signature for orderReference={}", dto.getOrderReference());
        return validatePayment(dto);
    }

    private PaymentResponseDto getPaymentResponseDto(String jsonKey) throws JsonProcessingException {
        PaymentResponseDto dto;
        dto = objectMapper.readValue(jsonKey, PaymentResponseDto.class);
        log.info("Processing payment: orderReference={}, status={}",
            dto.getOrderReference(), dto.getTransactionStatus());
        return dto;
    }

    private boolean isInvalidSignature(PaymentResponseDto dto) {
        String calculatedSignature = encryptionUtil.generateResponseSignature(dto, wayForPaySecret);
        if (!calculatedSignature.equals(dto.getMerchantSignature())) {
            log.error("Invalid signature for orderReference={}", dto.getOrderReference());
            return true;
        }
        return false;
    }

    private PaymentResponseWayForPay buildErrorResponse(String message) {
        log.error("Payment processing error: {}", message);
        return PaymentResponseWayForPay.builder()
            .status("ERROR")
            .orderReference(null)
            .time(LocalDateTime.now().toString())
            .signature(null)
            .build();
    }

    private Payment mapPayment(PaymentResponseDto response, String decodedOrderReference) {
        if (response.getFee() == null) {
            response.setFee("0");
        }
        return Payment.builder()
            .id(Long.valueOf(decodedOrderReference
                .substring(decodedOrderReference.lastIndexOf("_") + 1)))
            .currency(response.getCurrency())
            .amount(Long.parseLong(response.getAmount()) * 100)
            .orderStatus(OrderStatus.FORMED)
            .senderCellPhone(response.getPhone())
            .maskedCard(response.getCardPan())
            .cardType(response.getCardType())
            .orderTime(response.getCreatedDate())
            .settlementDate(parseSettlementDate(""))
            .fee(0L)
            .paymentSystem(response.getPaymentSystem())
            .senderEmail(response.getEmail())
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }

    private String parseSettlementDate(String settlementDate) {
        return settlementDate.isEmpty()
            ? LocalDate.now().toString()
            : LocalDate.parse(settlementDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString();
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
        TariffsInfo tariffsInfo = tariffsInfoRepository.findById(tariffId)
            .orElseThrow(() -> new NotFoundException(TARIFF_NOT_FOUND + tariffId));

        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException(LOCATION_DOESNT_FOUND_BY_ID + locationId));

        checkIfTariffIsAvailableForCurrentLocation(tariffsInfo, location);

        return getUserPointsAndAllBagsDtoByTariffIdAndUserPoints(tariffsInfo.getId(), 0);
    }

    @Override
    public UserPointsAndAllBagsDto getFirstPageDataByOrderId(String uuid, Long orderId) {
        User user = userRepository.findUserByUuid(uuid).orElseThrow(
            () -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));

        checkIsOrderOfCurrentUser(user, order);

        TariffsInfo tariffsInfo = order.getTariffsInfo();

        Location location = getLocationByOrderIdThroughLazyInitialization(order);

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
            boolean isAvailable = isTariffAvailableForCurrentLocation(tariffsInfo, location);
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
        List<BagTranslationDto> bagTranslationDtoList =
            bagRepository.findAllActiveBagsByTariffsInfoId(tariffId).stream()
                .map(bag -> buildBagTranslationDto(orderId, bag))
                .toList();
        return new UserPointsAndAllBagsDto(bagTranslationDtoList, userPoints);
    }

    private UserPointsAndAllBagsDto getUserPointsAndAllBagsDtoByTariffIdAndUserPoints(Long tariffId,
        Integer userPoints) {
        List<BagTranslationDto> bagTranslationDtoList =
            bagRepository.findAllActiveBagsByTariffsInfoId(tariffId).stream()
                .map(bag -> modelMapper.map(bag, BagTranslationDto.class))
                .sorted(Comparator.comparing(BagTranslationDto::getCapacity).reversed())
                .toList();
        return new UserPointsAndAllBagsDto(bagTranslationDtoList, userPoints);
    }

    private BagTranslationDto buildBagTranslationDto(Long orderId, Bag source) {
        return BagTranslationDto.builder()
            .id(source.getId())
            .capacity(source.getCapacity())
            .price(BigDecimal.valueOf(source.getFullPrice())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .nameUk(source.getNameUk())
            .nameEn(source.getNameEn())
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
    public PaymentSystemResponse processNewOrder(OrderResponseDto dto, String uuid) {
        validateOrderRequestAddress(dto);

        adjustPaymentDetails(dto);

        Order order = modelMapper.map(dto, Order.class);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.FORMED);
        order.setCounterOrderPaymentId(0L);

        User currentUser = userRepository.findByUuid(uuid);

        OrderAddress orderAddress = formAndSaveOrderAddress(
            dto.getAddressId(), dto.getLocationId(), currentUser);

        UBSuser userData = formAndSaveUbsUser(
            dto.getPersonalData(), null, orderAddress, currentUser);

        order = formAndSaveOrderRequest(dto, order, currentUser, userData);
        long sumToPayInCoins = getLastPayment(order).getAmount();

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        saveOrderEvent(OrderHistory.ORDER_FORMED_UK, OrderHistory.CLIENT_UK, order);

        PaymentSystemResponse paymentSystemResponse =
            processPaymentResponse(dto, order, sumToPayInCoins, currentUser);

        notificationService.notifyCreatedOrder(order);

        return paymentSystemResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PaymentSystemResponse processExistingOrder(OrderResponseDto dto, String uuid, Long orderId) {
        validateOrderRequestAddress(dto);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));

        User currentUser = userRepository.findByUuid(uuid);
        checkIsOrderOfCurrentUser(currentUser, order);

        if (order.getOrderStatus() != OrderStatus.FORMED
            || order.getOrderPaymentStatus() != OrderPaymentStatus.UNPAID) {
            throw new BadRequestException(ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED);
        }

        adjustPaymentDetails(dto);

        order.setPointsToUse(dto.getPointsToUse());
        order.setAdditionalOrders(dto.getAdditionalOrders());
        order.setComment(dto.getOrderComment());

        OrderAddress orderAddress = getOrUpdateOrderAddress(
            order.getUbsUser().getOrderAddress(), dto.getAddressId(), dto.getLocationId(), currentUser);

        UBSuser userData = formAndSaveUbsUser(
            dto.getPersonalData(), order.getUbsUser().getId(), orderAddress, currentUser);

        order = formAndSaveOrderRequest(dto, order, currentUser, userData);
        long sumToPayInCoins = getLastPayment(order).getAmount();

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        saveOrderEvent(OrderHistory.ORDER_STATUS_UPDATED_UK, OrderHistory.CLIENT_UK, order);

        PaymentSystemResponse paymentSystemResponse =
            processPaymentResponse(dto, order, sumToPayInCoins, currentUser);

        if (order.getOrderPaymentStatus() == OrderPaymentStatus.UNPAID) {
            notificationService.notifyUnpaidOrderPermanently(order, sumToPayInCoins, paymentSystemResponse);
        }

        return paymentSystemResponse;
    }

    private void validateOrderRequestAddress(OrderResponseDto dto) {
        if (!checkIfAddressMatchLocationArea(dto.getLocationId(), dto.getAddressId())) {
            throw new AddressNotWithinLocationAreaException(ADDRESS_NOT_WITHIN_LOCATION_AREA_MESSAGE);
        }
    }

    private void adjustPaymentDetails(OrderResponseDto dto) {
        if (!dto.isShouldBePaid()) {
            dto.setCertificates(Collections.emptySet());
            dto.setPointsToUse(0);
        }
    }

    private Order formAndSaveOrderRequest(OrderResponseDto dto, Order order, User currentUser, UBSuser userData) {
        TariffsInfo tariffsInfo = findTariffsInfoByBagIdsWithinLocation(getBagIds(dto.getBags()), dto.getLocationId());
        List<OrderBag> bagsOrdered = new ArrayList<>();
        long sumToPayInCoinsWithoutDiscount =
            formBagsToBeSavedAndCalculateOrderSum(bagsOrdered, dto.getBags(), tariffsInfo);

        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        long sumToPayInCoins = reduceOrderSumDueToUsedPoints(sumToPayInCoinsWithoutDiscount, dto.getPointsToUse());
        if (sumToPayInCoinsWithoutDiscount == sumToPayInCoins) {
            order.setPointsToUse(0);
            dto.setPointsToUse(0);
        }

        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPayInCoins =
            formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPayInCoins);
        if (sumToPayInCoins <= 0) {
            dto.setShouldBePaid(false);
        }

        return formAndSaveOrder(order, orderCertificates, bagsOrdered, userData, currentUser, sumToPayInCoins,
            tariffsInfo);
    }

    private void saveOrderEvent(String eventName, String author, Order order) {
        eventService.save(eventName, author, order);
        log.info("Saved event: eventName={}, author={}, orderId={}", eventName, author, order.getId());
    }

    private PaymentSystemResponse processPaymentResponse(OrderResponseDto dto, Order order, long sumToPayInCoins,
        User currentUser) {
        if (dto.isShouldBePaid()) {
            return processPayment(dto, order, sumToPayInCoins, currentUser);
        } else {
            return getPaymentRequestDto(order, "");
        }
    }

    private PaymentSystemResponse processPayment(OrderResponseDto dto, Order order, long sumToPayInCoins,
        User currentUser) {
        return switch (dto.getPaymentSystem()) {
            case WAY_FOR_PAY -> processWayForPay(order, sumToPayInCoins);
            case MONOBANK -> processMonoBank(order, sumToPayInCoins, currentUser);
        };
    }

    private PaymentSystemResponse processWayForPay(Order order, long sumToPayInCoins) {
        PaymentWayForPayRequestDto requestDto = formPaymentRequestForWayForPay(order.getId(), sumToPayInCoins);
        String link = getLinkFromWayForPayCheckoutResponse(wayForPayClient.getCheckOutResponse(requestDto));
        return getPaymentRequestDto(order, link);
    }

    private PaymentSystemResponse processMonoBank(Order order, long sumToPayInCoins, User currentUser) {
        MonoBankPaymentRequestDto requestDto =
            formPaymentRequestForMonoBank(order.getId(), sumToPayInCoins, currentUser);
        CheckoutResponseFromMonoBank checkoutResponse = monoBankClient.getCheckoutResponse(requestDto, token);
        return getPaymentRequestDto(order, checkoutResponse.pageUrl());
    }

    private MonoBankPaymentRequestDto formPaymentRequestForMonoBank(Long orderId, long sumToPayInCoins, User user) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return MonoBankPaymentRequestDto.builder()
            .amount((int) sumToPayInCoins)
            .merchantPaymentInfo(MerchantPaymentInfo.builder()
                .orderReference(OrderUtils.generateEncodedOrderReference(orderId, order))
                .emails(Set.of(user.getRecipientEmail()))
                .orderList(getBasketOrders(order))
                .build())
            .redirectUrl(monoBankRedirectionUrl)
            .webHookUrl(monoBankPaymentRedirectUrl)
            .validity(VALIDITY_DURATION_TEN_DAYS)
            .build();
    }

    private List<BasketOrder> getBasketOrders(Order order) {
        return order.getOrderBags().stream()
            .filter(bag -> bag.getAmount() != 0)
            .map(this::convert)
            .toList();
    }

    private BasketOrder convert(OrderBag bag) {
        return BasketOrder.builder()
            .name(bag.getNameUk())
            .quantity(bag.getAmount().floatValue())
            .sum((int) (bag.getPrice() * bag.getAmount()))
            .code(bag.getNameUk())
            .build();
    }

    private boolean checkIfAddressMatchLocationArea(long locationId, long addressId) {
        Address address = addressRepo.findById(addressId)
            .orElseThrow(() -> new NotFoundException(ADDRESS_NOT_FOUND_BY_ID_MESSAGE + addressId));

        boolean isKyivTariff = checkIfCityBelongsToKyivTariff(address.getBaseAddress().getCityEn());

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
                .getGeocodingResultByCityAndCountryAndLocale(UKRAINE_EN, address.getBaseAddress().getCityEn(),
                    LANG_EN).geometry.location;
            Coordinates addressCoordinates = Coordinates.builder().latitude(latLng.lat).longitude(latLng.lng).build();
            address.setCoordinates(addressCoordinates);
            addressRepo.save(address);
        }
    }

    private List<Integer> getBagIds(List<BagDto> dto) {
        return dto.stream()
            .map(BagDto::getId)
            .toList();
    }

    private Bag findActiveBagById(Integer id) {
        return bagRepository.findActiveBagById(id)
            .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + id));
    }

    private TariffsInfo findTariffsInfoByBagIdsWithinLocation(List<Integer> bagIds, Long locationId) {
        return tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(bagIds, locationId)
            .orElseThrow(
                () -> new NotFoundException(String.format(TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST, bagIds, locationId)));
    }

    private PaymentSystemResponse getPaymentRequestDto(Order order, String link) {
        return PaymentSystemResponse.builder()
            .orderId(order.getId())
            .link(link)
            .build();
    }

    private void checkIfAddressHasBeenDeleted(Address address) {
        if (address.getBaseAddress().getAddressStatus().equals(AddressStatus.DELETED)) {
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

    private void formAndSaveUser(User currentUser, int pointsToUse, Order order) {
        currentUser.getOrders().add(order);
        if (pointsToUse != 0) {
            currentUser.setCurrentPoints(currentUser.getCurrentPoints() - pointsToUse);
            currentUser.getChangeOfPointsList().add(ChangeOfPoints.builder()
                .amount(-pointsToUse)
                .date(order.getOrderDate())
                .user(currentUser)
                .order(order)
                .reason(BonusReason.DEBIT_PAYMENT)
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

    public OrdersDataForUserDto getOrdersData(Order order) {
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
            .toList();

        Long amountWithDiscountInCoins = fullPriceInCoins
            - 100L * (order.getPointsToUse() + countCertificatesBonuses(certificateDtos));

        Long paidAmountInCoins = countPaidAmount(payments);

        Double amountBeforePayment = convertCoinsIntoBills(amountWithDiscountInCoins - paidAmountInCoins);

        double refundedBonuses = order.getPayment().stream()
            .filter(payment -> ENROLLMENT_TO_THE_BONUS_ACCOUNT_EN.equals(payment.getReceiptLink()))
            .map(payment -> payment.getAmount().doubleValue())
            .reduce(0.0, Double::sum);

        refundedBonuses /= -CURRENCY_CONVERSION_RATE;

        Double refundedMoney =
            order.getRefund() == null ? 0.0 : order.getRefund().getAmount().doubleValue() / CURRENCY_CONVERSION_RATE;

        return OrdersDataForUserDto.builder()
            .id(order.getId())
            .dateForm(order.getOrderDate())
            .datePaid(order.getOrderDate())
            .orderStatusUk(orderStatusTranslation.getNameUk())
            .orderStatusEn(orderStatusTranslation.getNameEn())
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
            .paymentStatusUk(paymentStatusTranslation.getTranslationValueUk())
            .paymentStatusEn(paymentStatusTranslation.getTranslationsValueEn())
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
        OrderAddress address = order.getUbsUser().getOrderAddress();
        return AddressInfoDto.builder()
            .addressCityUk(address.getBaseAddress().getCityUk())
            .addressCityEn(address.getBaseAddress().getCityEn())
            .addressComment(address.getBaseAddress().getAddressComment())
            .addressDistinctUk(address.getBaseAddress().getDistrictUk())
            .addressDistinctEn(address.getBaseAddress().getDistrictEn())
            .addressRegionUk(address.getBaseAddress().getRegionUk())
            .addressRegionEn(address.getBaseAddress().getRegionEn())
            .addressStreetUk(address.getBaseAddress().getStreetUk())
            .addressStreetEn(address.getBaseAddress().getStreetEn())
            .houseCorpus(address.getBaseAddress().getHouseCorpus())
            .houseNumber(address.getBaseAddress().getHouseNumber())
            .entranceNumber(address.getBaseAddress().getEntranceNumber())
            .build();
    }

    private List<BagForUserDto> bagForUserDtosBuilder(Order order) {
        List<OrderBag> bagsAmountInOrder = order.getOrderBags();
        Map<Integer, Integer> actualBagsAmount = orderBagService.getActualBagsAmountForOrder(bagsAmountInOrder);
        return bagsAmountInOrder.stream()
            .map(orderBag -> buildBagForUserDto(orderBag, actualBagsAmount.get(orderBag.getBag().getId())))
            .toList();
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
        UBSuser ubsUser = ubsUserRepository.findUbsUserByOrderId(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        User user = ubsUser.getUser();
        if (!Objects.equals(user.getUuid(), uuid)) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }
        return UserInfoDto.builder()
            .customerName(ubsUser.getFirstName())
            .customerSurname(ubsUser.getLastName())
            .customerPhoneNumber(ubsUser.getPhoneNumber())
            .customerEmail(ubsUser.getEmail())
            .totalUserViolations(user.getViolations())
            .customerId(user.getId())
            .senderName(ubsUser.getSenderFirstName() == null ? ubsUser.getFirstName() : ubsUser.getSenderFirstName())
            .senderSurname(ubsUser.getSenderLastName() == null ? ubsUser.getLastName() : ubsUser.getSenderLastName())
            .senderEmail(ubsUser.getSenderEmail() == null ? ubsUser.getEmail() : ubsUser.getSenderEmail())
            .senderPhoneNumber(
                ubsUser.getSenderPhoneNumber() == null ? ubsUser.getPhoneNumber() : ubsUser.getSenderPhoneNumber())
            .userViolationForCurrentOrder(userRepository.checkIfUserHasViolationForCurrentOrder(user.getId(), orderId))
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
    public UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String userUuid) {
        UBSuser ubsUser = getUbsUserById(dtoUpdate.getCustomerId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        checkUserHasAccessToUpdateData(ubsUser, userUuid, authentication);

        ubsUserRepository.save(updateRecipientDataInOrder(ubsUser, dtoUpdate));
        if (!isAdmin(authentication)) {
            eventService.save(OrderHistory.CHANGED_SENDER_UK, OrderHistory.CLIENT_UK,
                ubsUser.getOrders().getFirst());
        } else {
            eventService.save(OrderHistory.CHANGED_SENDER_UK, OrderHistory.UBS_ADMIN,
                ubsUser.getOrders().getFirst());
        }

        return UbsCustomersDto.builder()
            .name(ubsUser.getSenderFirstName() + " " + ubsUser.getSenderLastName())
            .email(ubsUser.getSenderEmail())
            .phoneNumber(ubsUser.getSenderPhoneNumber())
            .build();
    }

    /**
     * Method checks if current user is admin.
     *
     * @return {@link Boolean} true if user is admin, false otherwise;
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(UBS_EMPLOYEE_WITH_PREFIX));
    }

    private UBSuser getUbsUserById(Long recipientId) {
        return ubsUserRepository.findById(recipientId)
            .orElseThrow(() -> new UBSuserNotFoundException(RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST + recipientId));
    }

    private void checkUserHasAccessToUpdateData(UBSuser ubsUser, String userUuid, Authentication authentication) {
        String uuid = ubsUser.getUser().getUuid();
        if (checkUserRoleIsUser(authentication) && !(uuid.equals(userUuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }
    }

    private boolean checkUserRoleIsUser(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(USER_WITH_PREFIX));
    }

    @Override
    public Long createUserProfile(UserProfileCreateDto userProfileCreateDto) {
        if (!userRemoteClient.checkIfUserExistsByUuid(userProfileCreateDto.getUuid())) {
            throw new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST);
        }
        User user = userRepository.findByUuid(userProfileCreateDto.getUuid());
        if (user != null) {
            throw new BadRequestException(USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS);
        }
        user = userRepository.save(User.builder()
            .uuid(userProfileCreateDto.getUuid())
            .recipientEmail(userProfileCreateDto.getEmail())
            .recipientName(userProfileCreateDto.getName())
            .currentPoints(0)
            .violations(0)
            .dateOfRegistration(LocalDate.now()).build());
        return user.getId();
    }

    private UBSuser updateRecipientDataInOrder(UBSuser ubsUser, UbsCustomersDtoUpdate dto) {
        if (nonNull(dto.getCustomerEmail())) {
            ubsUser.setSenderEmail(dto.getCustomerEmail());
        }
        if (nonNull(dto.getCustomerName())) {
            ubsUser.setSenderFirstName(dto.getCustomerName());
        }
        if (nonNull(dto.getCustomerSurname())) {
            ubsUser.setSenderLastName(dto.getCustomerSurname());
        }
        if (nonNull(dto.getCustomerPhoneNumber())) {
            ubsUser.setSenderPhoneNumber(dto.getCustomerPhoneNumber());
        }

        return ubsUser;
    }

    private Order formAndSaveOrder(
        Order order, Set<Certificate> orderCertificates, List<OrderBag> bagsOrdered,
        UBSuser userData, User currentUser, long sumToPayInCoins, TariffsInfo tariffsInfo) {
        order.setTariffsInfo(tariffsInfo);
        order.setCertificates(orderCertificates);
        order.setOrderBags(bagsOrdered);
        order.setUbsUser(userData);
        order.setUser(currentUser);
        order.setSumTotalAmountWithoutDiscounts(calculateOrderSumWithoutDiscounts(bagsOrdered));
        order.setCounterOrderPaymentId(order.getCounterOrderPaymentId() + 1);
        setOrderPaymentStatus(order, sumToPayInCoins);

        Payment payment = Payment.builder()
            .amount(sumToPayInCoins)
            .orderStatus(OrderStatus.FORMED)
            .currency("UAH")
            .paymentStatus(PaymentStatus.UNPAID)
            .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .order(order).build();

        if (order.getPayment() == null) {
            order.setPayment(new ArrayList<>());
        }
        order.getPayment().add(payment);
        return orderRepository.save(order);
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

    private PaymentWayForPayRequestDto formPaymentRequestForWayForPay(Long orderId, long sumToPayInCoins) {
        Instant instant = Instant.now();
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        PaymentWayForPayRequestDto paymentWayForPayRequestDto = PaymentWayForPayRequestDto.builder()
            .transactionType("CREATE_INVOICE")
            .merchantAccount(merchantAccount)
            .merchantDomainName(merchantDomainName)
            .apiVersion(1)
            .serviceUrl(resultWayForPayUrl)
            .orderReference(OrderUtils.generateEncodedOrderReference(orderId, order))
            .orderDate(instant.getEpochSecond())
            .amount(convertCoinsIntoBills(sumToPayInCoins).intValue())
            .currency("UAH")
            .orderTimeout(VALIDITY_DURATION_TEN_DAYS)
            .productName(order.getOrderBags().stream()
                .filter(bag -> bag.getAmount() != 0)
                .map(orderBag -> orderBag.getNameUk().trim())
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
            .returnUrl(greenCityClientUrl)
            .build();

        paymentWayForPayRequestDto.setSignature(encryptionUtil
            .formRequestSignature(paymentWayForPayRequestDto, wayForPaySecret));

        return paymentWayForPayRequestDto;
    }

    private Double convertCoinsIntoBills(Long coins) {
        return BigDecimal.valueOf(coins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private UBSuser formAndSaveUbsUser(
        PersonalDataDto dto, Long id, OrderAddress orderAddress, User currentUser) {
        UBSuser userData = modelMapper.map(dto, UBSuser.class);
        userData.setId(id);
        userData.setUser(currentUser);
        userData.setPhoneNumber(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(userData.getPhoneNumber()));
        userData.setOrderAddress(orderAddress);
        userData = ubsUserRepository.save(userData);

        currentUser.getUbsUsers().add(userData);
        currentUser.setRecipientSurname(dto.getLastName());
        currentUser.setRecipientName(dto.getFirstName());
        currentUser.setRecipientPhone(dto.getPhoneNumber());
        userRepository.save(currentUser);

        return userData;
    }

    private OrderAddress getOrUpdateOrderAddress(
        OrderAddress currentOrderAddress, Long newAddressId, Long newLocationId, User currentUser) {
        OrderAddress newOrderAddress = formOrderAddress(newAddressId, newLocationId, currentUser);
        newOrderAddress.setId(currentOrderAddress.getId());
        if (currentOrderAddress.equals(newOrderAddress)) {
            return currentOrderAddress;
        }
        return orderAddressRepository.save(newOrderAddress);
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
                throw new BadRequestException(TOO_MANY_BAGS_EXCEPTION + courierLocation.getMax());
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
            .nameUk(bag.getNameUk())
            .nameEn(bag.getNameEn())
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
                .toList();
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
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId);
        }

        List<Event> orderEvents = eventRepository.findAllEventsByOrderId(orderId);
        if (orderEvents.isEmpty()) {
            throw new NotFoundException(EVENTS_NOT_FOUND_EXCEPTION + orderId);
        }

        localizeEventNames(orderEvents, language);
        return orderEvents.stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .sorted(Comparator.comparing(EventDto::getEventDate).reversed())
            .toList();
    }

    /**
     * Method that takes a list of events and a language and localizes the event
     * names and author names in the list of events.
     *
     * @param events   a list of events
     * @param language a language
     */
    private void localizeEventNames(List<Event> events, String language) {
        if (LANGUAGE_EN.equals(language)) {
            events.forEach(event -> {
                event.setEventNameUk(event.getEventNameEn());
                event.setAuthorNameUk(event.getAuthorNameEn());
            });
        } else if (!LANGUAGE_UK.equals(language)) {
            throw new BadRequestException("Unexpected value: " + language);
        }
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
        setTelegramBot(user, userProfileUpdateDto.getTelegramIsNotify());
        userProfileUpdateDto.getAddressDto().stream()
            .map(a -> modelMapper.map(a, OrderAddressDtoRequest.class))
            .forEach(addressRequestDto -> addressService.updateCurrentAddressForOrder(addressRequestDto, uuid));
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
                .toList();
        userProfileDto.setAddressDto(addressDto);
        userProfileDto.setBotList(botList);
        userProfileDto.setHasPassword(userRemoteClient.getPasswordStatus().isHasPassword());
        return userProfileDto;
    }

    private void setUserData(User user, UserProfileUpdateDto userProfileUpdateDto) {
        user.setRecipientName(userProfileUpdateDto.getRecipientName());
        user.setRecipientSurname(userProfileUpdateDto.getRecipientSurname());
        user.setAlternateEmail(userProfileUpdateDto.getAlternateEmail());
        String phone = userProfileUpdateDto.getRecipientPhone();
        user.setRecipientPhone(
            (phone == null || phone.trim().isEmpty()) ? null : UAPhoneNumberUtil.getE164PhoneNumberFormat(phone));
    }

    private void setTelegramBot(User user, Boolean telegramIsNotify) {
        TelegramChat telegramBot = telegramBotRepository.findByUser(user).orElse(null);
        if (telegramBot != null) {
            telegramBot.setIsNotify(telegramIsNotify);
            user.setTelegramBot(telegramBot);
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

    private OrderAddress formAndSaveOrderAddress(Long addressId, Long locationId, User currentUser) {
        return orderAddressRepository.save(formOrderAddress(addressId, locationId, currentUser));
    }

    private OrderAddress formOrderAddress(Long addressId, Long locationId, User currentUser) {
        Address address = addressRepo.findById(addressId)
            .orElseThrow(() -> new NotFoundException(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));
        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException(LOCATION_DOESNT_FOUND_BY_ID + locationId));

        checkIfAddressHasBeenDeleted(address);
        checkAddressUser(address, currentUser);

        OrderAddress orderAddress = modelMapper.map(address, OrderAddress.class);
        orderAddress.setLocation(location);

        return orderAddress;
    }

    @Override
    @Transactional
    public void deleteOrder(String uuid, Long id) {
        Order order = ordersForUserRepository.getAllByUserUuidAndId(uuid, id);
        if (order == null) {
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }
        order.getOrderBags().clear();
        orderRepository.saveAndFlush(order);
        orderRepository.delete(order);
    }

    private Long convertBillsIntoCoins(Double bills) {
        return BigDecimal.valueOf(bills)
            .movePointRight(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(AppConstant.NO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
            .longValue();
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
            log.info("Payment failed: orderId={}, transactionStatus={}, paymentStatus={}",
                order.getId(), dto.getTransactionStatus(), orderPayment.getPaymentStatus());
        }
    }

    protected void checkOrderStatusApproved(PaymentResponseDto dto,
        Payment orderPayment,
        Order order,
        String decodedOrderReference) {
        if (dto.getTransactionStatus().equals(APPROVED_STATUS)) {
            orderPayment.setPaymentId(decodedOrderReference.split("_")[1]);
            orderPayment.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            orderPayment.setOrder(order);
            removePaymentLinkForOrder(order);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_PAID_UK, OrderHistory.SYSTEM_UK, order);
            eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM_UK + orderPayment.getPaymentId(),
                OrderHistory.SYSTEM_UK, order);
            log.info("Payment approved: orderId={}, status={}",
                order.getId(), orderPayment.getPaymentStatus());
        } else {
            log.info("Payment not approved: orderId={}, transactionStatus={}",
                order.getId(), dto.getTransactionStatus());
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
            .toList();
    }

    private String createLink(BotType type, String uuid) {
        String linkTemplate = null;
        if ("TELEGRAM".equals(type.name())) {
            linkTemplate = String.format("%s%s%s%s",
                TELEGRAM_PART_1_OF_LINK, telegramBotName, TELEGRAM_PART_3_OF_LINK, uuid);
        }
        return linkTemplate;
    }

    private List<AllActiveLocationsDto> getAllActiveLocationsByCourierId(Long courierId) {
        List<Location> locations = locationRepository.findAllActiveLocationsByCourierId(courierId);
        return getAllActiveLocationsDtos(locations, courierId);
    }

    private List<AllActiveLocationsDto> getAllActiveLocationsDtos(List<Location> locations, Long courierId) {
        Map<RegionDto, List<LocationWithTariffInfoDto>> map = locations.stream()
            .collect(toMap(x -> modelMapper.map(x, RegionDto.class),
                x -> new ArrayList<>(List.of(LocationWithTariffInfoDto.builder()
                    .locationId(x.getId())
                    .nameUk(x.getNameUk())
                    .nameEn(x.getNameEn())
                    .tariffInfoDto(modelMapper.map(
                        tariffsInfoRepository
                            .findTariffInfoByLocationIdAndCourierId(x.getId(), courierId)
                            .orElse(null),
                        TariffInfoDto.class))
                    .build())),
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
            .toList();
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
        orderCourierPopUpDto.setOrderIsPresent(lastOrder.isPresent());
        orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocationsByCourierId(courierId));
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
    public TariffInfoByLocationDto getTariffInfoForLocation(Long courierId, Long locationId) {
        if (!courierRepository.existsCourierById(courierId)) {
            throw new NotFoundException(COURIER_IS_NOT_FOUND_BY_ID + courierId);
        }
        if (!locationRepository.existsById(locationId)) {
            throw new NotFoundException(LOCATION_DOESNT_FOUND_BY_ID + locationId);
        }
        return TariffInfoByLocationDto.builder()
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
            throw new NotFoundException(TARIFF_FOR_ORDER_NOT_EXIST + id);
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
    public PaymentSystemResponse processOrder(String userUuid, OrderWayForPayClientDto dto) {
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

    @Override
    public String formedLink(Order order, long sumToPayInCoins) {
        Order increment = incrementCounter(order);
        PaymentWayForPayRequestDto paymentWayForPayRequestDto =
            formPaymentRequestForWayForPay(increment.getId(), sumToPayInCoins);
        paymentWayForPayRequestDto
            .setOrderReference(OrderUtils.generateEncodedOrderReference(increment.getId(), order));
        return getLinkFromWayForPayCheckoutResponse(wayForPayClient.getCheckOutResponse(paymentWayForPayRequestDto));
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
            eventService.save(OrderHistory.ORDER_CONFIRMED_UK, OrderHistory.SYSTEM_UK, order);
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
                .reason(BonusReason.DEBIT_PAYMENT)
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
            .toList();

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
        return allActiveLocations.stream().map(locationToLocationsDtoMapper::convert).toList();
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
            .filter(list -> !list.isEmpty())
            .orElseThrow(() -> new NotFoundException(String.format(TARIFF_NOT_FOUND_BY_LOCATION_ID, locationId)));
    }

    @Override
    public List<LocationsDto> getAllLocationsByCourierId(Long courierId) {
        if (!courierRepository.existsCourierById(courierId)) {
            throw new NotFoundException(COURIER_IS_NOT_FOUND_BY_ID + courierId);
        }
        List<Location> locations = locationRepository.findAllActiveLocationsByCourierId(courierId);
        return locations.stream()
            .map(locationToLocationsDtoMapper::convert)
            .map(locationsDto -> locationsDto.setTariffsId(
                tariffsInfoRepository.findTariffIdByLocationIdAndCourierId(locationsDto.getId(), courierId)
                    .orElseThrow(() -> new NotFoundException(
                        String.format(TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST, locationsDto.getId(), courierId)))))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void validatePaymentFromMonoBank(MonoBankPaymentResponseDto response) {
        String decodedOrderReference = OrderUtils.decodeOrderReference(response.getOrderReference());
        String[] ids = decodedOrderReference.split("_");
        Order order = orderRepository.findById(Long.valueOf(ids[0]))
            .orElseThrow(() -> new BadRequestException(PAYMENT_VALIDATION_ERROR));
        Payment payment = createPayment(response, order, decodedOrderReference);
        checkPaymentResponseStatus(response, payment, order);
    }

    private Payment createPayment(MonoBankPaymentResponseDto response, Order order, String decodedOrderReference) {
        PaymentInfo paymentInfo = Optional.ofNullable(response.getPaymentInfo()).orElseGet(() -> PaymentInfo.builder()
            .cardNumber(DEFAULT_VALUE_FOR_PAYMENT)
            .paymentSystem(DEFAULT_VALUE_FOR_PAYMENT)
            .paymentMethod(DEFAULT_VALUE_FOR_PAYMENT)
            .fee(0)
            .terminal(DEFAULT_VALUE_FOR_PAYMENT)
            .build());
        return Payment.builder()
            .id(extractIdFromReference(decodedOrderReference))
            .currency("UAH")
            .amount((long) response.getAmount())
            .orderStatus(OrderStatus.FORMED)
            .responseStatus(response.getStatus())
            .senderCellPhone(order.getUser().getRecipientPhone())
            .maskedCard(paymentInfo.cardNumber())
            .cardType(paymentInfo.paymentSystem())
            .responseCode(parseErrorCode(response.getErrorCode()))
            .responseDescription(Optional.ofNullable(response.getFailureReason()).orElse("0"))
            .orderTime(formatDate(response.getCreatedDate()))
            .settlementDate(formatDate(response.getModifiedDate()))
            .fee(paymentInfo.fee().longValue())
            .paymentSystem(paymentInfo.paymentSystem())
            .senderEmail(order.getUser().getRecipientEmail())
            .paymentId(response.getInvoiceId())
            .order(order)
            .paymentType(PaymentType.AUTO)
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }

    private Long extractIdFromReference(String decodedOrderReference) {
        return Long.valueOf(decodedOrderReference.substring(decodedOrderReference.lastIndexOf("_") + 1));
    }

    private int parseErrorCode(String errorCode) {
        return (errorCode == null) ? 0 : Integer.parseInt(errorCode);
    }

    private String formatDate(String modifiedDate) {
        return OffsetDateTime.parse(modifiedDate).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void checkPaymentResponseStatus(MonoBankPaymentResponseDto response, Payment payment, Order order) {
        MonoBankStatuses status = MonoBankStatuses.valueOf(response.getStatus().toUpperCase());

        switch (status) {
            case SUCCESS -> {
                updatePaymentAndOrderStatus(payment, order, PaymentStatus.PAID, OrderPaymentStatus.PAID);
                logPaymentEvent(order, payment.getPaymentId());
                removePaymentLinkForOrder(order);
            }
            case REVERSED -> {
                updatePaymentAndOrderStatus(payment, order, PaymentStatus.UNPAID, OrderPaymentStatus.UNPAID);
                logOrderEvent(order, OrderHistory.PAYMENT_REVERSED_UK);
            }
            case PROCESSING -> {
                updatePaymentAndOrderStatus(payment, order, PaymentStatus.UNPAID, OrderPaymentStatus.UNPAID);
                logOrderEvent(order, OrderHistory.PAYMENT_PENDING_UK);
            }
            case FAILURE -> {
                updatePaymentAndOrderStatus(payment, order, PaymentStatus.UNPAID, OrderPaymentStatus.UNPAID);
                logOrderEvent(order, OrderHistory.PAYMENT_FAILURE_UK);
            }
            default -> updatePaymentAndOrderStatus(payment, order, PaymentStatus.UNPAID, OrderPaymentStatus.UNPAID);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    private void updatePaymentAndOrderStatus(Payment payment, Order order, PaymentStatus paymentStatus,
        OrderPaymentStatus orderStatus) {
        payment.setPaymentStatus(paymentStatus);
        order.setOrderPaymentStatus(orderStatus);
    }

    private void logOrderEvent(Order order, String event) {
        eventService.save(event, OrderHistory.SYSTEM_UK, order);
    }

    private void logPaymentEvent(Order order, String paymentId) {
        eventService.save(OrderHistory.ORDER_PAID_UK, OrderHistory.SYSTEM_UK, order);
        eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM_UK + paymentId, OrderHistory.SYSTEM_UK, order);
    }

    private void removePaymentLinkForOrder(Order order) {
        List<UserNotification> userNotification = userNotificationRepository
            .findAllUserNotificationByOrderAndNotificationType(order, NotificationType.UNPAID_ORDER);
        if (!userNotification.isEmpty()) {
            userNotification.stream()
                .map(notification -> notificationParameterRepository
                    .findNotificationParameterByUserNotificationAndKey(notification, PAY_BUTTON))
                .forEach(parameter -> parameter.ifPresent(notificationParameterRepository::delete));
        }
    }
}
