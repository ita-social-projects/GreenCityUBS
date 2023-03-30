package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.address.AddressExportDetailsDto;
import greencity.dto.bag.AdditionalBagInfoDto;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.bag.BagMappingDto;
import greencity.dto.bag.BagTransDto;
import greencity.dto.bag.ReasonNotTakeBagDto;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.courier.CourierInfoDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.employee.EmployeeNameIdDto;
import greencity.dto.employee.EmployeePositionDtoRequest;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.order.DetailsOrderInfoDto;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.ExportDetailsDtoUpdate;
import greencity.dto.order.GeneralOrderInfo;
import greencity.dto.order.NotTakenOrderReasonDto;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailDto;
import greencity.dto.order.OrderDetailInfoDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderDetailStatusRequestDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.OrderPaymentStatusesTranslationDto;
import greencity.dto.order.OrderStatusPageDto;
import greencity.dto.order.OrderStatusesTranslationDto;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.order.UpdateOrderPageAdminDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.ManualPaymentResponseDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.dto.position.PositionDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.violation.ViolationsInfoDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import greencity.enums.CancellationReason;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.PaymentType;
import greencity.enums.SortingOrder;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.EmployeeOrderPositionRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderDetailRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.ServiceRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.notification.NotificationServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static greencity.constant.ErrorMessage.INCORRECT_ECO_NUMBER;
import static greencity.constant.ErrorMessage.NOT_FOUND_ADDRESS_BY_ORDER_ID;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.PAYMENT_NOT_FOUND;
import static greencity.constant.ErrorMessage.RECEIVING_STATION_NOT_FOUND;
import static greencity.constant.ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.USER_HAS_NO_OVERPAYMENT;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
@Slf4j
public class UBSManagementServiceImpl implements UBSManagementService {
    private final TariffsInfoRepository tariffsInfoRepository;
    private final OrderRepository orderRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final UserRemoteClient userRemoteClient;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BagRepository bagRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final NotificationServiceImpl notificationService;
    private final FileService fileService;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private final EventService eventService;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final ServiceRepository serviceRepository;
    private final OrdersAdminsPageService ordersAdminsPageService;

    private static final String DEFAULT_IMAGE_PATH = AppConstant.DEFAULT_IMAGE;

    private final Set<OrderStatus> orderStatusesBeforeShipment =
        EnumSet.of(OrderStatus.FORMED, OrderStatus.CONFIRMED, OrderStatus.ADJUSTMENT);
    private final Set<OrderStatus> orderStatusesAfterConfirmation =
        EnumSet.of(OrderStatus.ON_THE_ROUTE, OrderStatus.DONE, OrderStatus.BROUGHT_IT_HIMSELF, OrderStatus.CANCELED);
    private static final String FORMAT_DATE = "dd-MM-yyyy";
    @Lazy
    @Autowired
    private UBSClientService ubsClientService;

