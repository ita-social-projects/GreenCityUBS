package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.repository.*;
import greencity.service.locations.LocationApiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentUtil {

    private final ModelMapper modelMapper;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final EventService eventService;
    private final FileService fileService;
    private final OrderRepository orderRepository;
    private final UBSManagementServiceImpl ubsManagementServiceImpl;
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


    Long convertBillsIntoCoins(Double bills) {
        return bills == null ? 0
                : BigDecimal.valueOf(bills)
                .movePointRight(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(AppConstant.NO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
                .longValue();
    }

    Double convertCoinsIntoBills(Long coins) {
        return coins == null ? 0
                : BigDecimal.valueOf(coins)
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
                .doubleValue();
    }

    Double setTotalPrice(CounterOrderDetailsDto dto) {
        if (isContainsExportedBags(dto)) {
            return dto.getSumExported();
        }
        if (isContainsConfirmedBags(dto)) {
            return dto.getSumConfirmed();
        }
        return dto.getSumAmount();
    }
    Boolean isContainsConfirmedBags(CounterOrderDetailsDto dto) {
        return dto.getSumConfirmed() != 0;
    }

    Boolean isContainsExportedBags(CounterOrderDetailsDto dto) {
        return dto.getSumExported() != 0;
    }

}
