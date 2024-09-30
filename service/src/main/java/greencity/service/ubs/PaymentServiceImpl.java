package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.ManualPaymentResponseDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.dto.refund.RefundDto;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.order.Refund;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.PaymentType;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.CertificateRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.RefundRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import static greencity.constant.AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT_ENG;
import static greencity.constant.AppConstant.PAYMENT_REFUND_ENG;
import static greencity.constant.ErrorMessage.CANNOT_REFUND_MONEY;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static greencity.constant.ErrorMessage.INCOMPATIBLE_ORDER_STATUS_FOR_MONEY_REFUND;
import static greencity.constant.ErrorMessage.INVALID_REQUESTED_REFUND_AMOUNT;
import static greencity.constant.ErrorMessage.ORDER_CAN_NOT_BE_UPDATED;
import static greencity.constant.ErrorMessage.ORDER_HAS_NO_OVERPAYMENT;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.PAYMENT_NOT_FOUND;
import static greencity.constant.ErrorMessage.REFUND_CONFLICT_MONEY_AND_BONUSES;
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
    private final NotificationService notificationService;
    private final OrderBagService orderBagService;
    private final CertificateRepository certificateRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final RefundRepository refundRepository;
    private final UserRepository userRepository;

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
        Long sumToPayInCoins = PaymentUtil.convertBillsIntoCoins(sumToPay);
        Long paidAmountInCoins = PaymentUtil.calculatePaidAmount(order);
        Long overpaymentInCoins = PaymentUtil.calculateOverpayment(order, sumToPayInCoins);
        Long unPaidAmountInCoins = PaymentUtil.calculateUnpaidAmount(order, sumToPayInCoins, paidAmountInCoins);
        PaymentTableInfoDto paymentTableInfoDto = new PaymentTableInfoDto();
        paymentTableInfoDto.setOverpayment(PaymentUtil.convertCoinsIntoBills(overpaymentInCoins));
        paymentTableInfoDto.setUnPaidAmount(PaymentUtil.convertCoinsIntoBills(unPaidAmountInCoins));
        paymentTableInfoDto.setPaidAmount(PaymentUtil.convertCoinsIntoBills(paidAmountInCoins));
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
    public ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String email) {
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
    @Transactional
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
    public ManualPaymentResponseDto updateManualPayment(Long paymentId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String uuid) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean processRefundForOrder(Order order, RefundDto refundDto,
        String employeeEmail) {
        if (OrderStatus.BROUGHT_IT_HIMSELF == order.getOrderStatus()) {
            processRefundForBroughtItHimselfOrder(order, refundDto, employeeEmail);
            return false;
        } else if (OrderStatus.CANCELED == order.getOrderStatus() || OrderStatus.DONE == order.getOrderStatus()) {
            processRefundForDoneAndCanceledOrder(order, refundDto, employeeEmail);
            return true;
        }
        return false;
    }

    private void processRefundForBroughtItHimselfOrder(Order order, RefundDto refundDto, String employeeEmail) {
        if (isRefundDtoValid(refundDto)) {
            convertRefundAmountIntoCoins(refundDto);
            if (refundDto.isReturnBonuses()) {
                Long possibleBonusesRefundAmount =
                    Long.valueOf(order.getPointsToUse()) + PaymentUtil.calculatePaidAmount(order);
                validateRefundAmount(refundDto.getAmount(), possibleBonusesRefundAmount);
                refundPaymentsInBonus(order, employeeEmail, refundDto.getAmount());
            } else if (refundDto.isReturnMoney()) {
                validateRefundAmount(refundDto.getAmount(), PaymentUtil.calculatePaidAmount(order));
                refundPaymentsInMoney(order, employeeEmail, refundDto.getAmount());
            }
        }
    }

    private void processRefundForDoneAndCanceledOrder(Order order, RefundDto refundDto, String employeeEmail) {
        if (!isRefundDtoValid(refundDto)) {
            throw new BadRequestException(String.format(ORDER_CAN_NOT_BE_UPDATED, order.getOrderStatus()));
        }
        if (refundDto.isReturnBonuses()) {
            refundPaymentsInBonus(order, employeeEmail);
        } else if (refundDto.isReturnMoney()) {
            Long paidAmount = PaymentUtil.calculatePaidAmount(order);
            refundPaymentsInMoney(order, employeeEmail, paidAmount);
        } else {
            throw new BadRequestException(String.format(ORDER_CAN_NOT_BE_UPDATED, order.getOrderStatus()));
        }
    }

    private boolean isRefundDtoValid(RefundDto refundDto) {
        if (refundDto == null) {
            return false;
        }
        if (refundDto.isReturnBonuses() && refundDto.isReturnMoney()) {
            throw new BadRequestException(REFUND_CONFLICT_MONEY_AND_BONUSES);
        }
        return true;
    }

    private void refundPaymentsInMoney(Order order, String employeeEmail, Long amount) {
        if (OrderStatus.CANCELED != order.getOrderStatus()
            && OrderStatus.BROUGHT_IT_HIMSELF != order.getOrderStatus()) {
            throw new BadRequestException(INCOMPATIBLE_ORDER_STATUS_FOR_MONEY_REFUND);
        }
        try {
            checkOverpayment(amount);
        } catch (BadRequestException e) {
            if (OrderStatus.BROUGHT_IT_HIMSELF == order.getOrderStatus()) {
                throw new BadRequestException(INVALID_REQUESTED_REFUND_AMOUNT);
            } else {
                throw e;
            }
        }
        order.getPayment().add(
            buildPaymentForRefund(-amount, PAYMENT_REFUND_ENG, order));
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_REFUNDED);
        try {
            refundRepository.save(Refund.builder()
                .order(order)
                .amount(amount)
                .date(LocalDateTime.now(ZoneId.of("Europe/Kiev")))
                .build());
        } catch (RuntimeException e) {
            throw new BadRequestException(CANNOT_REFUND_MONEY);
        }
        orderRepository.save(order);
        eventService.saveEvent(OrderHistory.CANCELED_ORDER_MONEY_REFUND, employeeEmail, order);
    }

    private void refundPaymentsInBonus(Order order, String email) {
        CounterOrderDetailsDto prices =
            PaymentUtil.getPriceDetails(order.getId(), orderRepository, orderBagService, certificateRepository);
        Long overpaymentInCoins =
            PaymentUtil.calculateOverpayment(order,
                PaymentUtil.convertBillsIntoCoins(PaymentUtil.setTotalPrice(prices)));
        refundPaymentsInBonus(order, email, overpaymentInCoins);
    }

    private void refundPaymentsInBonus(Order order, String email, Long amount) {
        checkOverpayment(amount);
        User currentUser = order.getUser();
        order.getPayment().add(
            buildPaymentForRefund(-amount, ENROLLMENT_TO_THE_BONUS_ACCOUNT_ENG, order));
        transferPointsToUser(order, currentUser, amount);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_REFUNDED);
        orderRepository.save(order);
        userRepository.save(currentUser);
        eventService.saveEvent(OrderHistory.ADDED_BONUSES, email, order);
    }

    private void checkOverpayment(long overpayment) {
        if (overpayment <= 0) {
            throw new BadRequestException(ORDER_HAS_NO_OVERPAYMENT);
        }
    }

    private void validateRefundAmount(Long refundAmount, Long limit) {
        if (refundAmount > limit) {
            throw new BadRequestException(INVALID_REQUESTED_REFUND_AMOUNT);
        }
    }

    private void convertRefundAmountIntoCoins(RefundDto refundDto) {
        if (refundDto != null && refundDto.getAmount() != null) {
            refundDto.setAmount(refundDto.getAmount() * 100);
        }
    }

    private Payment buildPaymentForRefund(Long amount, String receiptLink, Order order) {
        return Payment.builder()
            .amount(amount)
            .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .receiptLink(receiptLink)
            .order(order)
            .currency("UAH")
            .orderStatus(OrderStatus.FORMED)
            .paymentStatus(PaymentStatus.PAID)
            .build();
    }

    private void transferPointsToUser(Order order, User user, long pointsInCoins) {
        int uahPoints = PaymentUtil.convertCoinsIntoBills(pointsInCoins).intValue();
        user.setCurrentPoints(user.getCurrentPoints() + uahPoints);

        user.setChangeOfPointsList(ListUtils.defaultIfNull(user.getChangeOfPointsList(), new ArrayList<>()));
        user.getChangeOfPointsList()
            .add(ChangeOfPoints.builder()
                .user(user)
                .amount(uahPoints)
                .date(LocalDateTime.now())
                .order(order)
                .build());
        notificationService.notifyBonuses(order, (long) uahPoints);
    }

    private void updateOrderPaymentStatusForManualPayment(Order order) {
        CounterOrderDetailsDto dto =
            PaymentUtil.getPriceDetails(order.getId(), orderRepository, orderBagService, certificateRepository);
        double paymentsForCurrentOrder = order.getPayment().stream().filter(payment -> payment.getPaymentStatus()
            .equals(PaymentStatus.PAID)).map(Payment::getAmount).map(PaymentUtil::convertCoinsIntoBills)
            .reduce(Double::sum)
            .orElse((double) 0);
        double totalPaidAmount = paymentsForCurrentOrder + dto.getCertificateBonus() + dto.getBonus();
        double totalAmount = PaymentUtil.setTotalPrice(dto);

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

    private void checkAvailableOrderForEmployee(Order order, String email) {
        Long employeeId = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        Optional<TariffsInfo> tariffsInfoOptional = tariffsInfoRepository.findTariffsInfoByIdForEmployee(
            order.getTariffsInfo().getId(), employeeId);
        if (tariffsInfoOptional.isEmpty()) {
            throw new BadRequestException(ErrorMessage.CANNOT_ACCESS_ORDER_FOR_EMPLOYEE + order.getId());
        }
    }

    private Payment changePaymentEntity(Payment updatePayment,
        ManualPaymentRequestDto requestDto,
        MultipartFile image) {
        updatePayment.setSettlementDate(requestDto.getSettlementdate());
        updatePayment.setAmount(requestDto.getAmount());
        updatePayment.setPaymentId(requestDto.getPaymentId());
        updatePayment.setReceiptLink(requestDto.getReceiptLink());
        if (requestDto.getImagePath().isEmpty()) {
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

    private ManualPaymentResponseDto buildPaymentResponseDto(Payment payment) {
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

    private Payment buildPaymentEntity(Order order, ManualPaymentRequestDto paymentRequestDto, MultipartFile image,
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
            .orderStatus(order.getOrderStatus())
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
}