    /**
     * Method gets all order payments, count paid amount, amount which user should
     * paid and overpayment amount.
     *
     * @param orderId  of {@link Long} order id;
     * @param sumToPay of {@link Long} sum to pay;
     * @return {@link PaymentTableInfoDto }
     * @author Nazar Struk, Ostap Mykhailivskyi
     */
    @Override
    public PaymentTableInfoDto getPaymentInfo(long orderId, Long sumToPay) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Long paidAmount = calculatePaidAmount(order);
        Long overpayment = calculateOverpayment(order, sumToPay);
        Long unPaidAmount = calculateUnpaidAmount(order, sumToPay, paidAmount);
        PaymentTableInfoDto paymentTableInfoDto = new PaymentTableInfoDto();
        paymentTableInfoDto.setOverpayment(overpayment);
        paymentTableInfoDto.setUnPaidAmount(unPaidAmount);
        paymentTableInfoDto.setPaidAmount(paidAmount);
        List<PaymentInfoDto> paymentInfoDtos = order.getPayment().stream()
            .filter(payment -> payment.getPaymentStatus().equals(PaymentStatus.PAID))
            .map(x -> modelMapper.map(x, PaymentInfoDto.class)).collect(Collectors.toList());
        paymentTableInfoDto.setPaymentInfoDtos(getAmountInUAH(paymentInfoDtos));
        return paymentTableInfoDto;
    }

    private List<PaymentInfoDto> getAmountInUAH(List<PaymentInfoDto> paymentInfoDtos) {
        if (!paymentInfoDtos.isEmpty()) {
            for (PaymentInfoDto paymentInfoDto : paymentInfoDtos) {
                if (paymentInfoDto != null) {
                    Long coins = paymentInfoDto.getAmount() / 100;
                    paymentInfoDto.setAmount(coins);
                }
            }
        }
        return paymentInfoDtos;
    }

    /**
     * Method return's information about overpayment and used bonuses on canceled
     * and done orders.
     *
     * @param orderId  of {@link Long} order id;
     * @param sumToPay of {@link Long} sum to pay;
     * @param marker   of {@link Long} marker;
     * @return {@link PaymentTableInfoDto }
     * @author Ostap Mykhailivskyi
     */
    @Override
    public PaymentTableInfoDto returnOverpaymentInfo(Long orderId, Long sumToPay, Long marker) {
        Order order = orderRepository.findUserById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Long overpayment = calculateOverpayment(order, sumToPay);
        PaymentTableInfoDto dto = getPaymentInfo(orderId, sumToPay);
        PaymentInfoDto payDto = PaymentInfoDto.builder().amount(overpayment)
            .settlementdate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).build();
        if (marker == 1L) {
            payDto.setComment(AppConstant.PAYMENT_REFUND);
        } else {
            payDto.setComment(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT);
            User user = order.getUser();
            user.setCurrentPoints(user.getCurrentPoints() + Integer.parseInt(overpayment.toString()));
            orderRepository.save(order);
        }
        dto.getPaymentInfoDtos().add(payDto);
        dto.setOverpayment(dto.getOverpayment() - overpayment);
        return dto;
    }

    @Override
    public PageableDto<CertificateDtoForSearching> getAllCertificates(Pageable page, String columnName,
        SortingOrder sortingOrder) {
        PageRequest pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(),
            Sort.by(Sort.Direction.fromString(sortingOrder.toString()), columnName));
        Page<Certificate> certificates = certificateRepository.findAll(pageRequest);
        return getAllCertificatesTranslationDto(certificates);
    }

    @Override
    public ViolationsInfoDto getAllUserViolations(String email) {
        String uuidId = userRemoteClient.findUuidByEmail(email);
        User user = userRepository.findUserByUuid(uuidId).orElseThrow(() -> new NotFoundException(
            USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        return modelMapper.map(user, ViolationsInfoDto.class);
    }

    private PageableDto<CertificateDtoForSearching> getAllCertificatesTranslationDto(Page<Certificate> pages) {
        List<CertificateDtoForSearching> certificateForSearchingDTOS = pages
            .stream()
            .map(
                certificatesTranslations -> modelMapper.map(certificatesTranslations, CertificateDtoForSearching.class))
            .collect(Collectors.toList());
        return new PageableDto<>(
            certificateForSearchingDTOS,
            pages.getTotalElements(),
            pages.getPageable().getPageNumber(),
            pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPointsToUser(AddingPointsToUserDto addingPointsToUserDto) {
        String ourUUid = userRemoteClient.findUuidByEmail(addingPointsToUserDto.getEmail());
        User ourUser = userRepository.findUserByUuid(ourUUid).orElseThrow(() -> new NotFoundException(
            USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        if (ourUser.getCurrentPoints() == null) {
            ourUser.setCurrentPoints(0);
        }
        ourUser.setCurrentPoints(ourUser.getCurrentPoints() + addingPointsToUserDto.getAdditionalPoints());
        ChangeOfPoints changeOfPoints = ChangeOfPoints.builder()
            .amount(addingPointsToUserDto.getAdditionalPoints())
            .date(LocalDateTime.now())
            .user(ourUser)
            .build();
        ourUser.getChangeOfPointsList().add(changeOfPoints);
        userRepository.save(ourUser);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public ReadAddressByOrderDto getAddressByOrderId(Long orderId) {
        if (orderRepository.findById(orderId).isEmpty()) {
            throw new NotFoundException(NOT_FOUND_ADDRESS_BY_ORDER_ID + orderId);
        }
        return modelMapper.map(orderAddressRepository.getOrderAddressByOrderId(orderId), ReadAddressByOrderDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<OrderAddressDtoResponse> updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Long orderId,
        String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Optional<OrderAddress> addressForAdminPage = orderAddressRepository.findById(dtoUpdate.getAddressId());
        if (addressForAdminPage.isPresent()) {
            orderAddressRepository.save(updateAddressOrderInfo(addressForAdminPage.get(), dtoUpdate));
            eventService.saveEvent(OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE, email, order);
            return addressForAdminPage.map(value -> modelMapper.map(value, OrderAddressDtoResponse.class));
        } else {
            throw new NotFoundException(NOT_FOUND_ADDRESS_BY_ORDER_ID + dtoUpdate.getAddressId());
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<OrderInfoDto> getOrdersForUser(String uuid) {
        List<Order> orders = orderRepository.getAllOrdersOfUser(uuid);
        List<OrderInfoDto> dto = new ArrayList<>();
        orders.forEach(order -> dto.add(modelMapper.map(order, OrderInfoDto.class)));
        dto.forEach(data -> data.setOrderPrice(getPriceDetails(data.getId()).getTotalSumAmount()));
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderStatusPageDto getOrderStatusData(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        checkAvailableOrderForEmployee(order, email);
        CounterOrderDetailsDto prices = getPriceDetails(orderId);

        var bagInfoDtoList = bagRepository.findBagsByTariffsInfoId(order.getTariffsInfo().getId()).stream()
            .map(bag -> modelMapper.map(bag, BagInfoDto.class))
            .collect(Collectors.toList());

        Integer servicePrice = serviceRepository.findServiceByTariffsInfoId(order.getTariffsInfo().getId())
            .map(it -> it.getPrice())
            .orElse(0);

        var orderAddress = order.getUbsUser().getOrderAddress();

        UserInfoDto userInfoDto =
            ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(orderId, order.getUser().getUuid());
        GeneralOrderInfo infoAboutStatusesAndDateFormed =
            getInfoAboutStatusesAndDateFormed(Optional.of(order));
        AddressExportDetailsDto addressDtoForAdminPage = getAddressDtoForAdminPage(orderAddress);
        return OrderStatusPageDto.builder()
            .generalOrderInfo(infoAboutStatusesAndDateFormed)
            .userInfoDto(userInfoDto)
            .addressExportDetailsDto(addressDtoForAdminPage)
            .addressComment(orderAddress.getAddressComment())
            .bags(bagInfoDtoList)
            .orderFullPrice(setTotalPrice(prices))
            .orderDiscountedPrice(getPaymentInfo(orderId, prices.getSumAmount().longValue()).getUnPaidAmount())
            .orderBonusDiscount(prices.getBonus()).orderCertificateTotalDiscount(prices.getCertificateBonus())
            .orderExportedPrice(prices.getSumExported()).orderExportedDiscountedPrice(prices.getTotalSumExported())
            .amountOfBagsOrdered(order.getAmountOfBagsOrdered())
            .amountOfBagsExported(order.getExportedQuantity())
            .amountOfBagsConfirmed(order.getConfirmedQuantity())
            .numbersFromShop(order.getAdditionalOrders())
            .certificates(prices.getCertificate())
            .paymentTableInfoDto(getPaymentInfo(orderId, setTotalPrice(prices).longValue()))
            .exportDetailsDto(getOrderExportDetails(orderId))
            .employeePositionDtoRequest(getAllEmployeesByPosition(orderId, email))
            .comment(order.getComment())
            .courierPricePerPackage(servicePrice)
            .courierInfo(modelMapper.map(order.getTariffsInfo(), CourierInfoDto.class))
            .build();
    }

    private Double setTotalPrice(CounterOrderDetailsDto dto) {
        if (isContainsExportedBags(dto)) {
            return dto.getSumExported();
        }
        if (isContainsConfirmedBags(dto)) {
            return dto.getSumConfirmed();
        }
        return dto.getSumAmount();
    }

    private Boolean isContainsConfirmedBags(CounterOrderDetailsDto dto) {
        return dto.getSumConfirmed() != 0 && dto.getSumExported() == 0;
    }

    private Boolean isContainsExportedBags(CounterOrderDetailsDto dto) {
        return dto.getSumExported() != 0;
    }

    /**
     * This is private method which formed {@link AddressExportDetailsDto} in order
     * to collect information about Address and order formed data and order id.
     * 
     * @param address {@link Address}.
     * @return {@link AddressExportDetailsDto}.
     *
     * @author Yuriy Bahlay.
     */
    private AddressExportDetailsDto getAddressDtoForAdminPage(OrderAddress address) {
        return AddressExportDetailsDto.builder()
            .addressId(address.getId())
            .addressCity(address.getCity())
            .addressCityEng(address.getCityEn())
            .addressStreet(address.getStreet())
            .addressStreetEng(address.getStreetEn())
            .addressDistrict(address.getDistrict())
            .addressDistrictEng(address.getDistrictEn())
            .addressEntranceNumber(address.getEntranceNumber())
            .addressHouseCorpus(address.getHouseCorpus())
            .addressHouseNumber(address.getHouseNumber())
            .addressRegion(address.getRegion())
            .addressRegionEng(address.getRegionEn())
            .build();
    }

    /**
     * This is private method which is form {@link GeneralOrderInfo} and set info
     * about order data formed and about current order status and order payment
     * status with some translation with some language and arrays with orderStatuses
     * and OrderPaymentStatuses with translation.
     * 
     * @param order {@link Order}.
     * @return {@link GeneralOrderInfo}.
     *
     * @author Yuriy Bahlay.
     */
    private GeneralOrderInfo getInfoAboutStatusesAndDateFormed(Optional<Order> order) {
        OrderStatus orderStatus = order.isPresent() ? order.get().getOrderStatus() : OrderStatus.CANCELED;
        Optional<OrderStatusTranslation> orderStatusTranslation =
            orderStatusTranslationRepository.getOrderStatusTranslationById((long) orderStatus.getNumValue());
        String currentOrderStatusTranslation =
            orderStatusTranslation.isPresent() ? orderStatusTranslation.get().getName() : orderStatus.name();
        String currentOrderStatusTranslationEng =
            orderStatusTranslation.isPresent() ? orderStatusTranslation.get().getNameEng()
                : orderStatus.name();

        OrderPaymentStatus orderStatusPayment =
            order.map(Order::getOrderPaymentStatus).orElse(OrderPaymentStatus.UNPAID);
        Order currentOrder = order.orElseGet(Order::new);
        OrderPaymentStatusTranslation currentOrderStatusPaymentTranslation = orderPaymentStatusTranslationRepository
            .getById((long) orderStatusPayment.getStatusValue());

        return GeneralOrderInfo.builder()
            .id(order.isPresent() ? order.get().getId() : 0)
            .dateFormed(order.map(Order::getOrderDate).orElse(null))
            .orderStatusesDtos(getOrderStatusesTranslation())
            .orderPaymentStatusesDto(getOrderPaymentStatusesTranslation())
            .orderStatus(order.map(Order::getOrderStatus).orElse(null))
            .orderPaymentStatus(order.map(Order::getOrderPaymentStatus).orElse(null))
            .orderPaymentStatusName(currentOrderStatusPaymentTranslation.getTranslationValue())
            .orderPaymentStatusNameEng(currentOrderStatusPaymentTranslation.getTranslationsValueEng())
            .orderStatusName(currentOrderStatusTranslation)
            .orderStatusNameEng(currentOrderStatusTranslationEng)
            .adminComment(currentOrder.getAdminComment())
            .build();
    }

    /**
     * This is method which is get order translation statuses in two languages like:
     * ua and en.
     *
     * @return {@link List}.
     *
     * @author Yuriy Bahlay.
     */
    private List<OrderStatusesTranslationDto> getOrderStatusesTranslation() {
        List<OrderStatusesTranslationDto> orderStatusesTranslationDtos = new ArrayList<>();
        List<OrderStatusTranslation> orderStatusTranslations =
            orderStatusTranslationRepository.findAllBy();
        if (!orderStatusTranslations.isEmpty()) {
            for (OrderStatusTranslation orderStatusTranslation : orderStatusTranslations) {
                OrderStatusesTranslationDto orderStatusesTranslationDto = new OrderStatusesTranslationDto();
                setValueForOrderStatusIsCancelledOrDoneAsTrue(orderStatusTranslation, orderStatusesTranslationDto);
                orderStatusesTranslationDto.setUa(orderStatusTranslation.getName());
                orderStatusesTranslationDto.setEng(orderStatusTranslation.getNameEng());
                if (!Objects.equals(OrderStatus.getConvertedEnumFromLongToEnum(orderStatusTranslation.getStatusId()),
                    "")) {
                    OrderStatus.getConvertedEnumFromLongToEnum(orderStatusTranslation.getStatusId());
                    orderStatusesTranslationDto
                        .setKey(OrderStatus.getConvertedEnumFromLongToEnum(orderStatusTranslation.getStatusId()));
                }
                orderStatusesTranslationDtos.add(orderStatusesTranslationDto);
            }
        }
        return orderStatusesTranslationDtos;
    }

    /**
     * This is method which set value as true for orderStatus Cancelled or Done.
     *
     * @param orderStatusTranslation      {@link OrderStatusTranslation}.
     * @param orderStatusesTranslationDto {@link OrderStatusesTranslationDto}.
     *
     * @author Yuriy Bahlay.
     */
    private void setValueForOrderStatusIsCancelledOrDoneAsTrue(OrderStatusTranslation orderStatusTranslation,
        OrderStatusesTranslationDto orderStatusesTranslationDto) {
        orderStatusesTranslationDto
            .setAbleActualChange(OrderStatus.CANCELED.getNumValue() == orderStatusTranslation.getStatusId()
                || OrderStatus.DONE.getNumValue() == orderStatusTranslation.getStatusId());
    }

    /**
     * This is method which is get order payment statuses translation.
     *
     * @return {@link List}.
     *
     * @author Yuriy Bahlay.
     */
    private List<OrderPaymentStatusesTranslationDto> getOrderPaymentStatusesTranslation() {
        List<OrderPaymentStatusesTranslationDto> orderStatusesTranslationDtos = new ArrayList<>();
        List<OrderPaymentStatusTranslation> orderStatusPaymentTranslations = orderPaymentStatusTranslationRepository
            .getAllBy();
        if (!orderStatusPaymentTranslations.isEmpty()) {
            for (OrderPaymentStatusTranslation orderStatusPaymentTranslation : orderStatusPaymentTranslations) {
                OrderPaymentStatusesTranslationDto translationDto = new OrderPaymentStatusesTranslationDto();
                translationDto.setUa(orderStatusPaymentTranslation.getTranslationValue());
                translationDto.setEng(orderStatusPaymentTranslation.getTranslationsValueEng());
                if (!Objects.equals(OrderPaymentStatus.getConvertedEnumFromLongToEnumAboutOrderPaymentStatus(
                    orderStatusPaymentTranslation.getOrderPaymentStatusId()), "")) {
                    translationDto.setKey(OrderPaymentStatus.getConvertedEnumFromLongToEnumAboutOrderPaymentStatus(
                        orderStatusPaymentTranslation.getOrderPaymentStatusId()));
                }
                orderStatusesTranslationDtos.add(translationDto);
            }
        }
        return orderStatusesTranslationDtos;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<OrderDetailInfoDto> getOrderDetails(Long orderId, String language) {
        OrderDetailDto dto = new OrderDetailDto();
        Order order = orderRepository.getOrderDetails(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        setOrderDetailDto(dto, order);
        return modelMapper.map(dto, new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public void setOrderDetail(Long orderId,
        Map<Integer, Integer> confirmed, Map<Integer, Integer> exported, String email) {
        final long wasPaid =
            paymentRepository.selectSumPaid(orderId) == null ? 0L
                : paymentRepository.selectSumPaid(orderId) / 100;
        collectEventsAboutSetOrderDetails(confirmed, exported, orderId, email);
        final Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));

        if (nonNull(confirmed)) {
            for (Map.Entry<Integer, Integer> entry : confirmed.entrySet()) {
                if (Boolean.TRUE
                    .equals(orderDetailRepository.ifRecordExist(orderId, entry.getKey().longValue()) <= 0)) {
                    orderDetailRepository.insertNewRecord(orderId, entry.getKey().longValue());
                    orderDetailRepository.updateAmount(0, orderId, entry.getKey().longValue());
                }
                orderDetailRepository
                    .updateConfirm(entry.getValue(), orderId,
                        entry.getKey().longValue());
            }
        }

        if (nonNull(exported)) {
            for (Map.Entry<Integer, Integer> entry : exported.entrySet()) {
                if (Boolean.TRUE
                    .equals(orderDetailRepository.ifRecordExist(orderId, entry.getKey().longValue()) <= 0)) {
                    orderDetailRepository.insertNewRecord(orderId, entry.getKey().longValue());
                    orderDetailRepository.updateAmount(0, orderId, entry.getKey().longValue());
                }
                orderDetailRepository
                    .updateExporter(entry.getValue(), orderId,
                        entry.getKey().longValue());
            }
        }

        long discount = orderRepository.findSumOfCertificatesByOrderId(orderId);
        long totalPrice = setTotalPrice(getPriceDetails(orderId)).longValue();
        long needToPay = totalPrice - wasPaid - discount;

        if (needToPay == 0) {
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.PAID.name());
            return;
        }
        if (totalPrice - wasPaid >= 0 && needToPay < 0) {
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.PAID.name());
            recalculateCertificates(totalPrice - wasPaid, order);
            return;
        }
        if (totalPrice < wasPaid) {
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.PAID.name());
            recalculateCertificates(0L, order);
            return;
        }
        if (needToPay > 0 && wasPaid + discount != 0) {
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.HALF_PAID.name());
            notificationService.notifyHalfPaidPackage(order);
            return;
        }
        if (wasPaid + discount == 0) {
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.UNPAID.name());
        }
    }

    private void recalculateCertificates(long amount, Order order) {
        Set<Certificate> certificates = order.getCertificates();
        for (Certificate certificate : certificates) {
            amount = amount - certificate.getPoints();
            if (amount < 0) {
                certificate.setPoints(certificate.getPoints() + (int) amount);
                certificateRepository.save(certificate);
                amount = 0L;
            }
        }
        recalculatePoints(amount, order);
    }

    private void recalculatePoints(long amount, Order order) {
        if (order.getPointsToUse() > amount) {
            userRepository.updateUserCurrentPoints(order.getUser().getId(), order.getPointsToUse() - (int) amount);
            order.setPointsToUse((int) amount);
            orderRepository.updateOrderPointsToUse(order.getId(), (int) amount);
        }
    }

    private void collectEventsAboutSetOrderDetails(Map<Integer, Integer> confirmed, Map<Integer, Integer> exported,
        Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));

        StringBuilder values = new StringBuilder();
        int countOfChanges = 0;
        if (nonNull(exported)) {
            collectEventAboutExportedWaste(exported, order, orderId, countOfChanges, values);
        }
        if (nonNull(confirmed)) {
            collectEventAboutConfirmWaste(confirmed, order, orderId, countOfChanges, values);
        }
        if (nonNull(confirmed) || nonNull(exported)) {
            eventService.saveEvent(values.toString(), email, order);
        }
    }

    private void collectEventAboutConfirmWaste(Map<Integer, Integer> confirmed, Order order,
        Long orderId, int countOfChanges, StringBuilder values) {
        for (Map.Entry<Integer, Integer> entry : confirmed.entrySet()) {
            Integer capacity = bagRepository.findCapacityById(entry.getKey());
            Optional<Bag> bagOptional = bagRepository.findById(entry.getKey());
            if (bagOptional.isPresent() && checkOrderStatusAboutConfirmWaste(order)) {
                Optional<Long> confirmWasteWas = Optional.empty();
                Optional<Long> initialAmount = Optional.empty();
                Bag bag = bagOptional.get();
                if (Boolean.TRUE.equals(orderDetailRepository.ifRecordExist(orderId, entry.getKey().longValue()) > 0)) {
                    confirmWasteWas =
                        Optional.ofNullable(orderDetailRepository.getConfirmWaste(orderId, entry.getKey().longValue()));
                    initialAmount =
                        Optional.ofNullable(orderDetailRepository.getAmount(orderId, entry.getKey().longValue()));
                }
                if (entry.getValue().longValue() != confirmWasteWas.orElse(0L)) {
                    if (countOfChanges == 0) {
                        values.append(OrderHistory.CHANGE_ORDER_DETAILS + " ");
                    }
                    values.append(bag.getName()).append(" ").append(capacity).append(" л: ")
                        .append(confirmWasteWas.orElse(initialAmount.orElse(0L)))
                        .append(" шт на ").append(entry.getValue()).append(" шт.");
                }
            }
        }
    }

    private void collectEventAboutExportedWaste(Map<Integer, Integer> exported, Order order,
        Long orderId, int countOfChanges, StringBuilder values) {
        for (Map.Entry<Integer, Integer> entry : exported.entrySet()) {
            Integer capacity = bagRepository.findCapacityById(entry.getKey());
            Optional<Bag> bagOptional = bagRepository.findById(entry.getKey());
            if (bagOptional.isPresent() && checkOrderStatusAboutExportedWaste(order)) {
                Optional<Long> exporterWasteWas = Optional.empty();
                Optional<Long> confirmWasteWas = Optional.empty();
                Bag bag = bagOptional.get();
                if (Boolean.TRUE.equals(orderDetailRepository.ifRecordExist(orderId, entry.getKey().longValue()) > 0)) {
                    exporterWasteWas =
                        Optional
                            .ofNullable(orderDetailRepository.getExporterWaste(orderId, entry.getKey().longValue()));
                    confirmWasteWas =
                        Optional.ofNullable(orderDetailRepository.getConfirmWaste(orderId, entry.getKey().longValue()));
                }
                if (entry.getValue().longValue() != exporterWasteWas.orElse(0L)) {
                    if (countOfChanges == 0) {
                        values.append(OrderHistory.CHANGE_ORDER_DETAILS + " ");
                        countOfChanges++;
                    }
                    values.append(bag.getName()).append(" ").append(capacity).append(" л: ")
                        .append(exporterWasteWas.orElse(confirmWasteWas.orElse(0L)))
                        .append(" шт на ").append(entry.getValue()).append(" шт.");
                }
            }
        }
    }

    private boolean checkOrderStatusAboutConfirmWaste(Order order) {
        return order.getOrderStatus() == OrderStatus.ADJUSTMENT
            || order.getOrderStatus() == OrderStatus.CONFIRMED
            || order.getOrderStatus() == OrderStatus.FORMED
            || order.getOrderStatus() == OrderStatus.NOT_TAKEN_OUT
            || order.getOrderStatus() == OrderStatus.ON_THE_ROUTE
            || order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF;
    }

    private boolean checkOrderStatusAboutExportedWaste(Order order) {
        return order.getOrderStatus() == OrderStatus.DONE
            || order.getOrderStatus() == OrderStatus.CANCELED;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public CounterOrderDetailsDto getOrderSumDetails(Long orderId) {
        CounterOrderDetailsDto dto = getPriceDetails(orderId);
        Order order = orderRepository.getOrderDetails(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));

        double totalSumAmount = dto.getTotalSumAmount();
        double totalSumConfirmed = dto.getTotalSumConfirmed();
        double totalSumExported = dto.getTotalSumExported();

        updateOrderPaymentStatus(order, totalSumAmount, totalSumConfirmed, totalSumExported);
        return dto;
    }

    private CounterOrderDetailsDto getPriceDetails(Long id) {
        CounterOrderDetailsDto dto = new CounterOrderDetailsDto();
        Order order = orderRepository.getOrderDetails(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Bag> bag = bagRepository.findBagsByOrderId(id);
        final List<Certificate> currentCertificate = certificateRepository.findCertificate(id);

        double sumAmount = 0;
        double sumConfirmed = 0;
        double sumExported = 0;
        double totalSumAmount;
        double totalSumConfirmed;
        double totalSumExported;
        if (!bag.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : order.getAmountOfBagsOrdered().entrySet()) {
                sumAmount += entry.getValue() * bag
                    .stream()
                    .filter(b -> b.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + entry.getKey()))
                    .getFullPrice();
            }
            for (Map.Entry<Integer, Integer> entry : order.getConfirmedQuantity().entrySet()) {
                sumConfirmed += entry.getValue() * bag
                    .stream()
                    .filter(b -> b.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + entry.getKey()))
                    .getFullPrice();
            }
            for (Map.Entry<Integer, Integer> entry : order.getExportedQuantity().entrySet()) {
                sumExported += entry.getValue() * bag
                    .stream()
                    .filter(b -> b.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + entry.getKey()))
                    .getFullPrice();
            }
        }

        if (!currentCertificate.isEmpty()) {
            double totalSumAmountToCheck =
                (sumAmount - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                    + order.getPointsToUse()));
            totalSumAmount = totalSumAmountToCheck <= 0 ? 0 : totalSumAmountToCheck;
            totalSumConfirmed =
                (sumConfirmed
                    - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                        + order.getPointsToUse()));
            totalSumExported =
                (sumExported - ((currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                    + order.getPointsToUse()));
            dto.setCertificateBonus(
                currentCertificate.stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0).doubleValue());
            dto.setCertificate(
                currentCertificate.stream().map(Certificate::getCode).collect(Collectors.toList()));
        } else {
            dto.setCertificateBonus((double) 0);
            totalSumAmount = sumAmount - order.getPointsToUse();
            totalSumConfirmed = sumConfirmed - order.getPointsToUse();
            totalSumExported = sumExported - order.getPointsToUse();
        }
        if (order.getConfirmedQuantity().isEmpty()) {
            totalSumConfirmed = 0;
        }
        if (order.getExportedQuantity().isEmpty()) {
            totalSumExported = 0;
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
        setDtoInfo(dto, sumAmount, sumExported, sumConfirmed, totalSumAmount, totalSumConfirmed, totalSumExported);
        return dto;
    }

    private void setDtoInfoFromOrder(CounterOrderDetailsDto dto, Order order) {
        dto.setOrderComment(order.getComment());
        dto.setNumberOrderFromShop(order.getAdditionalOrders());
        dto.setBonus(order.getPointsToUse().doubleValue());
    }

    private void setDtoInfo(CounterOrderDetailsDto dto, double sumAmount, double sumExported, double sumConfirmed,
        double totalSumAmount, double totalSumConfirmed, double totalSumExported) {
        dto.setSumAmount(sumAmount);
        dto.setSumConfirmed(sumConfirmed);
        dto.setSumExported(sumExported);
        dto.setTotalSumAmount(totalSumAmount);
        dto.setTotalSumConfirmed(totalSumConfirmed);
        dto.setTotalSumExported(totalSumExported);
    }

    private void updateOrderPaymentStatus(Order currentOrder, double totalSumAmount, double totalConfirmed,
        double totalExported) {
        long paymentsForCurrentOrder = currentOrder.getPayment().stream().filter(payment -> payment.getPaymentStatus()
            .equals(PaymentStatus.PAID)).map(Payment::getAmount).map(amount -> amount / 100).reduce(Long::sum)
            .orElse(0L);

        if (orderStatusesBeforeShipment.contains(currentOrder.getOrderStatus())) {
            setOrderPaymentStatusForConfirmedBags(currentOrder, paymentsForCurrentOrder, totalSumAmount,
                totalConfirmed);
        } else if (orderStatusesAfterConfirmation.contains(currentOrder.getOrderStatus())) {
            setOrderPaymentStatusForExportedBags(currentOrder, paymentsForCurrentOrder, totalExported);
        }
        orderRepository.save(currentOrder);
    }

    private void setOrderPaymentStatusForConfirmedBags(Order currentOrder, long paymentsForCurrentOrder,
        double totalSumAmount, double totalConfirmed) {
        boolean paidCondition = paymentsForCurrentOrder > 0 && paymentsForCurrentOrder >= totalSumAmount
            && paymentsForCurrentOrder >= totalConfirmed;
        boolean halfPaidCondition = paymentsForCurrentOrder > 0 && totalSumAmount > paymentsForCurrentOrder
            || totalConfirmed > paymentsForCurrentOrder;

        if (paidCondition) {
            currentOrder.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            notificationService.notifyPaidOrder(currentOrder);

            if (currentOrder.getOrderStatus() == OrderStatus.ADJUSTMENT) {
                notificationService.notifyCourierItineraryFormed(currentOrder);
            }
        } else if (halfPaidCondition) {
            currentOrder.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
            notificationService.notifyHalfPaidPackage(currentOrder);
        }
    }

    private void setOrderPaymentStatusForExportedBags(Order currentOrder, long paymentsForCurrentOrder,
        double totalExported) {
        boolean halfPaidCondition = paymentsForCurrentOrder > 0 && totalExported > paymentsForCurrentOrder;
        boolean paidCondition = totalExported <= paymentsForCurrentOrder && paymentsForCurrentOrder > 0;

        if (halfPaidCondition) {
            currentOrder.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
            notificationService.notifyHalfPaidPackage(currentOrder);
        } else if (paidCondition) {
            currentOrder.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            notificationService.notifyPaidOrder(currentOrder);
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public OrderDetailStatusDto getOrderDetailStatus(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Payment> payment = paymentRepository.findAllByOrderId(id);
        if (payment.isEmpty()) {
            throw new NotFoundException(PAYMENT_NOT_FOUND + id);
        }
        return buildStatuses(order, payment.get(0));
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public OrderDetailStatusDto updateOrderDetailStatus(Long id, OrderDetailStatusRequestDto dto, String email) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Payment> payment = paymentRepository.findAllByOrderId(id);
        if (payment.isEmpty()) {
            throw new NotFoundException(PAYMENT_NOT_FOUND + id);
        }
        if (nonNull(dto.getAdminComment())) {
            order.setAdminComment(dto.getAdminComment());
            eventService.saveEvent(OrderHistory.ADD_ADMIN_COMMENT, email, order);
            orderRepository.save(order);
        }
        if (nonNull(dto.getOrderStatus())) {
            order.setOrderStatus(OrderStatus.valueOf(dto.getOrderStatus()));
            if (order.getOrderStatus() == OrderStatus.ADJUSTMENT) {
                notificationService.notifyCourierItineraryFormed(order);
                eventService.saveEvent(OrderHistory.ORDER_ADJUSTMENT, email, order);
            } else if (order.getOrderStatus() == OrderStatus.CONFIRMED) {
                eventService.saveEvent(OrderHistory.ORDER_CONFIRMED, email, order);
            } else if (order.getOrderStatus() == OrderStatus.FORMED) {
                eventService.saveEvent(OrderHistory.ORDER_FORMED, email, order);
            } else if (order.getOrderStatus() == OrderStatus.NOT_TAKEN_OUT) {
                eventService.saveEvent(OrderHistory.ORDER_NOT_TAKEN_OUT, email, order);
            } else if (order.getOrderStatus() == OrderStatus.CANCELED) {
                setOrderCancellation(order, dto.getCancellationReason(), dto.getCancellationComment());
                eventService.saveEvent(OrderHistory.ORDER_CANCELLED, email, order);
            } else if (order.getOrderStatus() == OrderStatus.DONE) {
                eventService.saveEvent(OrderHistory.ORDER_DONE, email, order);
            } else if (order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF) {
                eventService.saveEvent(OrderHistory.ORDER_BROUGHT_IT_HIMSELF, email, order);
            } else if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE) {
                eventService.saveEvent(OrderHistory.ORDER_ON_THE_ROUTE, email, order);
            }
            orderRepository.save(order);
        }
        if (nonNull(dto.getOrderPaymentStatus())) {
            paymentRepository.findAllByOrderId(id)
                .forEach(x -> x.setPaymentStatus(PaymentStatus.valueOf(dto.getOrderPaymentStatus())));
            paymentRepository.saveAll(payment);
        }

        return buildStatuses(order, payment.get(0));
    }

    private void setOrderCancellation(Order order, String cancellationReason, String cancellationComment) {
        if (order.getPointsToUse() != 0 || !order.getCertificates().isEmpty()) {
            notificationService.notifyBonusesFromCanceledOrder(order);
            returnAllPointsFromOrder(order);
        }
        order.setCancellationComment(cancellationComment);
        order.setCancellationReason(CancellationReason.valueOf(cancellationReason));
    }

    private OrderDetailStatusDto buildStatuses(Order order, Payment payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
        String orderDate = order.getOrderDate().toLocalDate().format(formatter);
        return OrderDetailStatusDto.builder()
            .orderStatus(order.getOrderStatus().name())
            .paymentStatus(payment.getPaymentStatus().name())
            .date(orderDate)
            .build();
    }

    private void setOrderDetailDto(OrderDetailDto dto, Order order) {
        dto.setAmount(modelMapper.map(order, new TypeToken<List<BagMappingDto>>() {
        }.getType()));

        dto.setCapacityAndPrice(bagRepository.findBagsByOrderId(order.getId())
            .stream()
            .map(b -> modelMapper.map(b, BagInfoDto.class))
            .collect(Collectors.toList()));

        dto.setName(bagRepository.findAllByOrder(order.getId())
            .stream()
            .map(b -> modelMapper.map(b, BagTransDto.class))
            .collect(Collectors.toList()));

        dto.setOrderId(order.getId());
    }

    private OrderAddress updateAddressOrderInfo(OrderAddress address, OrderAddressExportDetailsDtoUpdate dto) {
        Optional.ofNullable(dto.getAddressCity()).ifPresent(address::setCity);
        Optional.ofNullable(dto.getAddressCityEng()).ifPresent(address::setCityEn);
        Optional.ofNullable(dto.getAddressRegion()).ifPresent(address::setRegion);
        Optional.ofNullable(dto.getAddressRegionEng()).ifPresent(address::setRegionEn);
        Optional.ofNullable(dto.getAddressDistrict()).ifPresent(address::setDistrict);
        Optional.ofNullable(dto.getAddressDistrictEng()).ifPresent(address::setDistrictEn);
        Optional.ofNullable(dto.getAddressStreet()).ifPresent(address::setStreet);
        Optional.ofNullable(dto.getAddressStreetEng()).ifPresent(address::setStreetEn);
        Optional.ofNullable(dto.getAddressHouseNumber()).ifPresent(address::setHouseNumber);
        Optional.ofNullable(dto.getAddressHouseCorpus()).ifPresent(address::setHouseCorpus);
        Optional.ofNullable(dto.getAddressEntranceNumber()).ifPresent(address::setEntranceNumber);

        return address;
    }

    private void returnAllPointsFromOrder(Order order) {
        Integer pointsToReturn = order.getPointsToUse();
        if (isNull(pointsToReturn) || pointsToReturn == 0) {
            return;
        }
        order.setPointsToUse(0);
        User user = order.getUser();
        if (isNull(user.getCurrentPoints())) {
            user.setCurrentPoints(0);
        }
        user.setCurrentPoints(user.getCurrentPoints() + pointsToReturn);
        ChangeOfPoints changeOfPoints = ChangeOfPoints.builder()
            .amount(pointsToReturn)
            .date(LocalDateTime.now())
            .user(user)
            .order(order)
            .build();
        if (isNull(user.getChangeOfPointsList())) {
            user.setChangeOfPointsList(new ArrayList<>());
        }
        user.getChangeOfPointsList().add(changeOfPoints);
        userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<DetailsOrderInfoDto> getOrderBagsDetails(Long orderId) {
        List<DetailsOrderInfoDto> detailsOrderInfoDtos = new ArrayList<>();
        List<Map<String, Object>> ourResult = bagRepository.getBagInfo(orderId);
        for (Map<String, Object> array : ourResult) {
            DetailsOrderInfoDto dto = objectMapper.convertValue(array, DetailsOrderInfoDto.class);
            detailsOrderInfoDtos.add(dto);
        }
        return detailsOrderInfoDtos;
    }

    /**
     * Method returns export details by order id.
     *
     * @param id of {@link Long} order id;
     * @return {@link ExportDetailsDto};
     * @author Orest Mahdziak
     */
    @Override
    public ExportDetailsDto getOrderExportDetails(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<ReceivingStation> receivingStation = receivingStationRepository.findAll();
        if (receivingStation.isEmpty()) {
            throw new NotFoundException(RECEIVING_STATION_NOT_FOUND);
        }
        return buildExportDto(order, receivingStation);
    }

    /**
     * Method returns update export details by order id.
     *
     * @param id    of {@link Long} order id;
     * @param dto   of{@link ExportDetailsDtoUpdate}
     * @param email {@link String} email;
     * @return {@link ExportDetailsDto};
     * @author Orest Mahdziak
     */
    @Override
    public ExportDetailsDto updateOrderExportDetails(Long id, ExportDetailsDtoUpdate dto, String email) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        final List<ReceivingStation> receivingStation = getAllReceivingStations();
        String action;
        if (order.getReceivingStation() == null && order.getDateOfExport() == null
            && order.getDeliverFrom() == null && order.getDeliverTo() == null) {
            action = OrderHistory.SET_EXPORT_DETAILS;
        } else {
            action = OrderHistory.UPDATE_EXPORT_DETAILS;
        }
        order.setReceivingStation(getUpdatedReceivingStation(dto.getReceivingStationId(), order));
        order.setDateOfExport(getUpdatedDateExport(dto.getDateExport(), order));
        if (order.getDateOfExport() != null && order.getDateOfExport().equals(LocalDate.now())
            && order.getOrderStatus().equals(OrderStatus.CONFIRMED)) {
            order.setOrderStatus(OrderStatus.ON_THE_ROUTE);
        }
        order.setDeliverFrom(getUpdatedDeliveryFrom(dto.getTimeDeliveryFrom(), order));
        order.setDeliverTo(getUpdatedDeliveryTo(dto.getTimeDeliveryTo(), order));
        orderRepository.save(order);
        eventService.saveEvent(action + getEventOnUpdateExportDetails(order), email, order);
        return buildExportDto(order, receivingStation);
    }

    private String getEventOnUpdateExportDetails(Order order) {
        String action = "";
        if (order.getDateOfExport() != null) {
            action = String.format(OrderHistory.UPDATE_EXPORT_DATA, order.getDateOfExport());
        }
        if (order.getDeliverFrom() != null && order.getDeliverTo() != null) {
            action = action + String.format(OrderHistory.UPDATE_DELIVERY_TIME, order.getDeliverFrom().toLocalTime(),
                order.getDeliverTo().toLocalTime());
        }
        if (order.getReceivingStation() != null) {
            action =
                action + String.format(OrderHistory.UPDATE_RECEIVING_STATION, order.getReceivingStation().getName());
        }
        return action;
    }

    private List<ReceivingStation> getAllReceivingStations() {
        List<ReceivingStation> receivingStation = receivingStationRepository.findAll();
        if (receivingStation.isEmpty()) {
            throw new NotFoundException(RECEIVING_STATION_NOT_FOUND);
        }
        return receivingStation;
    }

    private ReceivingStation getUpdatedReceivingStation(Long receivingStationId, Order order) {
        if (nonNull(receivingStationId)) {
            return receivingStationRepository.findById(receivingStationId)
                .orElseThrow(() -> new NotFoundException(
                    RECEIVING_STATION_NOT_FOUND_BY_ID + receivingStationId));
        } else if (isOrderStatusFormedOrCanceledOrBroughtHimself(order)) {
            return null;
        }
        return order.getReceivingStation();
    }

    private LocalDate getUpdatedDateExport(String dateExport, Order order) {
        if (dateExport != null) {
            String[] date = dateExport.split("T");
            return LocalDate.parse(date[0]);
        } else if (isOrderStatusFormedOrCanceledOrBroughtHimself(order)) {
            return null;
        }
        return order.getDateOfExport();
    }

    private LocalDateTime getUpdatedDeliveryFrom(String timeDeliveryFrom, Order order) {
        if (timeDeliveryFrom != null) {
            return LocalDateTime.parse(timeDeliveryFrom);
        } else if (isOrderStatusFormedOrCanceledOrBroughtHimself(order)) {
            return null;
        }
        return order.getDeliverFrom();
    }

    private LocalDateTime getUpdatedDeliveryTo(String timeDeliveryTo, Order order) {
        if (timeDeliveryTo != null) {
            return LocalDateTime.parse(timeDeliveryTo);
        } else if (isOrderStatusFormedOrCanceledOrBroughtHimself(order)) {
            return null;
        }
        return order.getDeliverTo();
    }

    private ExportDetailsDto buildExportDto(Order order, List<ReceivingStation> receivingStations) {
        return ExportDetailsDto.builder()
            .allReceivingStations(
                receivingStations.stream()
                    .map(receivingStation -> modelMapper.map(receivingStation, ReceivingStationDto.class))
                    .collect(Collectors.toList()))
            .dateExport(order.getDateOfExport() != null && order.getDeliverFrom() != null ? order.getDateOfExport()
                + "T" + order.getDeliverFrom().toLocalTime() : null)
            .timeDeliveryFrom(order.getDeliverFrom() != null ? order.getDeliverFrom().toString() : null)
            .timeDeliveryTo(order.getDeliverTo() != null ? order.getDeliverTo().toString() : null)
            .receivingStationId(nonNull(order.getReceivingStation()) ? order.getReceivingStation().getId() : null)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdditionalBagInfoDto> getAdditionalBagsInfo(Long orderId) {
        User user = userRepository.findUserByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        String recipientEmail = user.getRecipientEmail();
        List<AdditionalBagInfoDto> ourResult1 = new ArrayList<>();
        List<Map<String, Object>> ourResult = bagRepository.getAdditionalBagInfo(orderId, recipientEmail);
        for (Map<String, Object> array : ourResult) {
            AdditionalBagInfoDto dto = objectMapper.convertValue(array, AdditionalBagInfoDto.class);
            ourResult1.add(dto);
        }
        return ourResult1;
    }

    /**
     * Method that calculates overpayment on user's order.
     *
     * @param order    of {@link Order} order;
     * @param sumToPay of {@link Long} sum to pay;
     * @return {@link Long }
     * @author Ostap Mykhailivskyi
     */
    private Long calculateOverpayment(Order order, Long sumToPay) {
        long paymentSum = order.getPayment().stream()
            .filter(payment -> PaymentStatus.PAID.equals(payment.getPaymentStatus()))
            .map(payment -> payment.getAmount() / 100)
            .reduce(0L, Long::sum);

        long certificateSum = order.getCertificates().stream()
            .map(Certificate::getPoints)
            .reduce(0, Integer::sum);

        long overpayment = paymentSum + certificateSum + order.getPointsToUse() - sumToPay;

        return OrderStatus.CANCELED.equals(order.getOrderStatus())
            ? paymentSum
            : Math.max(overpayment, 0L);
    }

    /**
     * Method that calculate paid amount.
     *
     * @param order of {@link Order} order id;
     * @return {@link Long }
     * @author Ostap Mykhailivskyi
     */
    private Long calculatePaidAmount(Order order) {
        return order.getPayment().stream().filter(x -> x.getPaymentStatus().equals(PaymentStatus.PAID))
            .map(Payment::getAmount).map(amount -> amount / 100).reduce(0L, Long::sum);
    }

    /**
     * Method that calculate unpaid amount.
     *
     * @param order      of {@link Order} order id;
     * @param sumToPay   of {@link Long} sum to pay;
     * @param paidAmount of {@link Long} sum to pay;
     * @return {@link Long }
     * @author Ostap Mykhailivskyi
     */
    private Long calculateUnpaidAmount(Order order, Long sumToPay, Long paidAmount) {
        sumToPay =
            sumToPay - ((order.getCertificates().stream().map(Certificate::getPoints).reduce(Integer::sum).orElse(0))
                + order.getPointsToUse());
        return sumToPay - paidAmount > 0 ? Math.abs(sumToPay - paidAmount) : 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String email) {
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
    public ManualPaymentResponseDto updateManualPayment(Long paymentId,
        ManualPaymentRequestDto paymentRequestDto,
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
        CounterOrderDetailsDto dto = getPriceDetails(order.getId());
        long paymentsForCurrentOrder = order.getPayment().stream().filter(payment -> payment.getPaymentStatus()
            .equals(PaymentStatus.PAID)).map(Payment::getAmount).map(payment -> payment / 100).reduce(Long::sum)
            .orElse(0L);
        long totalPaidAmount = (long) (paymentsForCurrentOrder + dto.getCertificateBonus() + dto.getBonus());
        double totalAmount = setTotalPrice(dto);

        if (paymentsForCurrentOrder > 0 && totalAmount > totalPaidAmount) {
            order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
            notificationService.notifyHalfPaidPackage(order);
        } else if (paymentsForCurrentOrder > 0 && totalAmount <= totalPaidAmount) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            notificationService.notifyPaidOrder(order);
        } else if (paymentsForCurrentOrder == 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        }
        orderRepository.save(order);
    }

    private Payment changePaymentEntity(Payment updatePayment,
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
            .paymentStatus(verifyPaymentStatusOrder(order))
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
        if (order.getOrderPaymentStatus() != null) {
            if (order.getOrderPaymentStatus() == OrderPaymentStatus.PAID) {
                eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
            } else {
                eventService.save(OrderHistory.ORDER_HALF_PAID, OrderHistory.SYSTEM, order);
            }
        }
        return payment;
    }

    private PaymentStatus verifyPaymentStatusOrder(Order order) {
        if (order.getOrderPaymentStatus() == OrderPaymentStatus.PAID) {
            return PaymentStatus.PAID;
        }
        if (order.getOrderPaymentStatus() == OrderPaymentStatus.HALF_PAID) {
            return PaymentStatus.HALF_PAID;
        }
        if (order.getOrderPaymentStatus() == OrderPaymentStatus.UNPAID) {
            return PaymentStatus.UNPAID;
        }
        if (order.getOrderPaymentStatus() == OrderPaymentStatus.PAYMENT_REFUNDED) {
            return PaymentStatus.PAYMENT_REFUNDED;
        }
        return PaymentStatus.UNPAID;
    }

    @Override
    public EmployeePositionDtoRequest getAllEmployeesByPosition(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        checkAvailableOrderForEmployee(order, email);
        EmployeePositionDtoRequest dto = EmployeePositionDtoRequest.builder().orderId(order.getId()).build();
        List<EmployeeOrderPosition> newList = employeeOrderPositionRepository.findAllByOrderId(order.getId());
        if (!newList.isEmpty()) {
            Map<PositionDto, String> currentPositionEmployee = new HashMap<>();
            newList.forEach(x -> currentPositionEmployee.put(
                PositionDto.builder().id(x.getPosition().getId()).name(x.getPosition().getName()).build(),
                x.getEmployee().getFirstName().concat(" ").concat(x.getEmployee().getLastName())));
            dto.setCurrentPositionEmployees(currentPositionEmployee);
        }
        List<Position> positions = positionRepository.findAll();
        Map<PositionDto, List<EmployeeNameIdDto>> allPositionEmployee = new HashMap<>();
        for (Position position : positions) {
            PositionDto positionDto = PositionDto.builder().id(position.getId()).name(position.getName()).build();
            allPositionEmployee.put(positionDto, listAvailableEmployeeWithPosition(order, position)
                .stream().map(employee -> EmployeeNameIdDto.builder()
                    .id(employee.getId())
                    .name(employee.getFirstName() + " " + employee.getLastName())
                    .build())
                .collect(Collectors.toList()));
        }
        dto.setAllPositionsEmployees(allPositionEmployee);

        return dto;
    }

    @Override
    public ReasonNotTakeBagDto saveReason(Long orderId, String description, MultipartFile[] images) {
        final Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        List<String> pictures = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null) {
                pictures.add(fileService.upload(image));
            } else {
                pictures.add(DEFAULT_IMAGE_PATH);
            }
        }
        ReasonNotTakeBagDto dto = new ReasonNotTakeBagDto();
        dto.setImages(pictures);
        dto.setDescription(description);
        dto.setTime(LocalDate.now());
        dto.setCurrentUser(order.getUser().getRecipientName() + " " + order.getUser().getRecipientSurname());
        order.setImageReasonNotTakingBags(pictures);
        order.setReasonNotTakingBagDescription(description);
        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        return dto;
    }

    /**
     * This is service method which is save adminComment.
     *
     * @param adminCommentDto {@link AdminCommentDto}.
     * @param email           {@link String}.
     * @author Yuriy Bahlay.
     */
    @Override
    public void saveAdminCommentToOrder(AdminCommentDto adminCommentDto, String email) {
        Order order = orderRepository.findById(adminCommentDto.getOrderId()).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + adminCommentDto.getOrderId()));
        checkAvailableOrderForEmployee(order, email);
        order.setAdminComment(adminCommentDto.getAdminComment());
        orderRepository.save(order);
        eventService.save(OrderHistory.ADD_ADMIN_COMMENT, email
            + "  " + email, order);
    }

    /**
     * This is method updates eco id from the shop for order.
     *
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @param orderId      {@link Long}.
     * @param email        {@link String}.
     *
     * @author Yuriy Bahlay, Sikhovskiy Rostyslav.
     */
    @Override
    public void updateEcoNumberForOrder(EcoNumberDto ecoNumberDto, Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Set<String> oldEcoNumbers = Set.copyOf(order.getAdditionalOrders());
        Set<String> newEcoNumbers = ecoNumberDto.getEcoNumber();

        Collection<String> removed = CollectionUtils.subtract(oldEcoNumbers, newEcoNumbers);
        Collection<String> added = CollectionUtils.subtract(newEcoNumbers, oldEcoNumbers);
        StringBuilder historyChanges = new StringBuilder();

        if (!removed.isEmpty()) {
            historyChanges.append(collectInfoAboutChangesOfEcoNumber(removed, OrderHistory.DELETED_ECO_NUMBER));
            removed.stream()
                .forEach(oldNumber -> order.getAdditionalOrders().remove(oldNumber));
        }
        if (!added.isEmpty()
            && !added.contains("")) {
            historyChanges.append(collectInfoAboutChangesOfEcoNumber(added, OrderHistory.ADD_NEW_ECO_NUMBER));
            added.stream()
                .forEach(newNumber -> {
                    if (!newNumber.matches("[0-9]+") || newNumber.length() != 10) {
                        throw new BadRequestException(INCORRECT_ECO_NUMBER);
                    }
                    order.getAdditionalOrders().add(newNumber);
                });
        }

        orderRepository.save(order);
        eventService.saveEvent(historyChanges.toString(), email, order);
    }

    private String collectInfoAboutChangesOfEcoNumber(Collection<String> newEcoNumbers, String orderHistory) {
        return String.format("%s: %s; ", orderHistory, String.join("; ", newEcoNumbers));
    }

    /**
     * This is method which is updates admin page info for order.
     *
     * @param updateOrderPageDto {@link UpdateOrderPageAdminDto}.
     * @param orderId            {@link Long}.
     *
     * @author Yuriy Bahlay, Sikhovskiy Rostyslav.
     */
    @Override
    @Transactional
    public void updateOrderAdminPageInfo(UpdateOrderPageAdminDto updateOrderPageDto, Long orderId, String lang,
        String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        checkAvailableOrderForEmployee(order, email);
        try {
            if (nonNull(updateOrderPageDto.getGeneralOrderInfo())) {
                updateOrderDetailStatus(orderId, updateOrderPageDto.getGeneralOrderInfo(), email);
            }
            if (nonNull(updateOrderPageDto.getUserInfoDto())) {
                ubsClientService.updateUbsUserInfoInOrder(updateOrderPageDto.getUserInfoDto(), email);
            }
            if (nonNull(updateOrderPageDto.getAddressExportDetailsDto())) {
                updateAddress(updateOrderPageDto.getAddressExportDetailsDto(), orderId, email);
            }
            if (nonNull(updateOrderPageDto.getExportDetailsDto())) {
                updateOrderExportDetails(orderId, updateOrderPageDto.getExportDetailsDto(), email);
            }
            if (nonNull(updateOrderPageDto.getEcoNumberFromShop())) {
                updateEcoNumberForOrder(updateOrderPageDto.getEcoNumberFromShop(), orderId, email);
            }
            if (nonNull(updateOrderPageDto.getOrderDetailDto())) {
                setOrderDetail(
                    orderId,
                    updateOrderPageDto.getOrderDetailDto().getAmountOfBagsConfirmed(),
                    updateOrderPageDto.getOrderDetailDto().getAmountOfBagsExported(),
                    email);
            }
            if (nonNull(updateOrderPageDto.getUpdateResponsibleEmployeeDto())) {
                updateOrderPageDto.getUpdateResponsibleEmployeeDto().stream()
                    .forEach(dto -> ordersAdminsPageService.responsibleEmployee(List.of(orderId),
                        dto.getEmployeeId().toString(),
                        dto.getPositionId(),
                        email));
            } else {
                if (isOrderStatusFormedOrCanceledOrBroughtHimself(order)) {
                    List<EmployeeOrderPosition> employeeOrderPositions = employeeOrderPositionRepository
                        .findAllByOrderId(orderId);
                    if (!employeeOrderPositions.isEmpty()) {
                        employeeOrderPositionRepository.deleteAll(employeeOrderPositions);
                    }
                }
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private boolean isOrderStatusFormedOrCanceledOrBroughtHimself(Order order) {
        return order.getOrderStatus() == OrderStatus.FORMED
            || order.getOrderStatus() == OrderStatus.CANCELED
            || order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF;
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

    private List<Employee> listAvailableEmployeeWithPosition(Order order, Position position) {
        Long tariffId = order.getTariffsInfo().getId();
        return employeeRepository.findAllByEmployeePositionId(position.getId())
            .stream()
            .filter(
                employee -> tariffsInfoRepository.findTariffsInfoByIdForEmployee(tariffId, employee.getId())
                    .isPresent())
            .collect(Collectors.toList());
    }

    /**
     * This method checks if Employee is assigned to the order.
     * 
     * @param orderId - ID of chosen order {@link Long}.
     * @param email   - employee's email {@link String}.
     *
     * @return {@link Boolean}
     */
    public Boolean checkEmployeeForOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Long employeeId = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        boolean status = false;
        List<Long> tariffsInfoIds = employeeRepository.findTariffsInfoForEmployee(employeeId);
        for (Long id : tariffsInfoIds) {
            status = id.equals(order.getTariffsInfo().getId()) ? true : status;
        }
        return status;
    }

    @Override
    public void updateAllOrderAdminPageInfo(UpdateAllOrderPageDto updateAllOrderPageDto, String email, String lang) {
        for (Long id : updateAllOrderPageDto.getOrderId()) {
            Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
            try {
                updateOrderExportDetails(id, updateAllOrderPageDto.getExportDetailsDto(), email);
                checkUpdateResponsibleEmployeeDto(updateAllOrderPageDto, order, email);
            } catch (Exception e) {
                throw new BadRequestException(e.getMessage());
            }
        }
    }

    private void checkUpdateResponsibleEmployeeDto(UpdateAllOrderPageDto updateAllOrderPageDto, Order order,
        String email) {
        if (nonNull(updateAllOrderPageDto.getUpdateResponsibleEmployeeDto())) {
            updateAllOrderPageDto.getUpdateResponsibleEmployeeDto().stream()
                .forEach(dto -> {
                    if (nonNull(dto.getEmployeeId()) && nonNull(dto.getPositionId())) {
                        ordersAdminsPageService.responsibleEmployee(List.of(order.getId()),
                            dto.getEmployeeId().toString(),
                            dto.getPositionId(),
                            email);
                    }
                });
        }
    }

    private void checkOverpayment(Long overpayment) {
        if (overpayment == 0L) {
            throw new BadRequestException(USER_HAS_NO_OVERPAYMENT);
        }
    }

    @Override
    public AddBonusesToUserDto addBonusesToUser(AddBonusesToUserDto addBonusesToUserDto,
        Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        CounterOrderDetailsDto prices = getPriceDetails(orderId);
        Long overpayment = calculateOverpayment(order, setTotalPrice(prices).longValue());
        checkOverpayment(overpayment);
        User currentUser = order.getUser();

        order.getPayment().add(Payment.builder()
            .amount(overpayment * (-100))
            .settlementDate(addBonusesToUserDto.getSettlementdate())
            .paymentId(addBonusesToUserDto.getPaymentId())
            .receiptLink(addBonusesToUserDto.getReceiptLink())
            .order(order)
            .currency("UAH")
            .orderStatus(String.valueOf(OrderStatus.FORMED))
            .paymentStatus(PaymentStatus.PAID)
            .build());

        transferPointsToUser(order, currentUser, overpayment.intValue());

        orderRepository.save(order);
        userRepository.save(currentUser);
        eventService.saveEvent(OrderHistory.ADDED_BONUSES, email, order);

        return AddBonusesToUserDto.builder()
            .amount(addBonusesToUserDto.getAmount())
            .settlementdate(addBonusesToUserDto.getSettlementdate())
            .receiptLink(addBonusesToUserDto.getReceiptLink())
            .paymentId(addBonusesToUserDto.getPaymentId())
            .build();
    }

    private void transferPointsToUser(Order order, User user, int points) {
        if (points <= 0) {
            return;
        }

        user.setCurrentPoints(user.getCurrentPoints() + points);

        user.setChangeOfPointsList(ListUtils.defaultIfNull(user.getChangeOfPointsList(), new ArrayList<>()));
        user.getChangeOfPointsList()
            .add(ChangeOfPoints.builder()
                .user(user)
                .amount(points)
                .date(LocalDateTime.now())
                .order(order)
                .build());
        notificationService.notifyBonuses(order, (long) points);
    }

    @Override
    public void updateOrderStatusToExpected() {
        orderRepository.updateOrderStatusToExpected(OrderStatus.CONFIRMED.name(),
            OrderStatus.ON_THE_ROUTE.name(),
            LocalDate.now());
    }

    @Override
    public OrderCancellationReasonDto getOrderCancellationReason(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return OrderCancellationReasonDto.builder()
            .cancellationReason(order.getCancellationReason())
            .cancellationComment(order.getCancellationComment())
            .build();
    }

    @Override
    public NotTakenOrderReasonDto getNotTakenOrderReason(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        return NotTakenOrderReasonDto.builder()
            .description(order.getReasonNotTakingBagDescription())
            .images(order.getImageReasonNotTakingBags())
            .build();
    }
}
