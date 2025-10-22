package greencity.service.ubs.order;

import static greencity.constant.AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT_EN;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_ORDER_CANCELLATION_REASON;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.TARIFF_FOR_BAGS_AT_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TOO_MUCH_POINTS_FOR_ORDER;
import static greencity.constant.ErrorMessage.UBS_USER_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.USER_NOT_FOUND_BY_ID;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_CANCEL_EXCEPTION;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_GROUP;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_KEY;
import static greencity.constant.QuartzConstants.QUARTZ_SCHEDULER_EXCEPTION;
import static java.util.Objects.nonNull;
import greencity.constant.AppConstant;
import greencity.dto.address.AddressInfoDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.bag.BagMappingDto;
import greencity.dto.bag.BagTransDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.notification.SenderInfoDto;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailDto;
import greencity.dto.order.OrderDetailInfoDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentWithStatusDto;
import greencity.entity.order.Bag;
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
import greencity.enums.CertificateStatus;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.persistence.JpqlQueryHelper;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.OrdersForUserRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.OrderBagService;
import greencity.service.ubs.PaymentUtil;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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
    private final JpqlQueryHelper jpqlQueryHelper;
    private final CertificateRepository certificateRepository;
    private final OrderBagRepository orderBagRepository;
    private final UBSUserRepository ubsUserRepository;
    private final OrdersForUserRepository ordersForUserRepository;
    private final OrderBagService orderBagService;
    private final BagRepository bagRepository;

    @Override
    public Long formAndSaveOrderRequest(OrderResponseDto dto, Long orderId, Long userId, Long ubsUserId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_BY_ID + userId));
        UBSuser userData = ubsUserRepository.findById(ubsUserId)
            .orElseThrow(() -> new NotFoundException(UBS_USER_NOT_FOUND_BY_ID + ubsUserId));

        TariffsInfo tariffsInfo = findTariffsInfoByBagIdsWithinLocation(getBagIds(dto.getBags()), dto.getLocationId());
        List<BagInfoDto> bagsOrdered = prepareBags(dto, order, tariffsInfo.getId());
        Set<CertificateDto> orderCertificates = new HashSet<>();
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
    public void transferUserPointsToOrder(Long orderId, Integer pointsToUse) {
        if (pointsToUse <= 0) {
            return;
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
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
        boolean statusesIncluded = nonNull(statuses) && !statuses.isEmpty();
        String statusesLine = statusesIncluded ? "AND o.orderStatus IN (:statuses) " : "";
        String jpqlQueryString = "SELECT o FROM Order AS o WHERE o.user = "
            + "(SELECT u FROM User AS u WHERE u.uuid = :uuid) "
            + statusesLine
            + "ORDER BY o.orderDate DESC";
        TypedQuery<Order> jpqlQuery = jpqlQueryHelper
            .createPageableTypedQueryWithEntityGraph(
                Order.class, jpqlQueryString, List.of("refund", "ubsUser", "ubsUser.orderAddress"), page);
        jpqlQuery.setParameter("uuid", uuid);
        if (statusesIncluded) {
            jpqlQuery.setParameter("statuses", statuses);
        }
        Page<Order> orderPages = jpqlQueryHelper.runPageableTypedQueryWithEntityGraph(jpqlQuery, page);
        List<Order> orders = orderPages.getContent();
        List<OrdersDataForUserDto> dtos = new ArrayList<>();
        orders.forEach(order -> dtos.add(getOrdersData(order.getId())));

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
            throw new NotFoundException(ORDER_NOT_FOUND_BY_ID + id);
        }

        return getOrdersData(id);
    }

    @Override
    public OrderPaymentDetailDto getOrderPaymentDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        return buildOrderPaymentDetailDto(order);
    }

    @Override
    public OrderCancellationReasonDto getOrderCancellationReason(Long orderId, String uuid) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
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
            throw new NotFoundException(ORDER_NOT_FOUND_BY_ID + id);
        }
        unlockPointsAndCertificatesFromOrder(order);
        order.getOrderBags().clear();
        orderRepository.saveAndFlush(order);
        orderRepository.delete(order);
    }

    @Override
    public OrdersDataForUserDto getOrdersData(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        OrderInfoDto orderInfo = modelMapper.map(order, OrderInfoDto.class);
        orderInfo.setOrderPrice(PaymentUtil.getPriceDetails(
            orderInfo.getId(), orderRepository, orderBagService, certificateRepository)
            .getTotalSumAmount());
        List<BagForUserDto> bags = bagCalculatorService.bagForUserDtosBuilder(orderInfo);
        List<CertificateDto> certificates = mapCertificates(order);

        Long fullPrice = bagCalculatorService.calculateBagsSum(bags);
        Long amountWithDiscount = calculateDiscountedAmount(order, fullPrice, certificates);
        List<PaymentWithStatusDto> payments = order.getPayment().stream()
            .map(payment -> modelMapper.map(payment, PaymentWithStatusDto.class))
            .toList();
        Long paidAmount = paymentCalculatorService.countPaidAmount(payments);

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
            if (quartzScheduler.checkExists(jobKey) && !quartzScheduler.deleteJob(jobKey)) {
                throw new IllegalStateException(PAYMENT_EXPIRY_CANCEL_EXCEPTION);
            }
        } catch (SchedulerException exception) {
            throw new IllegalStateException(QUARTZ_SCHEDULER_EXCEPTION);
        }
    }

    @Override
    public OrderDetailDto getOrderDetails(Long orderId) {
        OrderDetailDto dto = new OrderDetailDto();
        Order order = orderRepository.getOrderDetails(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));

        dto.setAmount(modelMapper.map(order, new TypeToken<List<BagMappingDto>>() {}.getType()));
        dto.setCapacityAndPrice(orderBagService.findAllBagsByOrderId(order.getId())
            .stream()
            .map(b -> modelMapper.map(b, BagInfoDto.class))
            .collect(Collectors.toList()));
        dto.setName(bagRepository.findAllByOrder(order.getId())
            .stream()
            .map(b -> modelMapper.map(b, BagTransDto.class))
            .collect(Collectors.toList()));
        dto.setOrderId(order.getId());

        return dto;
    }

    @Override
    public List<OrderDetailInfoDto> getOrderDetailsInfo(Long orderId) {
        OrderDetailDto orderDeatails = getOrderDetails(orderId);
        return modelMapper.map(orderDeatails, new TypeToken<List<OrderDetailInfoDto>>() {}.getType());
    }

    private long applyBonusesAndCertificates(OrderResponseDto dto, Order order, long sumToPayInCoinsWithoutDiscount,
        Set<CertificateDto> orderCertificates) {
        long sumToPayInCoins = pointCalculatorService
            .reduceOrderSumDueToUsedPoints(sumToPayInCoinsWithoutDiscount, dto.getPointsToUse());
        if (sumToPayInCoinsWithoutDiscount == sumToPayInCoins) {
            order.setPointsToUse(0);
            dto.setPointsToUse(0);
        }

        sumToPayInCoins =
            certificateCalculatorService.applyCertificatesToOrder(
                dto, orderCertificates, order.getId(), sumToPayInCoins);
        return sumToPayInCoins;
    }

    private List<BagInfoDto> prepareBags(OrderResponseDto dto, Order order, Long tariffsInfoId) {
        List<BagInfoDto> bags = new ArrayList<>();
        bagCalculatorService.prepareBagsAndCalculateTotal(bags, dto.getBags(), tariffsInfoId);
        List<OrderBag> savedBags = orderBagRepository.findOrderBagsByOrderId(order.getId());
        List<Long> savedBagIds = savedBags.stream()
            .mapToLong(b -> b.getBag().getId())
            .boxed()
            .toList();
        bags.forEach(bag -> {
            int orderBagIndex = savedBagIds.indexOf(bag.getId().longValue());
            Long convertedPrice = bag.getPrice().longValue() * AppConstant.CURRENCY_CONVERSION_RATE;

            if (orderBagIndex == -1) {
                Bag bagEntity = bagRepository.findById(bag.getId())
                    .orElseThrow(() -> new NotFoundException("Bag not found"));
                OrderBag newOrderBag = OrderBag.builder()
                    .order(order)
                    .bag(bagEntity)
                    .amount(bag.getAmount())
                    .price(convertedPrice)
                    .nameUk(bag.getNameUk())
                    .nameEn(bag.getNameEn())
                    .capacity(bag.getCapacity())
                    .build();
                orderBagRepository.save(newOrderBag);
            } else {
                savedBags.get(orderBagIndex).setAmount(bag.getAmount());
            }
        });
        return bags;
    }

    private long calculateTotal(List<BagInfoDto> bagsOrdered) {
        return bagsOrdered.stream()
            .map(e -> e.getPrice() * e.getAmount())
            .mapToLong(e -> (long) (e * AppConstant.CURRENCY_CONVERSION_RATE))
            .sum();
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

    private Long formAndSaveOrder(
        Order order, Set<CertificateDto> orderCertificates, List<BagInfoDto> bagsOrdered,
        UBSuser userData, User currentUser, long sumToPayInCoins, TariffsInfo tariffsInfo) {
        List<String> certificatesIds = orderCertificates.stream()
            .map(CertificateDto::getCode)
            .toList();
        Set<Certificate> certificates = new HashSet<>(certificateRepository.findAllById(certificatesIds));
        order.setCertificates(certificates);
        order.setOrderBags(orderBagRepository.findOrderBagsByOrderId(order.getId()));

        order.setTariffsInfo(tariffsInfo);
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
        order = orderRepository.save(order);
        return order.getId();
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

    private void unlockPointsAndCertificatesFromOrder(Order order) {
        User user = order.getUser();
        int pointsToUse = order.getPointsToUse();

        if (pointsToUse > 0) {
            user.setCurrentPoints(user.getCurrentPoints() + pointsToUse);
            user.getChangeOfPointsList().add(ChangeOfPoints.builder()
                .user(user)
                .amount(pointsToUse)
                .date(LocalDateTime.now())
                .reason(BonusReason.RETURN_CANCELED_DRAFT_ORDER)
                .build());
        }

        order.getCertificates().forEach(this::unlockCertificate);
    }

    private void unlockCertificate(Certificate certificate) {
        certificate.setOrder(null);
        certificate.setCertificateStatus(CertificateStatus.ACTIVE);
        certificate.setDateOfUse(null);
        certificate.setPoints(certificate.getInitialPointsValue());
    }
}
