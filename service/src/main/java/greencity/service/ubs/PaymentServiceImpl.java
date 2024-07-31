package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.ManualPaymentResponseDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.entity.order.*;
import greencity.entity.user.employee.Employee;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.PaymentType;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.locations.LocationApiService;
import greencity.service.notification.NotificationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.*;
import static greencity.service.ubs.UBSManagementServiceImpl.FORMAT_DATE;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final ModelMapper modelMapper;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final EventService eventService;
    private final FileService fileService;
    private final OrderRepository orderRepository;
    private final UBSManagementServiceImpl ubsManagementServiceImpl;
    private final PaymentUtil paymentUtil;
    private final NotificationService notificationService;
    private final OrderBagService orderBagService;
    private final CertificateRepository certificateRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final UserRemoteClient userRemoteClient;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BagRepository bagRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final ServiceRepository serviceRepository;
    private final OrdersAdminsPageService ordersAdminsPageService;
    private final LocationApiService locationApiService;
    private final RefundRepository refundRepository;
    private final OrderLockService orderLockService;

    /**
     * Method gets all order payments, count paid amount, amount which user should
     * paid and overpayment amount.
     *
     * @param orderId  of {@link Long} order id;
     * @param sumToPay of {@link Double} sum to pay;
     * @return {@link PaymentTableInfoDto }
     * @author Nazar Struk, Ostap Mykhailivskyi
     */
    @Override
    public PaymentTableInfoDto getPaymentInfo(long orderId, Double sumToPay) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Long sumToPayInCoins = paymentUtil.convertBillsIntoCoins(sumToPay);
        Long paidAmountInCoins = calculatePaidAmount(order);
        Long overpaymentInCoins = calculateOverpayment(order, sumToPayInCoins);
        Long unPaidAmountInCoins = calculateUnpaidAmount(order, sumToPayInCoins, paidAmountInCoins);
        PaymentTableInfoDto paymentTableInfoDto = new PaymentTableInfoDto();
        paymentTableInfoDto.setOverpayment(paymentUtil.convertCoinsIntoBills(overpaymentInCoins));
        paymentTableInfoDto.setUnPaidAmount(paymentUtil.convertCoinsIntoBills(unPaidAmountInCoins));
        paymentTableInfoDto.setPaidAmount(paymentUtil.convertCoinsIntoBills(paidAmountInCoins));
        List<PaymentInfoDto> paymentInfoDtos = order.getPayment().stream()
                .filter(payment -> payment.getPaymentStatus().equals(PaymentStatus.PAID))
                .map(x -> modelMapper.map(x, PaymentInfoDto.class)).collect(Collectors.toList());
        paymentTableInfoDto.setPaymentInfoDtos(paymentInfoDtos);
        return paymentTableInfoDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto, MultipartFile image, String email) {
        if (Objects.isNull(image) && StringUtils.isBlank(paymentRequestDto.getReceiptLink())) {
            throw new BadRequestException("Receipt link or image must be present");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        checkAvailableOrderForEmployee(order, email);
        ManualPaymentResponseDto manualPaymentResponseDto = buildPaymentResponseDto(
                paymentRepository.save(buildPaymentEntity(order, paymentRequestDto, image, email)));
        updateOrderPaymentStatusForManualPayment(order);

        return manualPaymentResponseDto;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteManualPayment(Long paymentId, String uuid) {
        Employee employee = employeeRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not found"));
        if (payment.getImagePath() != null) {
            fileService.delete(payment.getImagePath());
        }
        paymentRepository.deletePaymentById(paymentId);
        eventService.save(OrderHistory.DELETE_PAYMENT_MANUALLY + payment.getPaymentId(),
                employee.getFirstName() + "  " + employee.getLastName(), payment.getOrder());
        updateOrderPaymentStatusForManualPayment(payment.getOrder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManualPaymentResponseDto updateManualPayment(Long paymentId, ManualPaymentRequestDto paymentRequestDto, MultipartFile image, String uuid) {
        Employee employee = employeeRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new NotFoundException(PAYMENT_NOT_FOUND + paymentId));
        Payment paymentUpdated = paymentRepository.save(changePaymentEntity(payment, paymentRequestDto, image));
        eventService.save(OrderHistory.UPDATE_PAYMENT_MANUALLY + paymentRequestDto.getPaymentId(),
                employee.getFirstName() + "  " + employee.getLastName(), payment.getOrder());

        ManualPaymentResponseDto manualPaymentResponseDto = buildPaymentResponseDto(paymentUpdated);
        updateOrderPaymentStatusForManualPayment(payment.getOrder());
        return manualPaymentResponseDto;
    }

    private void updateOrderPaymentStatusForManualPayment(Order order) {
        CounterOrderDetailsDto dto = getPriceDetails(order.getId());
        double paymentsForCurrentOrder = order.getPayment().stream().filter(payment -> payment.getPaymentStatus()
                        .equals(PaymentStatus.PAID)).map(Payment::getAmount).map(PaymentUtil::convertCoinsIntoBills).reduce(Double::sum)
                .orElse((double) 0);
        double totalPaidAmount = paymentsForCurrentOrder + dto.getCertificateBonus() + dto.getBonus();
        double totalAmount = paymentUtil.setTotalPrice(dto);

        if (paymentsForCurrentOrder > 0 && totalAmount > totalPaidAmount) {
            order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
            eventService.save(OrderHistory.ORDER_HALF_PAID, OrderHistory.SYSTEM, order);
            notificationService.notifyHalfPaidPackage(order);
        } else if (paymentsForCurrentOrder > 0 && totalAmount <= totalPaidAmount) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
            notificationService.notifyPaidOrder(order);
        } else if (paymentsForCurrentOrder == 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        }
        orderRepository.save(order);
    }

    public CounterOrderDetailsDto getPriceDetails(Long id) {
        CounterOrderDetailsDto dto = new CounterOrderDetailsDto();
        Order order = orderRepository.getOrderDetails(id)
                .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Bag> bag = orderBagService.findAllBagsInOrderBagsList(order.getOrderBags());
        final List<Certificate> currentCertificate = certificateRepository.findCertificate(id);

        long sumAmountInCoins = 0;
        long sumConfirmedInCoins = 0;
        long sumExportedInCoins = 0;
        long totalSumAmountInCoins;
        long totalSumConfirmedInCoins;
        long totalSumExportedInCoins;
        if (!bag.isEmpty()) {
            sumAmountInCoins = getSumInCoins(order.getAmountOfBagsOrdered().entrySet(), bag);
            sumConfirmedInCoins = getSumInCoins(order.getConfirmedQuantity().entrySet(), bag);
            sumExportedInCoins = getSumInCoins(order.getExportedQuantity().entrySet(), bag);

            if (order.getExportedQuantity().size() != 0) {
                sumExportedInCoins += getUbsCourierOrWriteOffStationSum(order);
            } else if (order.getConfirmedQuantity().size() != 0) {
                sumConfirmedInCoins += getUbsCourierOrWriteOffStationSum(order);
            } else {
                sumAmountInCoins += getUbsCourierOrWriteOffStationSum(order);
            }
        }

        if (!currentCertificate.isEmpty()) {
            Integer certificateBonus = currentCertificate.stream()
                    .map(Certificate::getPoints).reduce(Integer::sum).orElse(0);
            long certificatesAndBonusesInCoins = 100L * (certificateBonus + order.getPointsToUse());
            long totalSumAmountInCoinsToCheck = sumAmountInCoins - certificatesAndBonusesInCoins;
            totalSumAmountInCoins = totalSumAmountInCoinsToCheck <= 0 ? 0 : totalSumAmountInCoinsToCheck;
            totalSumConfirmedInCoins = sumConfirmedInCoins - certificatesAndBonusesInCoins;
            totalSumExportedInCoins = sumExportedInCoins - certificatesAndBonusesInCoins;
            dto.setCertificateBonus(certificateBonus.doubleValue());
            dto.setCertificate(currentCertificate.stream().map(Certificate::getCode).collect(Collectors.toList()));
        } else {
            dto.setCertificateBonus((double) 0);
            long bonusesInCoins = 100L * order.getPointsToUse();
            totalSumAmountInCoins = sumAmountInCoins - bonusesInCoins;
            totalSumConfirmedInCoins = sumConfirmedInCoins - bonusesInCoins;
            totalSumExportedInCoins = sumExportedInCoins - bonusesInCoins;
        }
        if (order.getConfirmedQuantity().isEmpty()) {
            totalSumConfirmedInCoins = 0;
        }
        if (order.getExportedQuantity().isEmpty()) {
            totalSumExportedInCoins = 0;
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

        setDtoInfoFromOrder(dto, order);
        setDtoInfo(dto, sumAmountInCoins, sumExportedInCoins, sumConfirmedInCoins,
                totalSumAmountInCoins, totalSumConfirmedInCoins, totalSumExportedInCoins);
        return dto;
    }

    void checkAvailableOrderForEmployee(Order order, String email) {
        Long employeeId = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        Optional<TariffsInfo> tariffsInfoOptional = tariffsInfoRepository.findTariffsInfoByIdForEmployee(
                order.getTariffsInfo().getId(), employeeId);
        if (tariffsInfoOptional.isEmpty()) {
            throw new BadRequestException(ErrorMessage.CANNOT_ACCESS_ORDER_FOR_EMPLOYEE + order.getId());
        }
    }

    void setDtoInfoFromOrder(CounterOrderDetailsDto dto, Order order) {
        dto.setOrderComment(order.getComment());
        dto.setNumberOrderFromShop(order.getAdditionalOrders());
        dto.setBonus(order.getPointsToUse().doubleValue());
    }

    void setDtoInfo(CounterOrderDetailsDto dto, long sumAmountInCoins, long sumExportedInCoins,
                    long sumConfirmedInCoins, long totalSumAmountInCoins, long totalSumConfirmedInCoins,
                    long totalSumExportedInCoins) {
        dto.setSumAmount(paymentUtil.convertCoinsIntoBills(sumAmountInCoins));
        dto.setSumConfirmed(paymentUtil.convertCoinsIntoBills(sumConfirmedInCoins));
        dto.setSumExported(paymentUtil.convertCoinsIntoBills(sumExportedInCoins));
        dto.setTotalSumAmount(paymentUtil.convertCoinsIntoBills(totalSumAmountInCoins));
        dto.setTotalSumConfirmed(paymentUtil.convertCoinsIntoBills(totalSumConfirmedInCoins));
        dto.setTotalSumExported(paymentUtil.convertCoinsIntoBills(totalSumExportedInCoins));
    }

    /**
     * Helper method of {@link #getPriceDetails(Long)} which calculates the total
     * price in coins based on the provided entries and bag items.
     *
     * @param entries the set of entries representing the quantity of bags of
     *                {@link java.util.Map.Entry}
     * @param bag     the list of bags to calculate the price from {@link Bag}
     * @return the total price in coins {@link Long}
     * @throws NotFoundException if a bag with a specific ID was not found
     * @author Yurii Midianyi
     */
    Long getSumInCoins(Set<Map.Entry<Integer, Integer>> entries, List<Bag> bag) {
        long result = 0L;
        for (Map.Entry<Integer, Integer> entry : entries) {
            result += entry.getValue() * bag
                    .stream()
                    .filter(b -> b.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + entry.getKey()))
                    .getFullPrice();
        }
        return result;
    }

    Long getUbsCourierOrWriteOffStationSum(Order order) {
        if (order.getUbsCourierSum() != null && order.getWriteOffStationSum() == null) {
            return order.getUbsCourierSum();
        } else if (order.getWriteOffStationSum() != null && order.getUbsCourierSum() == null) {
            return order.getWriteOffStationSum();
        } else if (order.getWriteOffStationSum() != null && order.getUbsCourierSum() != null) {
            return order.getWriteOffStationSum() + order.getUbsCourierSum();
        } else {
            return 0L;
        }
    }


    Payment changePaymentEntity(Payment updatePayment,
                                ManualPaymentRequestDto requestDto,
                                MultipartFile image) {
        updatePayment.setSettlementDate(requestDto.getSettlementdate());
        updatePayment.setAmount(requestDto.getAmount());
        updatePayment.setPaymentId(requestDto.getPaymentId());
        updatePayment.setReceiptLink(requestDto.getReceiptLink());
        if (requestDto.getImagePath().isEmpty() && requestDto.getImagePath() != null) {
            if (updatePayment.getImagePath() != null) {
                fileService.delete(updatePayment.getImagePath());
            }
            updatePayment.setImagePath(null);
        }
        if (image != null) {
            updatePayment.setImagePath(fileService.upload(image));
        }
        return updatePayment;
    }



    /**
     * Method that calculates overpayment on user's order in coins.
     *
     * @param order           of {@link Order} order;
     * @param sumToPayInCoins of {@link Long} sum to pay in coins;
     * @return {@link Long }
     * @author Ostap Mykhailivskyi
     */
    Long calculateOverpayment(Order order, Long sumToPayInCoins) {
        long paidAmountInCoins = calculatePaidAmount(order);

        long certificateSum = order.getCertificates().stream()
                .map(Certificate::getPoints)
                .reduce(0, Integer::sum);
        long bonusOverpaymentInCoins = 100L * (certificateSum + order.getPointsToUse()) - sumToPayInCoins;

        long overpaymentInCoins = paidAmountInCoins + bonusOverpaymentInCoins;

        return OrderStatus.CANCELED == order.getOrderStatus()
                ? paidAmountInCoins
                : Math.max(overpaymentInCoins, 0L);
    }

    /**
     * Method that calculate paid amount in coins.
     *
     * @param order of {@link Order} order id;
     * @return {@link Long } paid amount in coins;
     * @author Ostap Mykhailivskyi
     */
     Long calculatePaidAmount(Order order) {
        return order.getPayment().stream()
                .filter(x -> x.getPaymentStatus().equals(PaymentStatus.PAID))
                .map(Payment::getAmount)
                .reduce(0L, Long::sum);
    }

    /**
     * Method that calculate unpaid amount in coins.
     *
     * @param order             of {@link Order} order id;
     * @param sumToPayInCoins   of {@link Long} sum to pay in coins;
     * @param paidAmountInCoins of {@link Long} paid amount in coins;
     * @return {@link Long } unpaid amount in coins
     * @author Ostap Mykhailivskyi
     */
     Long calculateUnpaidAmount(Order order, Long sumToPayInCoins, Long paidAmountInCoins) {
        long unpaidAmountInCoins = sumToPayInCoins - paidAmountInCoins
                                   - 100L * (order.getPointsToUse() + (order.getCertificates().stream()
                .map(Certificate::getPoints)
                .reduce(Integer::sum)
                .orElse(0)));

        return Math.max(unpaidAmountInCoins, 0);
    }


    ManualPaymentResponseDto buildPaymentResponseDto(Payment payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
        return ManualPaymentResponseDto.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .settlementdate(payment.getSettlementDate())
                .amount(payment.getAmount())
                .receiptLink(payment.getReceiptLink())
                .imagePath(payment.getImagePath())
                .currentDate(LocalDate.now().format(formatter))
                .build();
    }
    Payment buildPaymentEntity(Order order, ManualPaymentRequestDto paymentRequestDto, MultipartFile image,
                               String email) {
        Payment payment = Payment.builder()
                .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
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
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        eventService.save(OrderHistory.ADD_PAYMENT_MANUALLY + paymentRequestDto.getPaymentId(),
                employee.getFirstName() + "  " + employee.getLastName(), order);
        return payment;
    }


//    Double setTotalPrice(CounterOrderDetailsDto dto) {
//        if (paymentUtil.isContainsExportedBags(dto)) {
//            return dto.getSumExported();
//        }
//        if (paymentUtil.isContainsConfirmedBags(dto)) {
//            return dto.getSumConfirmed();
//        }
//        return dto.getSumAmount();
//    }
//    private Boolean isContainsConfirmedBags(CounterOrderDetailsDto dto) {
//        return dto.getSumConfirmed() != 0;
//    }
//
//    private Boolean isContainsExportedBags(CounterOrderDetailsDto dto) {
//        return dto.getSumExported() != 0;
//    }

}
