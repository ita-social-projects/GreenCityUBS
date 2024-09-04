package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.ManualPaymentResponseDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.PaymentType;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final NotificationService notificationService;
    private final OrderBagService orderBagService;
    private final CertificateRepository certificateRepository;
    private final TariffsInfoRepository tariffsInfoRepository;

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
}
