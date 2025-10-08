package greencity.service.ubs.order;

import static greencity.constant.AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT_EN;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_ORDER_CANCELLATION_REASON;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TOO_MUCH_POINTS_FOR_ORDER;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_CANCEL_EXCEPTION;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_GROUP;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_KEY;
import static greencity.constant.QuartzConstants.QUARTZ_SCHEDULER_EXCEPTION;
import static java.util.Objects.nonNull;
import greencity.constant.AppConstant;
import greencity.dto.address.AddressInfoDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.notification.SenderInfoDto;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.BonusReason;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.calculator.BagCalculatorService;
import greencity.service.ubs.calculator.CertificateCalculatorService;
import greencity.service.ubs.calculator.PaymentCalculatorService;
import greencity.service.ubs.calculator.PointCalculatorService;
import greencity.util.MoneyConverterUtil;
import greencity.util.PointsUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final OrdersForUserRepository ordersForUserRepository;
    private final UserRepository userRepository;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final PointCalculatorService pointCalculatorService;
    private final BagCalculatorService bagCalculatorService;
    private final CertificateCalculatorService certificateCalculatorService;
    private final PaymentCalculatorService paymentCalculatorService;
    private final PointsUtils pointsUtils;
    private final MoneyConverterUtil moneyConverterUtil;
    private final ModelMapper modelMapper;
    private final Scheduler quartzScheduler;

    @Override
    public Order formAndSaveOrderRequest(OrderResponseDto dto, Order order, User currentUser, UBSuser userData) {
        TariffsInfo tariffsInfo = findTariffsInfoByBagIdsWithinLocation(getBagIds(dto.getBags()), dto.getLocationId());
        List<OrderBag> bagsOrdered = prepareBags(dto, tariffsInfo);
        Set<Certificate> orderCertificates = new HashSet<>();

        long sumToPayInCoinsWithoutDiscount = calculateTotal(bagsOrdered);
        pointsUtils.checkIfUserHasEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());

        long sumToPayInCoins =
            applyBonusesAndCertificates(dto, order, sumToPayInCoinsWithoutDiscount, orderCertificates);

        if (sumToPayInCoins <= 0) {
            dto.setShouldBePaid(false);
        }
        return formAndSaveOrder(order, orderCertificates, bagsOrdered, userData, currentUser, sumToPayInCoins,
            tariffsInfo);
    }

    @Override
    public void transferUserPointsToOrder(Order order, Integer pointsToUse) {
        if (pointsToUse <= 0) {
            return;
        }

        User user = order.getUser();
        pointsUtils.checkIfUserHasEnoughPoints(user.getCurrentPoints(), pointsToUse);

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

    @Override
    public OrdersDataForUserDto getOrderForUser(String uuid, Long id) {
        Order order = ordersForUserRepository.getAllByUserUuidAndId(uuid, id);
        if (order == null) {
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }

        return getOrdersData(order);
    }

    @Override
    public OrderPaymentDetailDto getOrderPaymentDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return buildOrderPaymentDetailDto(order);
    }

    @Override
    public OrderCancellationReasonDto getOrderCancellationReason(Long orderId, String uuid) {
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

    @Override
    public void deleteOrder(String uuid, Long id) {
        Order order = ordersForUserRepository.getAllByUserUuidAndId(uuid, id);
        if (order == null) {
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }
        order.getOrderBags().clear();
        orderRepository.saveAndFlush(order);
        orderRepository.delete(order);
    }

    @Override
    public OrdersDataForUserDto getOrdersData(Order order) {
        List<BagForUserDto> bags = bagCalculatorService.bagForUserDtosBuilder(order);
        List<CertificateDto> certificates = mapCertificates(order);

        Long fullPrice = bagCalculatorService.calculateBagsSum(bags);
        Long amountWithDiscount = calculateDiscountedAmount(order, fullPrice, certificates);
        Long paidAmount = paymentCalculatorService.countPaidAmount(order.getPayment());

        Double remainingToPay = moneyConverterUtil
            .convertCoinsIntoBills(amountWithDiscount - paidAmount);
        double refundedBonuses = calculateRefundedBonuses(order);
        Double refundedMoney = calculateRefundedMoney(order);

        OrderStatusTranslation orderStatusTranslation = getOrderStatus(order);
        OrderPaymentStatusTranslation paymentStatusTranslation = getPaymentStatus(order);

        OrderDataBuilderContext context = new OrderDataBuilderContext(
            order, orderStatusTranslation, bags, remainingToPay, refundedBonuses,
            refundedMoney, paidAmount, fullPrice, certificates, paymentStatusTranslation);

        return buildOrdersDataForUserDto(context);
    }

    @Override
    public void cancelPaymentExpiryJob(Long orderId) {
        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);
        try {
            if (!quartzScheduler.deleteJob(jobKey)) {
                throw new IllegalStateException(PAYMENT_EXPIRY_CANCEL_EXCEPTION);
            }
        } catch (SchedulerException exception) {
            throw new IllegalStateException(QUARTZ_SCHEDULER_EXCEPTION);
        }
    }

    private long applyBonusesAndCertificates(OrderResponseDto dto, Order order, long sumToPayInCoinsWithoutDiscount,
        Set<Certificate> orderCertificates) {
        long sumToPayInCoins = pointCalculatorService
            .reduceOrderSumDueToUsedPoints(sumToPayInCoinsWithoutDiscount, dto.getPointsToUse());
        if (sumToPayInCoinsWithoutDiscount == sumToPayInCoins) {
            order.setPointsToUse(0);
            dto.setPointsToUse(0);
        }

        sumToPayInCoins =
            certificateCalculatorService.applyCertificatesToOrder(
                dto, orderCertificates, order, sumToPayInCoins);
        return sumToPayInCoins;
    }

    private List<OrderBag> prepareBags(OrderResponseDto dto, TariffsInfo tariffsInfo) {
        List<OrderBag> bags = new ArrayList<>();
        bagCalculatorService.prepareBagsAndCalculateTotal(bags, dto.getBags(), tariffsInfo);
        return bags;
    }

    private long calculateTotal(List<OrderBag> bagsOrdered) {
        return bagsOrdered.stream().mapToLong(OrderBag::getPrice).sum();
    }

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
        UBSuser userData, User currentUser, long sumToPayInCoins, TariffsInfo tariffsInfo) {
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

    private OrderPaymentDetailDto buildOrderPaymentDetailDto(Order order) {
        int certificatePointsInCoins = order.getCertificates().stream()
            .flatMapToInt(c -> IntStream.of(c.getPoints()))
            .reduce(Integer::sum).orElse(0) * AppConstant.CURRENCY_CONVERSION_RATE;
        int pointsToUseInCoins = order.getPointsToUse() * AppConstant.CURRENCY_CONVERSION_RATE;
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

    private List<CertificateDto> mapCertificates(Order order) {
        return order.getCertificates().stream()
            .map(certificate -> modelMapper.map(certificate, CertificateDto.class))
            .toList();
    }

    private long calculateDiscountedAmount(Order order, Long fullPriceInCoins, List<CertificateDto> certificates) {
        return fullPriceInCoins
            - (long) AppConstant.CURRENCY_CONVERSION_RATE * (order.getPointsToUse()
                + certificateCalculatorService.countCertificatesBonuses(certificates));
    }

    private double calculateRefundedBonuses(Order order) {
        double refundedBonuses = order.getPayment().stream()
            .filter(payment -> ENROLLMENT_TO_THE_BONUS_ACCOUNT_EN.equals(payment.getReceiptLink()))
            .map(payment -> payment.getAmount().doubleValue())
            .reduce(0.0, Double::sum);

        refundedBonuses /= -AppConstant.CURRENCY_CONVERSION_RATE;
        return refundedBonuses;
    }

    private double calculateRefundedMoney(Order order) {
        return order.getRefund() == null
            ? 0.0
            : order.getRefund().getAmount().doubleValue() / AppConstant.CURRENCY_CONVERSION_RATE;
    }

    private OrderPaymentStatusTranslation getPaymentStatus(Order order) {
        return orderPaymentStatusTranslationRepository
            .getById((long) order.getOrderPaymentStatus().getStatusValue());
    }

    private OrderStatusTranslation getOrderStatus(Order order) {
        return orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue())
            .orElse(orderStatusTranslationRepository.getReferenceById(1L));
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

    private OrdersDataForUserDto buildOrdersDataForUserDto(OrderDataBuilderContext ctx) {
        return OrdersDataForUserDto.builder()
            .id(ctx.order().getId())
            .dateForm(ctx.order().getOrderDate())
            .datePaid(ctx.order().getOrderDate())
            .orderStatusUk(ctx.orderStatus().getNameUk())
            .orderStatusEn(ctx.orderStatus().getNameEn())
            .orderComment(ctx.order().getComment())
            .bags(ctx.bags())
            .additionalOrders(ctx.order().getAdditionalOrders())
            .amountBeforePayment(ctx.amountBeforePayment())
            .refundedBonuses(ctx.refundedBonuses())
            .refundedMoney(ctx.refundedMoney())
            .paidAmount(moneyConverterUtil.convertCoinsIntoBills(ctx.paidAmountInCoins()))
            .orderFullPrice(moneyConverterUtil.convertCoinsIntoBills(ctx.fullPriceInCoins()))
            .certificate(ctx.certificates())
            .bonuses(ctx.order().getPointsToUse().doubleValue())
            .sender(senderInfoDtoBuilder(ctx.order()))
            .address(addressInfoDtoBuilder(ctx.order()))
            .paymentStatusUk(ctx.paymentStatus().getTranslationValueUk())
            .paymentStatusEn(ctx.paymentStatus().getTranslationsValueEn())
            .paymentLink(ctx.order().getPaymentLink())
            .paymentLinkExpiry(ctx.order().getPaymentLinkExpiry())
            .build();
    }

    private record OrderDataBuilderContext(
        Order order,
        OrderStatusTranslation orderStatus,
        List<BagForUserDto> bags,
        Double amountBeforePayment,
        double refundedBonuses,
        Double refundedMoney,
        Long paidAmountInCoins,
        Long fullPriceInCoins,
        List<CertificateDto> certificates,
        OrderPaymentStatusTranslation paymentStatus) {
    }
}
