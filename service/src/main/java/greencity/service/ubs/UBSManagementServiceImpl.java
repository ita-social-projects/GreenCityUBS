package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.address.AddressExportDetailsDto;
import greencity.dto.bag.*;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.courier.CourierInfoDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.employee.EmployeeNameIdDto;
import greencity.dto.employee.EmployeePositionDtoRequest;
import greencity.dto.employee.EmployeePositionDtoResponse;
import greencity.dto.order.*;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.*;
import greencity.dto.position.PositionDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.violation.ViolationsInfoDto;
import greencity.entity.enums.*;
import greencity.entity.order.*;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.FoundException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.*;
import greencity.service.NotificationServiceImpl;
import lombok.AllArgsConstructor;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class UBSManagementServiceImpl implements UBSManagementService {
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final UserRemoteClient userRemoteClient;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BagRepository bagRepository;
    private final BagTranslationRepository bagTranslationRepository;
    private final UpdateOrderDetail updateOrderRepository;
    private final BagsInfoRepo bagsInfoRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final AdditionalBagsInfoRepo additionalBagsInfoRepo;
    private final NotificationServiceImpl notificationService;
    private final FileService fileService;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private static final String DEFAULT_IMAGE_PATH = AppConstant.DEFAULT_IMAGE;
    private final EventService eventService;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final ServiceRepository serviceRepository;

    private final Set<OrderStatus> orderStatusesBeforeShipment =
        EnumSet.of(OrderStatus.FORMED, OrderStatus.CONFIRMED, OrderStatus.ADJUSTMENT);
    private final Set<OrderStatus> orderStatusesAfterConfirmation =
        EnumSet.of(OrderStatus.ON_THE_ROUTE, OrderStatus.DONE, OrderStatus.BROUGHT_IT_HIMSELF, OrderStatus.CANCELED);
    private final OrdersAdminsPageService ordersAdminsPageService;
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
     * Method returns overpayment and bonuses to users account.
     *
     * @param orderId                   of {@link Long} order id;
     * @param overpaymentInfoRequestDto {@link OverpaymentInfoRequestDto}
     * @param uuid                      {@link String}.
     * @author Ostap Mykhailivskyi
     */
    @Override
    public void returnOverpayment(Long orderId,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        User user = userRepository.findUserByOrderId(orderId)
            .orElseThrow(
                () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Payment payment = createPayment(order, overpaymentInfoRequestDto);
        if ((order.getOrderStatus() == OrderStatus.DONE)) {
            returnOverpaymentForStatusDone(user, order, overpaymentInfoRequestDto, payment);
        }
        if (order.getOrderStatus() == OrderStatus.CANCELED
            && overpaymentInfoRequestDto.getComment().equals(AppConstant.PAYMENT_REFUND)) {
            returnOverpaymentAsMoneyForStatusCancelled(user, order, overpaymentInfoRequestDto);
            collectEventsAboutOverpayment(overpaymentInfoRequestDto.getComment(), order, currentUser);
        }
        if (order.getOrderStatus() == OrderStatus.CANCELED && overpaymentInfoRequestDto.getComment()
            .equals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT)) {
            returnOverpaymentAsBonusesForStatusCancelled(user, order, overpaymentInfoRequestDto);
            collectEventsAboutOverpayment(overpaymentInfoRequestDto.getComment(), order, currentUser);
        }
        if (OrderStatus.CANCELED.equals(order.getOrderStatus())
            && !AppConstant.PAYMENT_REFUND.equals(overpaymentInfoRequestDto.getComment())
            && !AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT.equals(overpaymentInfoRequestDto.getComment())) {
            throw new NotFoundException(COMMENT_ERROR + overpaymentInfoRequestDto.getComment());
        }
        order.getPayment().add(payment);
        userRepository.save(user);
    }

    /**
     * This is method which collect's events about overpayment for order.
     *
     * @param commentStatus {@link String} comments status.
     * @param order         {@link Order}.
     * @param currentUser   {@link User}.
     * @author Yuriy Bahlay.
     */
    private void collectEventsAboutOverpayment(String commentStatus, Order order, User currentUser) {
        if (commentStatus.equals(AppConstant.PAYMENT_REFUND)) {
            eventService.save(OrderHistory.RETURN_OVERPAYMENT_TO_CLIENT, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
        } else if (commentStatus.equals(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT)) {
            eventService.save(OrderHistory.RETURN_OVERPAYMENT_AS_BONUS_TO_CLIENT,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
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
        Order order = orderRepository.getUserByOrderId(orderId).orElseThrow(
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
        Page<Certificate> certificates = certificateRepository.getAll(pageRequest);
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
        return modelMapper.map(addressRepository.getAddressByOrderId(orderId), ReadAddressByOrderDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<OrderAddressDtoResponse> updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Long orderId,
        String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Optional<Address> addressForAdminPage = addressRepository.findById(dtoUpdate.getAddressId());
        if (addressForAdminPage.isPresent()) {
            addressRepository.save(updateAddressOrderInfo(addressForAdminPage.get(), dtoUpdate));
            eventService.save(OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
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
    public OrderStatusPageDto getOrderStatusData(Long orderId) {
        CounterOrderDetailsDto prices = getPriceDetails(orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        List<BagInfoDto> bagInfo = new ArrayList<>();
        List<Bag> bags = bagRepository.findAll();
        Integer fullPrice = serviceRepository.findFullPriceByCourierId(order.getTariffsInfo().getCourier().getId());
        Address address = order.getUbsUser().getAddress();
        bags.forEach(bag -> {
            BagInfoDto bagInfoDto = modelMapper.map(bag, BagInfoDto.class);
            bagInfoDto.setName(bagTranslationRepository.findNameByBagId(bag.getId()).toString());
            bagInfoDto.setNameEng(bagTranslationRepository.findNameEngByBagId(bag.getId()).toString());
            bagInfo.add(bagInfoDto);
        });
        UserInfoDto userInfoDto =
            ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(orderId, order.getUser().getUuid());
        GeneralOrderInfo infoAboutStatusesAndDateFormed =
            getInfoAboutStatusesAndDateFormed(Optional.of(order));
        AddressExportDetailsDto addressDtoForAdminPage = getAddressDtoForAdminPage(address);
        return OrderStatusPageDto.builder()
            .generalOrderInfo(infoAboutStatusesAndDateFormed)
            .userInfoDto(userInfoDto)
            .addressExportDetailsDto(addressDtoForAdminPage)
            .addressComment(address.getAddressComment()).bags(bagInfo)
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
            .employeePositionDtoRequest(getAllEmployeesByPosition(orderId))
            .comment(order.getComment())
            .courierPricePerPackage(fullPrice)
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
    private AddressExportDetailsDto getAddressDtoForAdminPage(Address address) {
        return AddressExportDetailsDto.builder()
            .addressId(address.getId())
            .addressCity(address.getCity())
            .addressStreet(address.getStreet())
            .addressDistrict(address.getDistrict())
            .addressEntranceNumber(address.getEntranceNumber())
            .addressHouseCorpus(address.getHouseCorpus())
            .addressHouseNumber(address.getHouseNumber())
            .addressRegion(address.getRegion())
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
        Map<Integer, Integer> confirmed, Map<Integer, Integer> exported, String uuid) {
        final long wasPaid =
            paymentRepository.selectSumPaid(orderId) == null ? 0L
                : paymentRepository.selectSumPaid(orderId) / 100;
        final User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        collectEventsAboutSetOrderDetails(confirmed, exported, orderId, currentUser);
        final Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));

        if (nonNull(confirmed)) {
            for (Map.Entry<Integer, Integer> entry : confirmed.entrySet()) {
                if (Boolean.TRUE.equals(!updateOrderRepository.ifRecordExist(orderId,
                    entry.getKey().longValue()))) {
                    updateOrderRepository.insertNewRecord(orderId, entry.getKey().longValue());
                    updateOrderRepository.updateAmount(0, orderId, entry.getKey().longValue());
                }
                updateOrderRepository
                    .updateConfirm(entry.getValue(), orderId,
                        entry.getKey().longValue());
            }
        }

        if (nonNull(exported)) {
            for (Map.Entry<Integer, Integer> entry : exported.entrySet()) {
                if (Boolean.TRUE.equals(!updateOrderRepository.ifRecordExist(orderId,
                    entry.getKey().longValue()))) {
                    updateOrderRepository.insertNewRecord(orderId, entry.getKey().longValue());
                    updateOrderRepository.updateAmount(0, orderId, entry.getKey().longValue());
                }
                updateOrderRepository
                    .updateExporter(entry.getValue(), orderId,
                        entry.getKey().longValue());
            }
        }

        long discount = orderRepository.findSumOfCertificatesByOrderId(orderId);
        var price = getPriceDetails(orderId);
        Long needToPay = setTotalPrice(price).longValue() - (wasPaid) - discount;

        if (needToPay <= 0) {
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.PAID.name());
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

    private void collectEventsAboutSetOrderDetails(Map<Integer, Integer> confirmed, Map<Integer, Integer> exported,
        Long orderId, User currentUser) {
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
            eventService.save(values.toString(),
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
    }

    private void collectEventAboutConfirmWaste(Map<Integer, Integer> confirmed, Order order,
        Long orderId, int countOfChanges, StringBuilder values) {
        for (Map.Entry<Integer, Integer> entry : confirmed.entrySet()) {
            Integer capacity = bagRepository.findCapacityById(entry.getKey());
            StringBuilder bagTranslation = bagTranslationRepository.findNameByBagId(entry.getKey());

            if (order.getOrderStatus() == OrderStatus.ADJUSTMENT
                || order.getOrderStatus() == OrderStatus.CONFIRMED
                || order.getOrderStatus() == OrderStatus.FORMED
                || order.getOrderStatus() == OrderStatus.NOT_TAKEN_OUT) {
                Optional<Long> confirmWasteWas = Optional.empty();
                if (Boolean.TRUE.equals(updateOrderRepository.ifRecordExist(orderId, entry.getKey().longValue()))) {
                    confirmWasteWas =
                        Optional.ofNullable(updateOrderRepository.getConfirmWaste(orderId, entry.getKey().longValue()));
                }
                if (entry.getValue().longValue() != confirmWasteWas.orElse(0L)) {
                    if (countOfChanges == 0) {
                        values.append(OrderHistory.CHANGE_ORDER_DETAILS + " ");
                    }
                    values.append(bagTranslation).append(" ").append(capacity).append(" л: ")
                        .append(confirmWasteWas.orElse(0L))
                        .append(" шт на ").append(entry.getValue()).append(" шт.");
                }
            }
        }
    }

    private void collectEventAboutExportedWaste(Map<Integer, Integer> exported, Order order,
        Long orderId, int countOfChanges, StringBuilder values) {
        for (Map.Entry<Integer, Integer> entry : exported.entrySet()) {
            Integer capacity = bagRepository.findCapacityById(entry.getKey());
            StringBuilder bagTranslation = bagTranslationRepository.findNameByBagId(entry.getKey());
            if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE
                || order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF
                || order.getOrderStatus() == OrderStatus.DONE
                || order.getOrderStatus() == OrderStatus.CANCELED) {
                Optional<Long> exporterWasteWas = Optional.empty();
                if (Boolean.TRUE.equals(updateOrderRepository.ifRecordExist(orderId, entry.getKey().longValue()))) {
                    exporterWasteWas =
                        Optional
                            .ofNullable(updateOrderRepository.getExporterWaste(orderId, entry.getKey().longValue()));
                }
                if (entry.getValue().longValue() != exporterWasteWas.orElse(0L)) {
                    if (countOfChanges == 0) {
                        values.append(OrderHistory.CHANGE_ORDER_DETAILS + " ");
                        countOfChanges++;
                    }
                    values.append(bagTranslation).append(" ").append(capacity).append(" л: ")
                        .append(exporterWasteWas.orElse(0L))
                        .append(" шт на ").append(entry.getValue()).append(" шт.");
                }
            }
        }
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
        List<Bag> bag = bagRepository.findBagByOrderId(id);
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
        List<Payment> payment = paymentRepository.paymentInfo(id);
        if (payment.isEmpty()) {
            throw new NotFoundException(PAYMENT_NOT_FOUND + id);
        }
        return buildStatuses(order, payment.get(0));
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public OrderDetailStatusDto updateOrderDetailStatus(Long id, OrderDetailStatusRequestDto dto, String uuid) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Payment> payment = paymentRepository.paymentInfo(id);
        if (payment.isEmpty()) {
            throw new NotFoundException(PAYMENT_NOT_FOUND + id);
        }
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (nonNull(dto.getAdminComment())) {
            order.setAdminComment(dto.getAdminComment());
            eventService.save(OrderHistory.ADD_ADMIN_COMMENT, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
            orderRepository.save(order);
        }
        if (nonNull(dto.getOrderStatus())) {
            order.setOrderStatus(OrderStatus.valueOf(dto.getOrderStatus()));
            if (order.getOrderStatus() == OrderStatus.ADJUSTMENT) {
                notificationService.notifyCourierItineraryFormed(order);
                eventService.save(OrderHistory.ORDER_ADJUSTMENT,
                    currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
            } else if (order.getOrderStatus() == OrderStatus.CONFIRMED) {
                eventService.save(OrderHistory.ORDER_CONFIRMED,
                    currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
            } else if (order.getOrderStatus() == OrderStatus.NOT_TAKEN_OUT) {
                eventService.save(
                    OrderHistory.ORDER_NOT_TAKEN_OUT + "  " + order.getComment() + "  "
                        + order.getImageReasonNotTakingBags(),
                    currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
            } else if (order.getOrderStatus() == OrderStatus.CANCELED && order.getPointsToUse() != 0) {
                notificationService.notifyBonusesFromCanceledOrder(order);
                returnAllPointsFromOrder(order);
                order.setCancellationComment(dto.getCancellationComment());
                eventService.save(OrderHistory.ORDER_CANCELLED + "  " + dto.getCancellationComment(),
                    currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
            } else if (order.getOrderStatus() == OrderStatus.DONE) {
                eventService.save(OrderHistory.ORDER_DONE,
                    currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
            } else if (order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF) {
                eventService.save(OrderHistory.ORDER_BROUGHT_IT_HIMSELF,
                    currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
            } else if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE) {
                eventService.save(OrderHistory.ORDER_ON_THE_ROUTE,
                    currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
            }
            orderRepository.save(order);
        }
        if (nonNull(dto.getOrderPaymentStatus())) {
            paymentRepository.paymentInfo(id)
                .forEach(x -> x.setPaymentStatus(PaymentStatus.valueOf(dto.getOrderPaymentStatus())));
            paymentRepository.saveAll(payment);
        }

        return buildStatuses(order, payment.get(0));
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

    private OrderDetailDto setOrderDetailDto(OrderDetailDto dto, Order order) {
        dto.setAmount(modelMapper.map(order, new TypeToken<List<BagMappingDto>>() {
        }.getType()));

        dto.setCapacityAndPrice(bagRepository.findBagByOrderId(order.getId())
            .stream()
            .map(b -> modelMapper.map(b, BagInfoDto.class))
            .collect(Collectors.toList()));

        dto.setName(bagTranslationRepository.findAllByOrder(order.getId())
            .stream()
            .map(b -> modelMapper.map(b, BagTransDto.class))
            .collect(Collectors.toList()));

        dto.setOrderId(order.getId());

        return dto;
    }

    private Address updateAddressOrderInfo(Address address, OrderAddressExportDetailsDtoUpdate dto) {
        if (nonNull(dto.getAddressCity())) {
            address.setCity(dto.getAddressCity());
        }
        if (nonNull(dto.getAddressRegion())) {
            address.setRegion(dto.getAddressRegion());
        }
        if (nonNull(dto.getAddressHouseNumber())) {
            address.setHouseNumber(dto.getAddressHouseNumber());
        }
        if (nonNull(dto.getAddressEntranceNumber())) {
            address.setEntranceNumber(dto.getAddressEntranceNumber());
        }
        if (nonNull(dto.getAddressDistrict())) {
            address.setDistrict(dto.getAddressDistrict());
        }
        if (nonNull(dto.getAddressStreet())) {
            address.setStreet(dto.getAddressStreet());
        }
        if (nonNull(dto.getAddressHouseCorpus())) {
            address.setHouseCorpus(dto.getAddressHouseCorpus());
        }
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
        List<Map<String, Object>> ourResult = bagsInfoRepository.getBagInfo(orderId);
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
     * @param id  of {@link Long} order id;
     * @param dto of{@link ExportDetailsDtoUpdate}
     * @return {@link ExportDetailsDto};
     * @author Orest Mahdziak
     */
    @Override
    public ExportDetailsDto updateOrderExportDetails(Long id, ExportDetailsDtoUpdate dto, String uuid) {
        final User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        if (nonNull(dto.getReceivingStationId())) {
            ReceivingStation station = receivingStationRepository.findById(dto.getReceivingStationId())
                .orElseThrow(() -> new NotFoundException(
                    RECEIVING_STATION_NOT_FOUND_BY_ID + dto.getReceivingStationId()));
            order.setReceivingStation(station);
        }
        List<ReceivingStation> receivingStation = receivingStationRepository.findAll();
        if (receivingStation.isEmpty()) {
            throw new NotFoundException(RECEIVING_STATION_NOT_FOUND);
        }
        String dateExport = dto.getDateExport() != null ? dto.getDateExport() : null;
        String timeDeliveryFrom = dto.getTimeDeliveryFrom() != null ? dto.getTimeDeliveryFrom() : null;
        String timeDeliveryTo = dto.getTimeDeliveryTo() != null ? dto.getTimeDeliveryTo() : null;
        if (dateExport != null) {
            String[] date = dateExport.split("T");
            order.setDateOfExport(LocalDate.parse(date[0]));
        }
        if (timeDeliveryFrom != null) {
            LocalDateTime dateTime = LocalDateTime.parse(timeDeliveryFrom);
            order.setDeliverFrom(dateTime);
        }
        if (timeDeliveryTo != null) {
            LocalDateTime dateAndTimeDeliveryTo = LocalDateTime.parse(timeDeliveryTo);
            order.setDeliverTo(dateAndTimeDeliveryTo);
        }
        orderRepository.save(order);
        final String receivingStationValue = order.getReceivingStation().getName();
        final LocalDateTime deliverFrom = order.getDeliverFrom();
        collectEventsAboutOrderExportDetails(receivingStationValue, deliverFrom, order, currentUser);
        return buildExportDto(order, receivingStation);
    }

    /**
     * This is private method which collect's event for order export details.
     *
     * @param receivingStationValue {@link String}.
     * @param deliverFrom           {@link LocalDateTime}.
     * @param order                 {@link Order}.
     * @author Yuriy Bahlay.
     */
    private void collectEventsAboutOrderExportDetails(String receivingStationValue, LocalDateTime deliverFrom,
        Order order, User currentUser) {
        if (receivingStationValue != null || deliverFrom != null) {
            eventService.save(OrderHistory.UPDATE_EXPORT_DETAILS, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
        } else {
            eventService.save(OrderHistory.SET_EXPORT_DETAILS, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
        }
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
        List<Map<String, Object>> ourResult = additionalBagsInfoRepo.getAdditionalBagInfo(orderId, recipientEmail);
        for (Map<String, Object> array : ourResult) {
            AdditionalBagInfoDto dto = objectMapper.convertValue(array, AdditionalBagInfoDto.class);
            ourResult1.add(dto);
        }
        return ourResult1;
    }

    /**
     * Method that calculate's overpayment on user's order.
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

    private ChangeOfPoints createChangeOfPoints(Order order, User user, Long amount) {
        return ChangeOfPoints.builder()
            .date(LocalDateTime.now())
            .user(user)
            .order(order)
            .amount(Math.toIntExact(amount))
            .build();
    }

    private Payment createPayment(Order order, OverpaymentInfoRequestDto dto) {
        return Payment.builder()
            .order(order)
            .orderStatus("approved")
            .comment(dto.getComment())
            .currency("UAH")
            .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .paymentStatus(PaymentStatus.PAYMENT_REFUNDED)
            .amount(dto.getOverpayment())
            .build();
    }

    private void returnOverpaymentForStatusDone(User user, Order order,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto,
        Payment payment) {
        user.setCurrentPoints((int) (user.getCurrentPoints() + overpaymentInfoRequestDto.getOverpayment()));
        user.getChangeOfPointsList()
            .add(createChangeOfPoints(order, user, overpaymentInfoRequestDto.getOverpayment()));
        payment.setComment(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT);
    }

    private void returnOverpaymentAsMoneyForStatusCancelled(User user, Order order,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto) {
        user.setCurrentPoints((int) (user.getCurrentPoints() + overpaymentInfoRequestDto.getBonuses()));
        user.getChangeOfPointsList().add(createChangeOfPoints(order, user, overpaymentInfoRequestDto.getBonuses()));
        order.getPayment().forEach(p -> p.setPaymentStatus(PaymentStatus.PAYMENT_REFUNDED));
    }

    private void returnOverpaymentAsBonusesForStatusCancelled(User user, Order order,
        OverpaymentInfoRequestDto overpaymentInfoRequestDto) {
        user.setCurrentPoints((int) (user.getCurrentPoints() + overpaymentInfoRequestDto.getOverpayment()
            + overpaymentInfoRequestDto.getBonuses()));
        user.getChangeOfPointsList()
            .add(createChangeOfPoints(order, user, overpaymentInfoRequestDto.getOverpayment()));
        user.getChangeOfPointsList().add(createChangeOfPoints(order, user, overpaymentInfoRequestDto.getBonuses()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));

        ManualPaymentResponseDto manualPaymentResponseDto = buildPaymentResponseDto(
            paymentRepository.save(buildPaymentEntity(order, paymentRequestDto, image, currentUser)));
        updateOrderPaymentStatusForManualPayment(order);
        return manualPaymentResponseDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteManualPayment(Long paymentId, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not found"));
        if (payment.getImagePath() != null) {
            fileService.delete(payment.getImagePath());
        }
        paymentRepository.deletePaymentById(paymentId);
        eventService.save(OrderHistory.DELETE_PAYMENT_MANUALLY + paymentId,
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), payment.getOrder());
        updateOrderPaymentStatusForManualPayment(payment.getOrder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManualPaymentResponseDto updateManualPayment(Long paymentId,
        ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
            () -> new NotFoundException(PAYMENT_NOT_FOUND + paymentId));
        Payment paymentUpdated = paymentRepository.save(changePaymentEntity(payment, paymentRequestDto, image));
        eventService.save(OrderHistory.UPDATE_PAYMENT_MANUALLY + paymentRequestDto.getPaymentId(),
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), payment.getOrder());

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
        User currentUser) {
        Payment payment = Payment.builder()
            .settlementDate(paymentRequestDto.getSettlementdate())
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
        eventService.save(OrderHistory.ADD_PAYMENT_MANUALLY + paymentRequestDto.getPaymentId(),
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
        return payment;
    }

    @Override
    public EmployeePositionDtoRequest getAllEmployeesByPosition(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
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
            allPositionEmployee.put(positionDto, employeeRepository.getAllEmployeeByPositionId(position.getId())
                .stream().map(employee -> EmployeeNameIdDto.builder()
                    .id(employee.getId())
                    .name(employee.getFirstName() + " " + employee.getLastName())
                    .build())
                .collect(Collectors.toList()));
            dto.setAllPositionsEmployees(allPositionEmployee);
        }
        dto.setAllPositionsEmployees(allPositionEmployee);

        return dto;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Transactional
    public void updatePositions(EmployeePositionDtoResponse dto, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(dto.getOrderId())
            .orElseThrow(
                () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + " " + dto.getOrderId()));
        List<EmployeeOrderPosition> employeeOrderPositions = new ArrayList<>();
        for (EmployeeOrderPositionDTO employeeOrderPositionDTO : dto.getEmployeeOrderPositionDTOS()) {
            String[] dtoFirstAndLastName = new String[0];
            try {
                dtoFirstAndLastName = employeeOrderPositionDTO.getName().split(" ");
            } catch (IndexOutOfBoundsException e) {
                throw new NotFoundException(EMPLOYEE_DOESNT_EXIST);
            }
            Position position = positionRepository.findById(employeeOrderPositionDTO.getPositionId())
                .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));
            Employee employee = employeeRepository.findByName(dtoFirstAndLastName[0], dtoFirstAndLastName[1])
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND));
            Long oldEmployeePositionId =
                employeeOrderPositionRepository.findPositionOfEmployeeAssignedForOrder(employee.getId());
            if (nonNull(oldEmployeePositionId) && oldEmployeePositionId != 0 && oldEmployeePositionId != 1) {
                collectEventsAboutUpdatingEmployeesAssignedForOrder(oldEmployeePositionId, order, currentUser);
            }
            employeeOrderPositions.add(EmployeeOrderPosition.builder()
                .employee(employee)
                .position(position)
                .order(order)
                .build());
        }
        List<EmployeeOrderPosition> newList = employeeOrderPositionRepository.findAllByOrderId(dto.getOrderId());
        if (!newList.isEmpty()) {
            employeeOrderPositionRepository.deleteAll(newList);
        }
        employeeOrderPositionRepository.saveAll(employeeOrderPositions);
    }

    /**
     * This is private method which collect's event when managers will update
     * assigned.
     *
     * @param position {@link Long}.
     * @param order    {@link Order}.
     * @author Yuriy Bahlay.
     */
    private void collectEventsAboutUpdatingEmployeesAssignedForOrder(Long position, Order order, User currentUser) {
        if (position == 2) {
            eventService.save(OrderHistory.UPDATE_MANAGER_CALL,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 3) {
            eventService.save(OrderHistory.UPDATE_MANAGER_LOGIEST,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 4) {
            eventService.save(OrderHistory.UPDATE_MANAGER_CALL_PILOT,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 5) {
            eventService.save(OrderHistory.UPDATE_MANAGER_DRIVER,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
    }

    @Override
    public ReasonNotTakeBagDto saveReason(Long orderId, String description, List<MultipartFile> images) {
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
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void assignEmployeesWithThePositionsToTheOrder(AssignEmployeesForOrderDto dto, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(dto.getOrderId()).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + dto.getOrderId()));
        if (dto.getEmployeesList() != null) {
            for (int i = 0; i < dto.getEmployeesList().size(); i++) {
                AssignForOrderEmployee assignForOrderEmployee = dto.getEmployeesList().get(i);
                boolean isExistEmployee = employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(dto.getOrderId(),
                    assignForOrderEmployee.getEmployeeId());
                if (isExistEmployee) {
                    throw new FoundException(
                        EMPLOYEE_ALREADY_ASSIGNED + assignForOrderEmployee.getEmployeeId());
                }
                Employee employeeForAssigning = employeeRepository.findById(assignForOrderEmployee.getEmployeeId())
                    .orElseThrow(() -> new NotFoundException(
                        EMPLOYEE_NOT_FOUND + assignForOrderEmployee.getEmployeeId()));
                Long positionForEmployee =
                    employeeRepository.findPositionForEmployee(assignForOrderEmployee.getEmployeeId())
                        .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));
                if (positionForEmployee != 1) {
                    EmployeeOrderPosition employeeOrderPositions = EmployeeOrderPosition.builder()
                        .order(order)
                        .employee(employeeForAssigning)
                        .position(Position.builder().id(positionForEmployee).build())
                        .build();
                    employeeOrderPositionRepository.save(employeeOrderPositions);
                    collectsEventsAboutAssigningEmployees(positionForEmployee, currentUser, order);
                } else {
                    throw new FoundException(ErrorMessage.EMPLOYEE_IS_NOT_ASSIGN);
                }
            }
        }
    }

    /**
     * This is private method which collect's event when managers will assign.
     *
     * @param position    {@link Long}.
     * @param currentUser {@link User}
     * @param order       {@link Order}.
     * @author Yuriy Bahlay.
     */
    private void collectsEventsAboutAssigningEmployees(Long position, User currentUser, Order order) {
        if (position == 2) {
            eventService.save(OrderHistory.ASSIGN_CALL_MANAGER,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 3) {
            eventService.save(OrderHistory.ASSIGN_LOGIEST,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 4) {
            eventService.save(OrderHistory.ASSIGN_CALL_PILOT,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        } else if (position == 5) {
            eventService.save(OrderHistory.ASSIGN_DRIVER,
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
    }

    /**
     * This is service method which is save adminComment.
     *
     * @param adminCommentDto {@link AdminCommentDto}.
     * @param uuid            {@link String}.
     * @author Yuriy Bahlay.
     */
    @Override
    public void saveAdminCommentToOrder(AdminCommentDto adminCommentDto, String uuid) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(adminCommentDto.getOrderId()).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + adminCommentDto.getOrderId()));
        order.setAdminComment(adminCommentDto.getAdminComment());
        orderRepository.save(order);
        eventService.save(OrderHistory.ADD_ADMIN_COMMENT, user.getRecipientName()
            + "  " + user.getRecipientSurname(), order);
    }

    /**
     * This is method updates eco id from the shop for order.
     *
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @param orderId      {@link Long}.
     * @param uuid         {@link String}.
     *
     * @author Yuriy Bahlay, Sikhovskiy Rostyslav.
     */
    @Override
    public void updateEcoNumberForOrder(EcoNumberDto ecoNumberDto, Long orderId, String uuid) {
        final User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
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
        eventService.save(historyChanges.toString(),
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
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
    public void updateOrderAdminPageInfo(UpdateOrderPageAdminDto updateOrderPageDto, Long orderId, String lang,
        String currentUser) {
        try {
            if (nonNull(updateOrderPageDto.getGeneralOrderInfo())) {
                updateOrderDetailStatus(orderId, updateOrderPageDto.getGeneralOrderInfo(), currentUser);
            }
            if (nonNull(updateOrderPageDto.getUserInfoDto())) {
                ubsClientService.updateUbsUserInfoInOrder(updateOrderPageDto.getUserInfoDto(), currentUser);
            }
            if (nonNull(updateOrderPageDto.getAddressExportDetailsDto())) {
                updateAddress(updateOrderPageDto.getAddressExportDetailsDto(), orderId, currentUser);
            }
            if (nonNull(updateOrderPageDto.getExportDetailsDto())) {
                updateOrderExportDetails(orderId, updateOrderPageDto.getExportDetailsDto(), currentUser);
            }
            if (nonNull(updateOrderPageDto.getEcoNumberFromShop())) {
                updateEcoNumberForOrder(updateOrderPageDto.getEcoNumberFromShop(), orderId, currentUser);
            }
            if (nonNull(updateOrderPageDto.getOrderDetailDto())) {
                setOrderDetail(
                    orderId,
                    updateOrderPageDto.getOrderDetailDto().getAmountOfBagsConfirmed(),
                    updateOrderPageDto.getOrderDetailDto().getAmountOfBagsExported(),
                    currentUser);
            }
            if (nonNull(updateOrderPageDto.getUpdateResponsibleEmployeeDto())) {
                updateOrderPageDto.getUpdateResponsibleEmployeeDto().stream()
                    .forEach(dto -> ordersAdminsPageService.responsibleEmployee(List.of(orderId),
                        dto.getEmployeeId().toString(),
                        dto.getPositionId(),
                        currentUser));
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public void updateAllOrderAdminPageInfo(UpdateAllOrderPageDto updateAllOrderPageDto, String uuid, String lang) {
        for (Long id : updateAllOrderPageDto.getOrderId()) {
            Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
            try {
                updateOrderExportDetails(id, updateAllOrderPageDto.getExportDetailsDto(), uuid);
                checkUpdateResponsibleEmployeeDto(updateAllOrderPageDto, order, uuid);
            } catch (Exception e) {
                throw new BadRequestException(e.getMessage());
            }
        }
    }

    private void checkUpdateResponsibleEmployeeDto(UpdateAllOrderPageDto updateAllOrderPageDto, Order order,
        String uuid) {
        if (nonNull(updateAllOrderPageDto.getUpdateResponsibleEmployeeDto())) {
            updateAllOrderPageDto.getUpdateResponsibleEmployeeDto().stream()
                .forEach(dto -> {
                    if (nonNull(dto.getEmployeeId()) && nonNull(dto.getPositionId())) {
                        ordersAdminsPageService.responsibleEmployee(List.of(order.getId()),
                            dto.getEmployeeId().toString(),
                            dto.getPositionId(),
                            uuid);
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
        Long orderId) {
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
}
