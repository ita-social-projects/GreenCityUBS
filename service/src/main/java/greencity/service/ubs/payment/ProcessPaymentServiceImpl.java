package greencity.service.ubs.payment;

import static greencity.constant.ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER;
import static greencity.constant.ErrorMessage.ORDER_ALREADY_PAID;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TOO_MUCH_POINTS_FOR_ORDER;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static greencity.util.OrderUtils.getLastPayment;
import static java.util.Objects.nonNull;
import com.google.maps.model.LatLng;
import greencity.client.WayForPayClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.KyivTariffLocation;
import greencity.constant.OrderHistory;
import greencity.constant.TariffLocation;
import greencity.dto.bag.BagDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.dto.user.PersonalDataDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.AddressStatus;
import greencity.enums.BonusReason;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.AddressRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.service.DistanceCalculationUtils;
import greencity.service.google.GoogleApiService;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.service.ubs.EventService;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.calculator.BagCalculatorService;
import greencity.service.ubs.calculator.CertificateCalculatorService;
import greencity.service.ubs.calculator.PaymentCalculatorService;
import greencity.service.ubs.calculator.PointCalculatorService;
import greencity.service.ubs.wayforpay.WayForPayService;
import greencity.util.OrderUtils;
import greencity.util.PointsUtils;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentServiceImpl implements ProcessPaymentService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepo;
    private final LocationRepository locationRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final UBSUserRepository ubsUserRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final GoogleApiService googleApiService;
    private final BagCalculatorService bagCalculatorService;
    private final PointCalculatorService pointCalculatorService;
    private final CertificateCalculatorService certificateCalculatorService;
    private final PaymentCalculatorService paymentCalculatorService;
    private final EventService eventService;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final WayForPayService wayForPayService;
    private final WayForPayClient wayForPayClient;
    private final ModelMapper modelMapper;
    private final PointsUtils pointsUtils;
    //TODO make class more readable,
    // extract some methods to the other services
    // clean up this service
    // remove duplicates

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
        //TODO another changes think about OrderService
        order = formAndSaveOrderRequest(dto, order, currentUser, userData);
        long sumToPayInCoins = getLastPayment(order).getAmount();

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        saveOrderEvent(OrderHistory.ORDER_FORMED_UK, OrderHistory.CLIENT_UK, order);

        PaymentSystemResponse paymentSystemResponse =
            processPaymentResponse(dto, order, sumToPayInCoins);

        notificationService.notifyCreatedOrder(order);

        return paymentSystemResponse;
    }

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
            processPaymentResponse(dto, order, sumToPayInCoins);

        if (order.getOrderPaymentStatus() == OrderPaymentStatus.UNPAID) {
            notificationService.notifyUnpaidOrderPermanently(order, sumToPayInCoins, paymentSystemResponse);
        }

        return paymentSystemResponse;
    }

    @Override
    @Transactional
    public PaymentSystemResponse processOrder(String userUuid, OrderWayForPayClientDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + dto.getOrderId()));
        checkOrderIsPaid(order.getOrderPaymentStatus());
        User currentUser = userRepository.findUserByUuid(userUuid)
            .orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST + userUuid));
        checkForNullCounter(order);
        long sumToPayInCoins = paymentCalculatorService.calculateSumToPay(dto, order, currentUser);

        transferUserPointsToOrder(order, dto.getPointsToUse());
        paymentVerification(sumToPayInCoins, order);

        if (sumToPayInCoins <= 0) {
            return wayForPayService.getPaymentRequestDto(order, null);
        } else {
            String link = formedLink(order, sumToPayInCoins);
            return wayForPayService.getPaymentRequestDto(order, link);
        }
    }

    @Override
    @Transactional
    public String formedLink(Order order, long sumToPayInCoins) {
        Order increment = incrementCounter(order);
        PaymentWayForPayRequestDto paymentWayForPayRequestDto =
            wayForPayService.formPaymentRequestForWayForPay(increment.getId(), sumToPayInCoins);
        paymentWayForPayRequestDto
            .setOrderReference(OrderUtils.generateEncodedOrderReference(increment.getId(), order));
        return wayForPayService.getLinkFromWayForPayCheckoutResponse(
            wayForPayClient.getCheckOutResponse(paymentWayForPayRequestDto));
    }

    private void validateOrderRequestAddress(OrderResponseDto dto) {
        if (!checkIfAddressMatchLocationArea(dto.getLocationId(), dto.getAddressId())) {
            throw new AddressNotWithinLocationAreaException(AppConstant.ADDRESS_NOT_WITHIN_LOCATION_AREA_MESSAGE);
        }
    }

    //TODO move to address
    private boolean checkIfAddressMatchLocationArea(long locationId, long addressId) {
        Address address = addressRepo.findById(addressId)
            .orElseThrow(() -> new NotFoundException(AppConstant.ADDRESS_NOT_FOUND_BY_ID_MESSAGE + addressId));

        boolean isKyivTariff = checkIfCityBelongsToKyivTariff(address.getBaseAddress().getCityEn());

        if (locationId == TariffLocation.KYIV_TARIFF.getLocationId()) {
            return isKyivTariff;
        } else if (locationId == TariffLocation.KYIV_REGION_20_KM_TARIFF.getLocationId()) {
            checkAndCalculateAddressCoordinatesIfEmpty(address);

            double addressLatitude = address.getCoordinates().getLatitude();
            double addressLongitude = address.getCoordinates().getLongitude();

            double distanceInKm =
                DistanceCalculationUtils.calculateDistanceInKmByHaversineFormula(AppConstant.KYIV_LATITUDE,
                    AppConstant.KYIV_LONGITUDE,
                    addressLatitude, addressLongitude);

            return distanceInKm <= AppConstant.LOCATION_40_KM_ZONE_VALUE && !isKyivTariff;
        } else {
            return locationRepository.findAddressAndLocationNamesMatch(locationId, addressId).isPresent();
        }
    }

    private boolean checkIfCityBelongsToKyivTariff(String cityName) {
        return Arrays.stream(KyivTariffLocation.values())
            .anyMatch(kyivTariffLocation -> kyivTariffLocation.getLocationName().equalsIgnoreCase(cityName));
    }

    //TODO Move to Address
    private void checkAndCalculateAddressCoordinatesIfEmpty(Address address) {
        if (address.getCoordinates().getLatitude() == 0.0 && address.getCoordinates().getLongitude() == 0.0) {
            LatLng latLng = googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(AppConstant.UKRAINE_EN,
                    address.getBaseAddress().getCityEn(),
                    AppConstant.LANG_EN).geometry.location;
            Coordinates addressCoordinates = Coordinates.builder().latitude(latLng.lat).longitude(latLng.lng).build();
            address.setCoordinates(addressCoordinates);
            addressRepo.save(address);
        }
    }

    private void adjustPaymentDetails(OrderResponseDto dto) {
        if (!dto.isShouldBePaid()) {
            dto.setCertificates(Collections.emptySet());
            dto.setPointsToUse(0);
        }
    }

    //TODO move to OrderAdress
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

    private Order formAndSaveOrderRequest(OrderResponseDto dto, Order order, User currentUser, UBSuser userData) {
        TariffsInfo tariffsInfo = findTariffsInfoByBagIdsWithinLocation(
            getBagIds(dto.getBags()), dto.getLocationId());
        List<OrderBag> bagsOrdered = new ArrayList<>();
        long sumToPayInCoinsWithoutDiscount = bagCalculatorService
            .prepareBagsAndCalculateTotal(bagsOrdered, dto.getBags(), tariffsInfo);

        pointsUtils.checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        long sumToPayInCoins = pointCalculatorService
            .reduceOrderSumDueToUsedPoints(sumToPayInCoinsWithoutDiscount, dto.getPointsToUse());
        if (sumToPayInCoinsWithoutDiscount == sumToPayInCoins) {
            order.setPointsToUse(0);
            dto.setPointsToUse(0);
        }

        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPayInCoins =
            certificateCalculatorService.applyCertificatesToOrder(
                dto, orderCertificates, order, sumToPayInCoins);
        if (sumToPayInCoins <= 0) {
            dto.setShouldBePaid(false);
        }
        return formAndSaveOrder(order, orderCertificates, bagsOrdered, userData, currentUser, sumToPayInCoins,
            tariffsInfo);
    }

    //TODO move to tarifs
    private TariffsInfo findTariffsInfoByBagIdsWithinLocation(List<Integer> bagIds, Long locationId) {
        return tariffsInfoRepository.findTariffsInfoByBagIdAndLocationId(bagIds, locationId)
            .orElseThrow(
                () -> new NotFoundException(String.format(TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST, bagIds, locationId)));
    }

    private List<Integer> getBagIds(List<BagDto> dto) {
        return dto.stream()
            .map(BagDto::getId)
            .toList();
    }

    private Order formAndSaveOrder(
        Order order, Set<Certificate> orderCertificates, List<OrderBag> bagsOrdered,
        UBSuser userData, User currentUser, long sumToPayInCoins, TariffsInfo tariffsInfo
    ) {
        order.setTariffsInfo(tariffsInfo);
        order.setCertificates(orderCertificates);
        order.setOrderBags(bagsOrdered);
        order.setUbsUser(userData);
        order.setUser(currentUser);
        order.setSumTotalAmountWithoutDiscounts(
            paymentCalculatorService.calculateOrderSumWithoutDiscounts(bagsOrdered));
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

    private void saveOrderEvent(String eventName, String author, Order order) {
        eventService.save(eventName, author, order);
        log.info("Saved event: eventName={}, author={}, orderId={}", eventName, author, order.getId());
    }

    private PaymentSystemResponse processPaymentResponse(OrderResponseDto dto, Order order, long sumToPayInCoins) {
        if (dto.isShouldBePaid()) {
            return paymentStrategyFactory.getPaymentStrategy(dto.getPaymentSystem())
                .processPayment(order, sumToPayInCoins);
        } else {
            return wayForPayService.getPaymentRequestDto(order, "");
        }
    }

    //TODO make duplicate or move to conroler like in project learning with pre authorised
    private void checkIsOrderOfCurrentUser(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER);
        }
    }

    //TODO move to AdressService
    private OrderAddress getOrUpdateOrderAddress(
        OrderAddress currentOrderAddress, Long newAddressId, Long newLocationId, User currentUser) {
        OrderAddress newOrderAddress = formOrderAddress(newAddressId, newLocationId, currentUser);
        newOrderAddress.setId(currentOrderAddress.getId());
        if (currentOrderAddress.equals(newOrderAddress)) {
            return currentOrderAddress;
        }
        return orderAddressRepository.save(newOrderAddress);
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
        pointsUtils.checkIfUserHaveEnoughPoints(user.getCurrentPoints(), pointsToUse);

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

    private Order incrementCounter(Order order) {
        order.setCounterOrderPaymentId(order.getCounterOrderPaymentId() + 1);
        orderRepository.save(order);
        return order;
    }
}
