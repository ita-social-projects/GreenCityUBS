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
import greencity.dto.location.api.DistrictDto;
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
import greencity.dto.position.PositionDto;
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
import greencity.entity.order.Refund;
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
import greencity.enums.SortingOrder;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.locations.LocationApiService;
import greencity.service.notification.NotificationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static greencity.constant.AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT_ENG;
import static greencity.constant.AppConstant.PAYMENT_REFUND_ENG;
import static greencity.constant.ErrorMessage.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
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
    private final LocationApiService locationApiService;
    private final RefundRepository refundRepository;
    private final OrderLockService orderLockService;
    private final OrderBagService orderBagService;
    private final PaymentService paymentService;
    private final EventRepository eventRepository;
    private static final String DEFAULT_IMAGE_PATH = AppConstant.DEFAULT_IMAGE;
    private static final List<String> ADMIN_POSITION_NAMES = List.of("Admin", "Super Admin");
    private final Set<OrderStatus> orderStatusesBeforeShipment =
        EnumSet.of(OrderStatus.FORMED, OrderStatus.CONFIRMED, OrderStatus.ADJUSTMENT);
    private final Set<OrderStatus> orderStatusesAfterConfirmation =
        EnumSet.of(OrderStatus.ON_THE_ROUTE, OrderStatus.DONE, OrderStatus.BROUGHT_IT_HIMSELF, OrderStatus.CANCELED);
    static final String FORMAT_DATE = "dd-MM-yyyy";
    private final UBSClientService ubsClientService;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<CertificateDtoForSearching> getAllCertificates(Pageable page, String columnName,
        SortingOrder sortingOrder) {
        PageRequest pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(),
            Sort.by(Sort.Direction.fromString(sortingOrder.toString()), columnName));
        Page<Certificate> certificates = certificateRepository.findAll(pageRequest);
        return getAllCertificatesTranslationDto(certificates);
    }

    /**
     * {@inheritDoc}
     */
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
    public Optional<OrderAddressDtoResponse> updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Order order,
        String email) {
        Optional<OrderAddress> addressForAdminPage = orderAddressRepository.findById(dtoUpdate.getId());
        if (addressForAdminPage.isPresent()) {
            orderAddressRepository.save(updateAddressOrderInfo(addressForAdminPage.get(), dtoUpdate));
            eventService.saveEvent(OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE, email, order);
            return addressForAdminPage.map(value -> modelMapper.map(value, OrderAddressDtoResponse.class));
        } else {
            throw new NotFoundException(NOT_FOUND_ADDRESS_BY_ORDER_ID + dtoUpdate.getId());
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
        dto.forEach(data -> data.setOrderPrice(
            PaymentUtil.getPriceDetails(data.getId(), orderRepository, orderBagService, certificateRepository)
                .getTotalSumAmount()));
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderStatusPageDto getOrderStatusData(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND));

        checkAvailableOrderForEmployee(order, email);
        CounterOrderDetailsDto prices =
            PaymentUtil.getPriceDetails(orderId, orderRepository, orderBagService, certificateRepository);

        var bagInfoDtoList = bagRepository.findAllActiveBagsByTariffsInfoId(order.getTariffsInfo().getId()).stream()
            .map(bag -> modelMapper.map(bag, BagInfoDto.class))
            .collect(Collectors.toList());

        Long servicePriceInCoins = serviceRepository.findServiceByTariffsInfoId(order.getTariffsInfo().getId())
            .map(it -> it.getPrice())
            .orElse(0L);

        var orderAddress = order.getUbsUser().getOrderAddress();

        UserInfoDto userInfoDto =
            ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(orderId, order.getUser().getUuid());
        GeneralOrderInfo infoAboutStatusesAndDateFormed =
            getInfoAboutStatusesAndDateFormed(Optional.of(order));
        AddressExportDetailsDto addressDtoForAdminPage = getAddressDtoForAdminPage(orderAddress);

        if (!order.isBlocked() && checkEmployeePositionsIsAdmin(employee.getEmployeePosition())) {
            orderLockService.lockOrder(order, employee);
        }

        return OrderStatusPageDto.builder()
            .generalOrderInfo(infoAboutStatusesAndDateFormed)
            .userInfoDto(userInfoDto)
            .addressExportDetailsDto(addressDtoForAdminPage)
            .addressComment(orderAddress.getAddressComment())
            .bags(bagInfoDtoList)
            .orderFullPrice(setTotalPrice(prices))
            .orderDiscountedPrice(paymentService.getPaymentInfo(orderId, prices.getSumAmount()).getUnPaidAmount())
            .orderBonusDiscount(prices.getBonus())
            .orderCertificateTotalDiscount(prices.getCertificateBonus())
            .orderExportedPrice(prices.getSumExported())
            .orderExportedDiscountedPrice(prices.getTotalSumExported())
            .amountOfBagsOrdered(order.getAmountOfBagsOrdered())
            .amountOfBagsExported(order.getExportedQuantity())
            .amountOfBagsConfirmed(order.getConfirmedQuantity())
            .numbersFromShop(order.getAdditionalOrders())
            .certificates(prices.getCertificate())
            .paymentTableInfoDto(paymentService.getPaymentInfo(orderId, setTotalPrice(prices)))
            .exportDetailsDto(getOrderExportDetails(orderId))
            .employeePositionDtoRequest(getAllEmployeesByPosition(orderId, email))
            .comment(order.getComment())
            .courierPricePerPackage(PaymentUtil.convertCoinsIntoBills(servicePriceInCoins))
            .courierInfo(modelMapper.map(order.getTariffsInfo(), CourierInfoDto.class))
            .writeOffStationSum(PaymentUtil.convertCoinsIntoBills(order.getWriteOffStationSum()))
            .build();
    }

    /**
     * Checks if any position in the given set has administrative permissions.
     * Specifically, it checks if any position's ID matches the IDs associated with
     * the "Admin" or "Super Admin" roles.
     *
     * @param positions the set of positions to check
     * @return true if any position in the set is an admin position, false otherwise
     */
    private boolean checkEmployeePositionsIsAdmin(Set<Position> positions) {
        List<Long> idsWithValidPermissionsToEditOrder = positionRepository
            .findAllIdsFromNames(ADMIN_POSITION_NAMES);
        return positions.stream()
            .anyMatch(p -> idsWithValidPermissionsToEditOrder.contains(p.getId()));
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
        return dto.getSumConfirmed() != 0;
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
     * @author Yuriy Bahlay.
     */
    private AddressExportDetailsDto getAddressDtoForAdminPage(OrderAddress address) {
        return AddressExportDetailsDto.builder()
            .id(address.getId())
            .city(address.getCity())
            .cityEn(address.getCityEn())
            .street(address.getStreet())
            .streetEn(address.getStreetEn())
            .district(address.getDistrict())
            .districtEn(address.getDistrictEn())
            .entranceNumber(address.getEntranceNumber())
            .houseCorpus(address.getHouseCorpus())
            .houseNumber(address.getHouseNumber())
            .region(address.getRegion())
            .regionEn(address.getRegionEn())
            .addressRegionDistrictList(
                locationApiService.getAllDistrictsInCityByNames(address.getRegion(), address.getCity()).stream()
                    .map(p -> modelMapper.map(p, DistrictDto.class))
                    .collect(Collectors.toList()))
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
            .blocked(currentOrder.isBlocked())
            .build();
    }

    /**
     * This is method which is get order translation statuses in two languages like:
     * ua and en.
     *
     * @return {@link List}.
     * @author Yuriy Bahlay.
     */
    private List<OrderStatusesTranslationDto> getOrderStatusesTranslation() {
        List<OrderStatusesTranslationDto> orderStatusesTranslationDtos = new ArrayList<>();
        List<OrderStatusTranslation> orderStatusTranslations =
            orderStatusTranslationRepository.findAllBy();
        if (!orderStatusTranslations.isEmpty()) {
            for (OrderStatusTranslation orderStatusTranslation : orderStatusTranslations) {
                OrderStatusesTranslationDto orderStatusesTranslationDto = new OrderStatusesTranslationDto();
                setValueForOrderStatusIsNotTakenOutOrDoneOrCancelledAsTrue(orderStatusTranslation,
                    orderStatusesTranslationDto);
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
     * This is method which set value as true for orderStatus Cancelled or Done or
     * Not Taken Out.
     *
     * @param orderStatusTranslation      {@link OrderStatusTranslation}.
     * @param orderStatusesTranslationDto {@link OrderStatusesTranslationDto}.
     * @author Yuriy Bahlay.
     */
    private void setValueForOrderStatusIsNotTakenOutOrDoneOrCancelledAsTrue(
        OrderStatusTranslation orderStatusTranslation,
        OrderStatusesTranslationDto orderStatusesTranslationDto) {
        orderStatusesTranslationDto
            .setAbleActualChange(OrderStatus.NOT_TAKEN_OUT.getNumValue() == orderStatusTranslation.getStatusId()
                || OrderStatus.DONE.getNumValue() == orderStatusTranslation.getStatusId()
                || OrderStatus.CANCELED.getNumValue() == orderStatusTranslation.getStatusId());
    }

    /**
     * This is method which is get order payment statuses translation.
     *
     * @return {@link List}.
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
    public void setOrderDetail(Order order,
        Map<Integer, Integer> confirmed, Map<Integer, Integer> exported, String email) {
        Long orderId = order.getId();
        final long wasPaidInCoins =
            paymentRepository.selectSumPaid(orderId) == null ? 0L
                : paymentRepository.selectSumPaid(orderId);
        collectEventsAboutSetOrderDetails(confirmed, exported, order, email);
        if (!(order.getOrderStatus() == OrderStatus.DONE || order.getOrderStatus() == OrderStatus.NOT_TAKEN_OUT
            || order.getOrderStatus() == OrderStatus.CANCELED)) {
            exported = null;
        }

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

        long discountInCoins = orderRepository.findSumOfCertificatesByOrderId(orderId) * 100L;
        long totalPriceInCoins = PaymentUtil.convertBillsIntoCoins(setTotalPrice(
            PaymentUtil.getPriceDetails(orderId, orderRepository, orderBagService, certificateRepository)));
        long needToPayInCoins = totalPriceInCoins - wasPaidInCoins - discountInCoins;

        updatePaymentStatus(order, wasPaidInCoins, discountInCoins, totalPriceInCoins, needToPayInCoins, email);
    }

    private void updatePaymentStatus(Order order, long wasPaidInCoins, long discountInCoins,
        long totalPriceInCoins, long needToPayInCoins, String email) {
        Long orderId = order.getId();

        if (needToPayInCoins == 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.PAID.name());
            return;
        }
        if (totalPriceInCoins - wasPaidInCoins >= 0 && needToPayInCoins < 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.PAID.name());
            recalculateCertificates(totalPriceInCoins - wasPaidInCoins, order);
            eventService.saveEvent(OrderHistory.ORDER_PAID, email, order);
            return;
        }
        if (totalPriceInCoins < wasPaidInCoins) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.PAID.name());
            eventService.saveEvent(OrderHistory.ORDER_PAID, email, order);
            recalculateCertificates(0L, order);
            return;
        }
        if (needToPayInCoins > 0 && wasPaidInCoins + discountInCoins != 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.HALF_PAID.name());
            eventService.saveEvent(OrderHistory.ORDER_HALF_PAID, email, order);
            notificationService.notifyHalfPaidPackage(order);
            return;
        }
        if (wasPaidInCoins + discountInCoins == 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
            orderRepository.updateOrderPaymentStatus(orderId, OrderPaymentStatus.UNPAID.name());
        }
    }

    private void recalculateCertificates(long amountInCoins, Order order) {
        Set<Certificate> certificates = order.getCertificates();
        for (Certificate certificate : certificates) {
            amountInCoins = amountInCoins - certificate.getPoints() * 100L;
            if (amountInCoins < 0) {
                certificate.setPoints(certificate.getPoints() + BigDecimal.valueOf(amountInCoins)
                    .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                    .setScale(0, RoundingMode.UP).intValue());
                certificateRepository.save(certificate);
                amountInCoins = 0L;
            }
        }
        recalculatePoints(BigDecimal.valueOf(PaymentUtil.convertCoinsIntoBills(amountInCoins))
            .setScale(0, RoundingMode.HALF_UP).intValue(), order);
    }

    private void recalculatePoints(int amount, Order order) {
        if (order.getPointsToUse() > amount) {
            userRepository.updateUserCurrentPoints(order.getUser().getId(), order.getPointsToUse() - amount);
            order.setPointsToUse(amount);
            orderRepository.updateOrderPointsToUse(order.getId(), amount);
        }
    }

    private void collectEventsAboutSetOrderDetails(Map<Integer, Integer> confirmed, Map<Integer, Integer> exported,
        Order order, String email) {
        StringBuilder values = new StringBuilder();
        int countOfChanges = 0;
        if (nonNull(exported)) {
            collectEventAboutExportedWaste(exported, order, order.getId(), countOfChanges, values);
        }
        if (nonNull(confirmed)) {
            collectEventAboutConfirmWaste(confirmed, order, order.getId(), countOfChanges, values);
        }
        if (nonNull(confirmed) || nonNull(exported)) {
            eventService.saveEvent(values.toString(), email, order);
        }
    }

    private void collectEventAboutConfirmWaste(Map<Integer, Integer> confirmed, Order order,
        Long orderId, int countOfChanges, StringBuilder values) {
        for (Map.Entry<Integer, Integer> entry : confirmed.entrySet()) {
            Integer capacity = bagRepository.findCapacityById(entry.getKey());
            Optional<Bag> bagOptional = bagRepository.findActiveBagById(entry.getKey());
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
            Optional<Bag> bagOptional = bagRepository.findActiveBagById(entry.getKey());
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
        CounterOrderDetailsDto dto =
            PaymentUtil.getPriceDetails(orderId, orderRepository, orderBagService, certificateRepository);
        Order order = orderRepository.getOrderDetails(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));

        long totalSumAmountInCoins = PaymentUtil.convertBillsIntoCoins(dto.getTotalSumAmount());
        long totalSumConfirmedInCoins = PaymentUtil.convertBillsIntoCoins(dto.getTotalSumConfirmed());
        long totalSumExportedInCoins = PaymentUtil.convertBillsIntoCoins(dto.getTotalSumExported());

        updateOrderPaymentStatus(order, totalSumAmountInCoins, totalSumConfirmedInCoins, totalSumExportedInCoins);
        return dto;
    }

    private void updateOrderPaymentStatus(Order currentOrder, long totalSumAmountInCoins, long totalConfirmedInCoins,
        long totalExportedInCoins) {
        long paymentsForCurrentOrderInCoins = currentOrder.getPayment().stream()
            .filter(payment -> payment.getPaymentStatus().equals(PaymentStatus.PAID))
            .map(Payment::getAmount)
            .reduce(Long::sum)
            .orElse(0L);

        if (orderStatusesBeforeShipment.contains(currentOrder.getOrderStatus())) {
            setOrderPaymentStatusForConfirmedBags(currentOrder, paymentsForCurrentOrderInCoins, totalSumAmountInCoins,
                totalConfirmedInCoins);
        } else if (orderStatusesAfterConfirmation.contains(currentOrder.getOrderStatus())) {
            setOrderPaymentStatusForExportedBags(currentOrder, paymentsForCurrentOrderInCoins, totalExportedInCoins);
        }
        orderRepository.save(currentOrder);
    }

    private void setOrderPaymentStatusForConfirmedBags(Order currentOrder, long paymentsForCurrentOrder,
        long totalSumAmount, long totalConfirmed) {
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
        long totalExported) {
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
        return buildStatuses(order, payment.getFirst());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderDetailStatusDto updateOrderDetailStatusById(Long id, OrderDetailStatusRequestDto dto, String email) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        return updateOrderDetailStatus(order, dto, email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderDetailStatusDto updateOrderDetailStatus(Order order, OrderDetailStatusRequestDto dto, String email) {
        List<Payment> payment = paymentRepository.findAllByOrderId(order.getId());
        if (payment.isEmpty()) {
            throw new NotFoundException(PAYMENT_NOT_FOUND + order.getId());
        }
        if (nonNull(dto.getAdminComment())) {
            order.setAdminComment(dto.getAdminComment());
            eventService.saveEvent(OrderHistory.ADD_ADMIN_COMMENT, email, order);
            orderRepository.save(order);
        }
        if (nonNull(dto.getOrderStatus())
            && (isNull(order.getOrderStatus()) || order.getOrderStatus().checkPossibleStatus(dto.getOrderStatus()))) {
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
                verifyPaidWithBonuses(order, email);
                setOrderCancellation(order, dto.getCancellationReason(), dto.getCancellationComment());
                eventService.saveEvent(OrderHistory.ORDER_CANCELLED, email, order);
            } else if (order.getOrderStatus() == OrderStatus.DONE) {
                eventService.saveEvent(OrderHistory.ORDER_DONE, email, order);
            } else if (order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF) {
                eventService.saveEvent(OrderHistory.ORDER_BROUGHT_IT_HIMSELF, email, order);
                notificationService.notifySelfPickupOrder(order);
            } else if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE) {
                eventService.saveEvent(OrderHistory.ORDER_ON_THE_ROUTE, email, order);
            }
            orderRepository.save(order);
        }
        if (nonNull(dto.getOrderPaymentStatus())) {
            payment.forEach(x -> x.setPaymentStatus(PaymentStatus.valueOf(dto.getOrderPaymentStatus())));
            paymentRepository.saveAll(payment);
        }

        return buildStatuses(order, payment.getFirst());
    }

    @Override
    public Boolean checkIfOrderStatusIsFormedToCanceled(Long orderId) {
        return eventRepository.wasOrderStatusChangedFromFormedToCanceled(orderId);
    }

    private void verifyPaidWithBonuses(Order order, String email) {
        if (order.getPointsToUse() > 0) {
            eventService.saveEvent(OrderHistory.RETURN_BONUSES_TO_CLIENT + ". Всього " + order.getPointsToUse(), email,
                order);
        }
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

        dto.setCapacityAndPrice(orderBagService.findAllBagsByOrderId(order.getId())
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
        Optional.ofNullable(dto.getCity()).ifPresent(address::setCity);
        Optional.ofNullable(dto.getCityEn()).ifPresent(address::setCityEn);
        Optional.ofNullable(dto.getRegion()).ifPresent(address::setRegion);
        Optional.ofNullable(dto.getRegionEn()).ifPresent(address::setRegionEn);
        Optional.ofNullable(dto.getDistrict()).ifPresent(address::setDistrict);
        Optional.ofNullable(dto.getDistrictEn()).ifPresent(address::setDistrictEn);
        Optional.ofNullable(dto.getStreet()).ifPresent(address::setStreet);
        Optional.ofNullable(dto.getStreetEn()).ifPresent(address::setStreetEn);
        Optional.ofNullable(dto.getHouseNumber()).ifPresent(address::setHouseNumber);
        Optional.ofNullable(dto.getHouseCorpus()).ifPresent(address::setHouseCorpus);
        Optional.ofNullable(dto.getEntranceNumber()).ifPresent(address::setEntranceNumber);

        return address;
    }

    private void returnAllPointsFromOrder(Order order) {
        Integer pointsToReturn = order.getPointsToUse();
        if (isNull(pointsToReturn) || pointsToReturn == 0) {
            return;
        }
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
    public ExportDetailsDto updateOrderExportDetailsById(Long id, ExportDetailsDtoUpdate dto, String email) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        return updateOrderExportDetails(order, dto, email);
    }

    /**
     * Method returns update export details by order id.
     *
     * @param order of {@link Order};
     * @param dto   of{@link ExportDetailsDtoUpdate};
     * @param email {@link String} email;
     * @return {@link ExportDetailsDto};
     * @author Orest Mahdziak
     */
    @Override
    public ExportDetailsDto updateOrderExportDetails(Order order, ExportDetailsDtoUpdate dto, String email) {
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
                PositionDto.builder()
                    .id(x.getPosition().getId())
                    .name(x.getPosition().getName())
                    .nameEn(x.getPosition().getNameEn())
                    .build(),
                x.getEmployee().getFirstName().concat(" ").concat(x.getEmployee().getLastName())));
            dto.setCurrentPositionEmployees(currentPositionEmployee);
        }
        List<Position> positions = positionRepository.findAll();
        Map<PositionDto, List<EmployeeNameIdDto>> allPositionEmployee = new HashMap<>();
        for (Position position : positions) {
            PositionDto positionDto = PositionDto.builder()
                .id(position.getId())
                .name(position.getName())
                .nameEn(position.getNameEn())
                .build();
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
    public void saveReason(Order order, String description, MultipartFile[] images) {
        List<String> pictures = (images != null) ? Arrays.stream(images)
            .map(this::processImage)
            .collect(Collectors.toList())
            : new ArrayList<>();
        ReasonNotTakeBagDto dto = new ReasonNotTakeBagDto();
        dto.setImages(pictures);
        dto.setDescription(description);
        dto.setTime(LocalDate.now());
        dto.setCurrentUser(order.getUser().getRecipientName() + " " + order.getUser().getRecipientSurname());
        order.setImageReasonNotTakingBags(pictures);
        order.setReasonNotTakingBagDescription(description);
        orderRepository.save(order);
    }

    private String processImage(MultipartFile image) {
        return (image != null) ? fileService.upload(image) : DEFAULT_IMAGE_PATH;
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
     * @author Yuriy Bahlay, Sikhovskiy Rostyslav.
     */
    @Override
    public void updateEcoNumberForOrderById(EcoNumberDto ecoNumberDto, Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        updateEcoNumberForOrder(ecoNumberDto, order, email);
    }

    /**
     * This is method updates eco id from the shop for order.
     *
     * @param ecoNumberDto {@link EcoNumberDto}.
     * @param order        {@link Order}.
     * @param email        {@link String}.
     * @author Yuriy Bahlay, Sikhovskiy Rostyslav.
     */
    @Override
    public void updateEcoNumberForOrder(EcoNumberDto ecoNumberDto, Order order, String email) {
        Set<String> oldEcoNumbers = Set.copyOf(order.getAdditionalOrders());
        Set<String> newEcoNumbers = ecoNumberDto.getEcoNumber();

        Collection<String> removed = CollectionUtils.subtract(oldEcoNumbers, newEcoNumbers);
        Collection<String> added = CollectionUtils.subtract(newEcoNumbers, oldEcoNumbers);
        StringBuilder historyChanges = new StringBuilder();

        if (!removed.isEmpty()) {
            historyChanges.append(collectInfoAboutChangesOfEcoNumber(removed, OrderHistory.DELETED_ECO_NUMBER));
            removed.forEach(oldNumber -> order.getAdditionalOrders().remove(oldNumber));
        }
        if (!added.isEmpty()
            && !added.contains("")) {
            historyChanges.append(collectInfoAboutChangesOfEcoNumber(added, OrderHistory.ADD_NEW_ECO_NUMBER));
            added.forEach(newNumber -> {
                if (!newNumber.matches("\\d{4,10}")) {
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

    private void updateOrderPageFields(UpdateOrderPageAdminDto updateOrderPageDto, Order order, String email) {
        if (nonNull(updateOrderPageDto.getUserInfoDto())) {
            ubsClientService.updateUbsUserInfoInOrder(updateOrderPageDto.getUserInfoDto(), email);
        }
        if (nonNull(updateOrderPageDto.getAddressExportDetailsDto())) {
            updateAddress(updateOrderPageDto.getAddressExportDetailsDto(), order, email);
        }
        setUbsCourierSumAndWriteOffStationSum(order, updateOrderPageDto.getWriteOffStationSum(),
            updateOrderPageDto.getUbsCourierSum());
        if (nonNull(updateOrderPageDto.getExportDetailsDto())) {
            updateOrderExportDetails(order, updateOrderPageDto.getExportDetailsDto(), email);
        }
        if (nonNull(updateOrderPageDto.getGeneralOrderInfo())) {
            updateOrderDetailStatus(order, updateOrderPageDto.getGeneralOrderInfo(), email);
        }
        if (nonNull(updateOrderPageDto.getEcoNumberFromShop())) {
            updateEcoNumberForOrder(updateOrderPageDto.getEcoNumberFromShop(), order, email);
        }
        if (nonNull(updateOrderPageDto.getOrderDetailDto())) {
            setOrderDetail(
                order,
                updateOrderPageDto.getOrderDetailDto().getAmountOfBagsConfirmed(),
                updateOrderPageDto.getOrderDetailDto().getAmountOfBagsExported(),
                email);
        }
        long orderId = order.getId();
        if (nonNull(updateOrderPageDto.getUpdateResponsibleEmployeeDto())) {
            updateOrderPageDto.getUpdateResponsibleEmployeeDto()
                .forEach(dto -> ordersAdminsPageService.responsibleEmployee(List.of(orderId),
                    dto.getEmployeeId().toString(),
                    dto.getPositionId(),
                    email));
        } else if (isOrderStatusFormedOrCanceledOrBroughtHimself(order)) {
            List<EmployeeOrderPosition> employeeOrderPositions = employeeOrderPositionRepository
                .findAllByOrderId(orderId);
            if (!employeeOrderPositions.isEmpty()) {
                employeeOrderPositionRepository.deleteAll(employeeOrderPositions);
            }
        }
        if (order.getOrderPaymentStatus().equals(OrderPaymentStatus.UNPAID)) {
            notificationService.notifyUnpaidOrder(order);
        }
    }

    /**
     * This is method which is updates admin page info for order and save reason.
     *
     * @param orderId                 {@link Long}.
     * @param updateOrderPageAdminDto {@link UpdateOrderPageAdminDto}.
     * @param language                {@link String}.
     * @param email                   {@link String}.
     * @param images                  {@link MultipartFile}.
     * @author Anton Bondar.
     */
    @Override
    @Transactional
    public void updateOrderAdminPageInfoAndSaveReason(Long orderId, UpdateOrderPageAdminDto updateOrderPageAdminDto,
        String language, String email, MultipartFile[] images) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        updateOrderAdminPageInfo(updateOrderPageAdminDto, order, language, email);
        saveReason(order, updateOrderPageAdminDto.getNotTakenOutReason(), images);
    }

    /**
     * This is method which is updates admin page info for order.
     *
     * @param updateOrderPageDto {@link UpdateOrderPageAdminDto}.
     * @param order              {@link Order}.
     * @author Yuriy Bahlay, Sikhovskiy Rostyslav.
     */
    @Override
    @Transactional
    public void updateOrderAdminPageInfo(UpdateOrderPageAdminDto updateOrderPageDto, Order order, String lang,
        String email) {
        checkAvailableOrderForEmployee(order, email);
        if (!processRefundForOrder(order, updateOrderPageDto, email)) {
            if (order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF
                && nonNull(updateOrderPageDto.getGeneralOrderInfo())) {
                updateOrderDetailStatus(order, updateOrderPageDto.getGeneralOrderInfo(), email);
            } else {
                try {
                    updateOrderPageFields(updateOrderPageDto, order, email);
                } catch (Exception e) {
                    throw new BadRequestException(e.getMessage());
                }
            }
        }
    }

    private void setUbsCourierSumAndWriteOffStationSum(Order order, Double writeOffStationSum, Double ubsCourierSum) {
        if (writeOffStationSum != null) {
            order.setWriteOffStationSum(PaymentUtil.convertBillsIntoCoins(writeOffStationSum));
        }
        if (ubsCourierSum != null) {
            order.setUbsCourierSum(PaymentUtil.convertBillsIntoCoins(ubsCourierSum));
        }
        orderRepository.save(order);
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
            status = id.equals(order.getTariffsInfo().getId()) || status;
        }
        return status;
    }

    @Override
    public void updateAllOrderAdminPageInfo(UpdateAllOrderPageDto updateAllOrderPageDto, String email, String lang) {
        for (Long id : updateAllOrderPageDto.getOrderId()) {
            Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
            try {
                updateOrderExportDetailsById(id, updateAllOrderPageDto.getExportDetailsDto(), email);
                checkUpdateResponsibleEmployeeDto(updateAllOrderPageDto, order, email);
            } catch (Exception e) {
                throw new BadRequestException(e.getMessage());
            }
        }
    }

    private void checkUpdateResponsibleEmployeeDto(UpdateAllOrderPageDto updateAllOrderPageDto, Order order,
        String email) {
        if (nonNull(updateAllOrderPageDto.getUpdateResponsibleEmployeeDto())) {
            updateAllOrderPageDto.getUpdateResponsibleEmployeeDto()
                .forEach(dto -> {
                    if (nonNull(dto.getEmployeeId()) && dto.getEmployeeId() > 0 && nonNull(dto.getPositionId())) {
                        ordersAdminsPageService.responsibleEmployee(List.of(order.getId()),
                            dto.getEmployeeId().toString(),
                            dto.getPositionId(),
                            email);
                    }
                });
        }
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

    @Override
    public void updateOrderStatusToExpected() {
        orderRepository.updateOrderStatusToExpected(OrderStatus.CONFIRMED.name(),
            OrderStatus.ON_THE_ROUTE.name(),
            LocalDate.now(ZoneId.of("Europe/Kiev")));
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

    private void checkOverpayment(long overpayment) {
        if (overpayment == 0) {
            throw new BadRequestException(USER_HAS_NO_OVERPAYMENT);
        }
    }

    private boolean processRefundForOrder(Order order, UpdateOrderPageAdminDto updateOrderPageAdminDto,
        String employeeEmail) {
        if (order.getOrderStatus() == OrderStatus.CANCELED || order.getOrderStatus() == OrderStatus.DONE
            || order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF) {
            if (updateOrderPageAdminDto.isReturnBonuses()) {
                refundPaymentsInBonus(order, employeeEmail);
                return true;
            } else if (updateOrderPageAdminDto.isReturnMoney()) {
                refundPaymentsInMoney(order, employeeEmail);
                return true;
            } else if (order.getOrderStatus() != OrderStatus.BROUGHT_IT_HIMSELF) {
                throw new BadRequestException(String.format(ORDER_CAN_NOT_BE_UPDATED, order.getOrderStatus()));
            }
        }
        return false;
    }

    private void refundPaymentsInMoney(Order order, String employeeEmail) {
        if (order.getOrderStatus() != OrderStatus.CANCELED) {
            throw new BadRequestException(INCOMPATIBLE_ORDER_STATUS_FOR_REFUND);
        }
        Long paidAmount = PaymentUtil.calculatePaidAmount(order);
        if (paidAmount > 0) {
            order.getPayment().add(Payment.builder()
                .amount(-paidAmount)
                .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .receiptLink(PAYMENT_REFUND_ENG)
                .order(order)
                .currency("UAH")
                .orderStatus(String.valueOf(OrderStatus.FORMED))
                .paymentStatus(PaymentStatus.PAID)
                .build());
            order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_REFUNDED);
            refundRepository.save(Refund.builder()
                .order(order)
                .amount(paidAmount)
                .date(LocalDateTime.now(ZoneId.of("Europe/Kiev")))
                .build());
            order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_REFUNDED);
            orderRepository.save(order);
            eventService.saveEvent(OrderHistory.CANCELED_ORDER_MONEY_REFUND, employeeEmail, order);
        } else {
            throw new BadRequestException(USER_HAS_NO_OVERPAYMENT);
        }
    }

    private void refundPaymentsInBonus(Order order, String email) {
        CounterOrderDetailsDto prices =
            PaymentUtil.getPriceDetails(order.getId(), orderRepository, orderBagService, certificateRepository);
        Long overpaymentInCoins =
            PaymentUtil.calculateOverpayment(order, PaymentUtil.convertBillsIntoCoins(setTotalPrice(prices)));
        checkOverpayment(overpaymentInCoins);
        User currentUser = order.getUser();

        order.getPayment().add(Payment.builder()
            .amount(-overpaymentInCoins)
            .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .receiptLink(ENROLLMENT_TO_THE_BONUS_ACCOUNT_ENG)
            .order(order)
            .currency("UAH")
            .orderStatus(String.valueOf(OrderStatus.FORMED))
            .paymentStatus(PaymentStatus.PAID)
            .build());

        transferPointsToUser(order, currentUser, overpaymentInCoins);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_REFUNDED);
        orderRepository.save(order);
        userRepository.save(currentUser);
        eventService.saveEvent(OrderHistory.ADDED_BONUSES, email, order);
    }
}