package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.RestClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.*;
import greencity.entity.enums.*;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.*;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.*;
import greencity.service.NotificationServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
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
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

@Service
@AllArgsConstructor
public class UBSManagementServiceImpl implements UBSManagementService {
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final RestClient restClient;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BagRepository bagRepository;
    private final BagTranslationRepository bagTranslationRepository;
    private final UpdateOrderDetail updateOrderRepository;
    private final BagsInfoRepo bagsInfoRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final BigOrderTableRepository bigOrderTableRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final AdditionalBagsInfoRepo additionalBagsInfoRepo;
    private final NotificationServiceImpl notificationService;
    private final FileService fileService;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private static final String defaultImagePath = AppConstant.DEFAULT_IMAGE;
    private final EventService eventService;
    private final LanguageRepository languageRepository;
    private final CustomTableViewRepo customTableViewRepo;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final ServiceRepository serviceRepository;

    private final Set<OrderStatus> orderStatusesBeforeShipment =
        EnumSet.of(OrderStatus.FORMED, OrderStatus.CONFIRMED, OrderStatus.ADJUSTMENT);
    private final Set<OrderStatus> orderStatusesAfterConfirmation =
        EnumSet.of(OrderStatus.ON_THE_ROUTE, OrderStatus.DONE, OrderStatus.BROUGHT_IT_HIMSELF, OrderStatus.CANCELED);
    private final OrdersAdminsPageService ordersAdminsPageService;
    @Lazy
    @Autowired
    private UBSClientService ubsClientService;

    /**
     * This method save or update view of orders table.
     *
     * @author Sikhovskiy Rostyslav.
     */
    @Override
    public void changeOrderTableView(String uuid, String titles) {
        if (Boolean.TRUE.equals(customTableViewRepo.existsByUuid(uuid))) {
            customTableViewRepo.update(uuid, titles);
        } else {
            CustomTableView customTableView = CustomTableView.builder()
                .uuid(uuid)
                .titles(titles)
                .build();
            customTableViewRepo.save(customTableView);
        }
    }

    /**
     * This method return parameters for orders table view.
     *
     * @author Sikhovskiy Rostyslav.
     */
    @Override
    public CustomTableViewDto getCustomTableParameters(String uuid) {
        if (Boolean.TRUE.equals(customTableViewRepo.existsByUuid(uuid))) {
            return castTableViewToDto(customTableViewRepo.findByUuid(uuid).getTitles());
        } else {
            return CustomTableViewDto.builder()
                .titles("")
                .build();
        }
    }

    private CustomTableViewDto castTableViewToDto(String titles) {
        return CustomTableViewDto.builder()
            .titles(titles)
            .build();
    }

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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
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
        if ((order.getOrderStatus() == OrderStatus.DONE)) {
            notificationService.notifyBonuses(order, overpayment);
        }
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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        User user = userRepository.findUserByOrderId(orderId)
            .orElseThrow(
                () -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
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
            () -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
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
        String uuidId = restClient.findUuidByEmail(email);
        User user = userRepository.findUserByUuid(uuidId).orElseThrow(() -> new UnexistingUuidExeption(
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
        String ourUUid = restClient.findUuidByEmail(addingPointsToUserDto.getEmail());
        User ourUser = userRepository.findUserByUuid(ourUUid).orElseThrow(() -> new UnexistingUuidExeption(
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
    public void sendNotificationAboutViolation(AddingViolationsToUserDto dto, String language) {
        Order order = orderRepository.findById(dto.getOrderID()).orElse(null);
        UserViolationMailDto mailDto;
        if (order != null) {
            mailDto = UserViolationMailDto.builder()
                .name(order.getUser().getRecipientName())
                .email(order.getUser().getRecipientEmail())
                .violationDescription(dto.getViolationDescription())
                .language(language)
                .build();
            restClient.sendViolationOnMail(mailDto);
        }
    }

    /**
     * {@inheritDoc} and {MaksymKuzbyt}
     */
    @Override
    public Page<BigOrderTableDTO> getOrders(OrderPage orderPage, OrderSearchCriteria searchCriteria, String uuid) {
        Page<Order> orders = bigOrderTableRepository.findAll(orderPage, searchCriteria);
        List<BigOrderTableDTO> orderList = new ArrayList<>();

        orders.forEach(o -> orderList.add(buildBigOrderTableDTO(o)));

        return new PageImpl<>(orderList, orders.getPageable(), orders.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public ReadAddressByOrderDto getAddressByOrderId(Long orderId) {
        if (orderRepository.findById(orderId).isEmpty()) {
            throw new NotFoundOrderAddressException(NOT_FOUND_ADDRESS_BY_ORDER_ID + orderId);
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
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Optional<Address> addressForAdminPage = addressRepository.findById(dtoUpdate.getAddressId());
        if (addressForAdminPage.isPresent()) {
            addressRepository.save(updateAddressOrderInfo(addressForAdminPage.get(), dtoUpdate));
            eventService.save(OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
            return addressForAdminPage.map(value -> modelMapper.map(value, OrderAddressDtoResponse.class));
        } else {
            throw new NotFoundOrderAddressException(NOT_FOUND_ADDRESS_BY_ORDER_ID + dtoUpdate.getAddressId());
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
    public OrderStatusPageDto getOrderStatusData(Long orderId, String languageCode) {
        CounterOrderDetailsDto prices = getPriceDetails(orderId);
        Optional<Order> order = orderRepository.findById(orderId);
        List<BagInfoDto> bagInfo = new ArrayList<>();
        List<Bag> bags = bagRepository.findAll();
        Language language = languageRepository.findLanguageByCode(languageCode);
        Integer fullPrice =
            serviceRepository.findFullPriceByCourierId(order.get().getCourierLocations().getCourier().getId());
        Address address = order.isPresent() ? order.get().getUbsUser().getAddress() : new Address();
        bags.forEach(bag -> {
            BagInfoDto bagInfoDto = modelMapper.map(bag, BagInfoDto.class);
            bagInfoDto.setName(bagTranslationRepository.findNameByBagId(bag.getId(), language.getId()).toString());
            bagInfo.add(bagInfoDto);
        });
        UserInfoDto userInfoDto = ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(orderId);
        GeneralOrderInfo infoAboutStatusesAndDateFormed =
            getInfoAboutStatusesAndDateFormed(order, language);
        AddressExportDetailsDto addressDtoForAdminPage = getAddressDtoForAdminPage(address);
        return OrderStatusPageDto.builder()
            .generalOrderInfo(infoAboutStatusesAndDateFormed)
            .userInfoDto(userInfoDto)
            .addressExportDetailsDto(addressDtoForAdminPage)
            .addressComment(address.getAddressComment()).bags(bagInfo)
            .orderFullPrice(prices.getSumAmount())
            .orderDiscountedPrice(getPaymentInfo(orderId, prices.getSumAmount().longValue()).getUnPaidAmount())
            .orderBonusDiscount(prices.getBonus()).orderCertificateTotalDiscount(prices.getCertificateBonus())
            .orderExportedPrice(prices.getSumExported()).orderExportedDiscountedPrice(prices.getTotalSumExported())
            .amountOfBagsOrdered(order.map(Order::getAmountOfBagsOrdered).orElse(null))
            .amountOfBagsExported(order.map(Order::getExportedQuantity).orElse(null))
            .amountOfBagsConfirmed(order.map(Order::getConfirmedQuantity).orElse(null))
            .numbersFromShop(order.map(Order::getAdditionalOrders).orElse(null))
            .certificates(prices.getCertificate())
            .paymentTableInfoDto(getPaymentInfo(orderId, prices.getSumAmount().longValue()))
            .exportDetailsDto(getOrderExportDetails(orderId))
            .employeePositionDtoRequest(getAllEmployeesByPosition(orderId))
            .comment(
                order.orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST)).getComment())
            .courierPricePerPackage(fullPrice)
            .courierInfo(modelMapper.map(order.get().getCourierLocations(), CourierInfoDto.class))
            .build();
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
     * @param order    {@link Order}.
     * @param language {@link Language}.
     * @return {@link GeneralOrderInfo}.
     *
     * @author Yuriy Bahlay.
     */
    private GeneralOrderInfo getInfoAboutStatusesAndDateFormed(Optional<Order> order, Language language) {
        OrderStatus orderStatus = order.isPresent() ? order.get().getOrderStatus() : OrderStatus.CANCELED;
        Optional<OrderStatusTranslation> orderStatusTranslation =
            orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(orderStatus.getNumValue(),
                languageRepository.findIdByCode(language.getCode()));
        String currentOrderStatusTranslation =
            orderStatusTranslation.isPresent() ? orderStatusTranslation.get().getName() : "order status not found";
        String currentOrderStatusPaymentTranslation = null;
        if (order.isPresent()) {
            currentOrderStatusPaymentTranslation =
                orderPaymentStatusTranslationRepository.findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(
                    (long) order.get().getOrderPaymentStatus().getStatusValue(), language.getId());
        }
        return GeneralOrderInfo.builder()
            .id(order.isPresent() ? order.get().getId() : 0)
            .dateFormed(order.map(Order::getOrderDate).orElse(null))
            .orderStatusesDtos(getOrderStatusesTranslation(order.orElse(null), language.getId()))
            .orderPaymentStatusesDto(getOrderPaymentStatusesTranslation(language.getId()))
            .orderStatus(order.map(Order::getOrderStatus).orElse(null))
            .orderPaymentStatus(order.map(Order::getOrderPaymentStatus).orElse(null))
            .orderPaymentStatusName(
                Optional.of(Objects.requireNonNull(currentOrderStatusPaymentTranslation)).orElse(null))
            .orderStatusName(currentOrderStatusTranslation)
            .adminComment(order.get().getAdminComment())
            .build();
    }

    /**
     * This is method which is get order translation statuses in two languages like:
     * ua and en.
     *
     * @param order      {@link Long}.
     * @param languageId {@link Long}.
     * @return {@link List}.
     *
     * @author Yuriy Bahlay.
     */
    private List<OrderStatusesTranslationDto> getOrderStatusesTranslation(Order order, Long languageId) {
        List<OrderStatusesTranslationDto> orderStatusesTranslationDtos = new ArrayList<>();
        List<OrderStatusTranslation> orderStatusTranslations =
            orderStatusTranslationRepository.getOrderStatusTranslationsByLanguageId(languageId);
        if (!orderStatusTranslations.isEmpty() && order != null) {
            for (OrderStatusTranslation orderStatusTranslation : orderStatusTranslations) {
                OrderStatusesTranslationDto orderStatusesTranslationDto = new OrderStatusesTranslationDto();
                setValueForOrderStatusIsCancelledOrDoneAsTrue(orderStatusTranslation, orderStatusesTranslationDto);
                orderStatusesTranslationDto.setTranslation(orderStatusTranslation.getName());
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
     * @param languageId {@link Long}.
     * @return {@link List}.
     *
     * @author Yuriy Bahlay.
     */
    private List<OrderPaymentStatusesTranslationDto> getOrderPaymentStatusesTranslation(Long languageId) {
        List<OrderPaymentStatusesTranslationDto> orderStatusesTranslationDtos = new ArrayList<>();
        List<OrderPaymentStatusTranslation> orderStatusPaymentTranslations = orderPaymentStatusTranslationRepository
            .getOrderStatusPaymentTranslationsByLanguageId(languageId);
        if (!orderStatusPaymentTranslations.isEmpty()) {
            for (OrderPaymentStatusTranslation orderStatusPaymentTranslation : orderStatusPaymentTranslations) {
                OrderPaymentStatusesTranslationDto translationDto = new OrderPaymentStatusesTranslationDto();
                translationDto.setTranslation(orderStatusPaymentTranslation.getTranslationValue());
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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        setOrderDetailDto(dto, order, language);
        return modelMapper.map(dto, new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public void setOrderDetail(Long orderId,
        Map<Integer, Integer> confirmed, Map<Integer, Integer> exported, String language, String uuid) {
        final User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        collectEventsAboutSetOrderDetails(confirmed, exported, orderId, currentUser, language);

        if (nonNull(exported)) {
            for (Map.Entry<Integer, Integer> entry : exported.entrySet()) {
                if (Boolean.TRUE.equals(!updateOrderRepository.ifRecordExist(orderId,
                    entry.getKey().longValue()))) {
                    updateOrderRepository.insertNewRecord(orderId,
                        entry.getKey().longValue());
                }
                updateOrderRepository
                    .updateExporter(entry.getValue(), orderId,
                        entry.getKey().longValue());
            }
        }
        if (nonNull(confirmed)) {
            for (Map.Entry<Integer, Integer> entry : confirmed.entrySet()) {
                if (Boolean.TRUE.equals(!updateOrderRepository.ifRecordExist(orderId,
                    entry.getKey().longValue()))) {
                    updateOrderRepository.insertNewRecord(orderId,
                        entry.getKey().longValue());
                }
                updateOrderRepository
                    .updateConfirm(entry.getValue(), orderId,
                        entry.getKey().longValue());
            }
        }
    }

    private void collectEventsAboutSetOrderDetails(Map<Integer, Integer> confirmed, Map<Integer, Integer> exported,
        Long orderId, User currentUser, String language) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Long languageId = languageRepository.findIdByCode(language);
        StringBuilder values = new StringBuilder();
        int countOfChanges = 0;
        if (nonNull(exported)) {
            collectEventAboutExportedWaste(exported, languageId, order, orderId, countOfChanges, values);
        }
        if (nonNull(confirmed)) {
            collectEventAboutConfirmWaste(confirmed, languageId, order, orderId, countOfChanges, values);
        }
        if (nonNull(confirmed) || nonNull(exported)) {
            eventService.save(values.toString(),
                currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), order);
        }
    }

    private void collectEventAboutConfirmWaste(Map<Integer, Integer> confirmed, Long languageId, Order order,
        Long orderId, int countOfChanges, StringBuilder values) {
        for (Map.Entry<Integer, Integer> entry : confirmed.entrySet()) {
            Integer capacity = bagRepository.findCapacityById(entry.getKey());
            StringBuilder bagTranslation = bagTranslationRepository.findNameByBagId(entry.getKey(), languageId);

            if (order.getOrderStatus() == OrderStatus.ADJUSTMENT
                || order.getOrderStatus() == OrderStatus.CONFIRMED
                || order.getOrderStatus() == OrderStatus.FORMED
                || order.getOrderStatus() == OrderStatus.NOT_TAKEN_OUT) {
                Long confirmWasteWas =
                    updateOrderRepository.getConfirmWaste(orderId, entry.getKey().longValue());
                if (nonNull(confirmWasteWas)
                    && !confirmWasteWas.equals(entry.getValue().longValue())) {
                    if (countOfChanges == 0) {
                        values.append(OrderHistory.CHANGE_ORDER_DETAILS + " ");
                    }
                    values.append(bagTranslation).append(" ").append(capacity).append(" л: ")
                        .append(confirmWasteWas)
                        .append(" шт на ").append(entry.getValue()).append(" шт.");
                }
            }
        }
    }

    private void collectEventAboutExportedWaste(Map<Integer, Integer> exported, Long languageId, Order order,
        Long orderId, int countOfChanges, StringBuilder values) {
        for (Map.Entry<Integer, Integer> entry : exported.entrySet()) {
            Integer capacity = bagRepository.findCapacityById(entry.getKey());
            StringBuilder bagTranslation = bagTranslationRepository.findNameByBagId(entry.getKey(), languageId);
            if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE
                || order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF
                || order.getOrderStatus() == OrderStatus.DONE
                || order.getOrderStatus() == OrderStatus.CANCELED) {
                Long exporterWasteWas = updateOrderRepository.getExporterWaste(orderId,
                    entry.getKey().longValue());
                if (!exporterWasteWas.equals(entry.getValue().longValue())) {
                    if (countOfChanges == 0) {
                        values.append(OrderHistory.CHANGE_ORDER_DETAILS + " ");
                        countOfChanges++;
                    }
                    values.append(bagTranslation).append(" ").append(capacity).append(" л: ")
                        .append(exporterWasteWas)
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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));

        double totalSumAmount = dto.getTotalSumAmount();
        double totalSumConfirmed = dto.getTotalSumConfirmed();
        double totalSumExported = dto.getTotalSumExported();

        updateOrderPaymentStatus(order, totalSumAmount, totalSumConfirmed, totalSumExported);
        return dto;
    }

    private CounterOrderDetailsDto getPriceDetails(Long id) {
        CounterOrderDetailsDto dto = new CounterOrderDetailsDto();
        Order order = orderRepository.getOrderDetails(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Bag> bag = bagRepository.findBagByOrderId(id);
        List<Certificate> currentCertificate = certificateRepository.findCertificate(id);

        double sumAmount = 0;
        double sumConfirmed = 0;
        double sumExported = 0;
        double totalSumAmount;
        double totalSumConfirmed;
        double totalSumExported;

        List<Integer> amountValues = new ArrayList<>(order.getAmountOfBagsOrdered().values());

        List<Integer> confirmedValues = new ArrayList<>(order.getConfirmedQuantity().values());

        List<Integer> exportedValues = new ArrayList<>(order.getExportedQuantity().values());

        for (int i = 0; i < bag.size(); i++) {
            sumAmount += amountValues.get(i) * bag.get(i).getFullPrice();
            if (!confirmedValues.isEmpty()) {
                sumConfirmed += confirmedValues.get(i) * bag.get(i).getFullPrice();
            }
            if (!exportedValues.isEmpty()) {
                sumExported += exportedValues.get(i) * bag.get(i).getFullPrice();
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
        if (confirmedValues.isEmpty()) {
            totalSumConfirmed = 0;
        }
        if (exportedValues.isEmpty()) {
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

        setDtoInfo(dto, sumAmount, sumExported, sumConfirmed, totalSumAmount, totalSumConfirmed, totalSumExported,
            order);
        return dto;
    }

    private void setDtoInfo(CounterOrderDetailsDto dto, double sumAmount, double sumExported, double sumConfirmed,
        double totalSumAmount, double totalSumConfirmed, double totalSumExported, Order order) {
        dto.setSumAmount(sumAmount);
        dto.setSumConfirmed(sumConfirmed);
        dto.setSumExported(sumExported);
        dto.setOrderComment(order.getComment());
        dto.setNumberOrderFromShop(order.getAdditionalOrders());
        dto.setBonus(order.getPointsToUse().doubleValue());
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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Payment> payment = paymentRepository.paymentInfo(id);
        if (payment.isEmpty()) {
            throw new PaymentNotFoundException(PAYMENT_NOT_FOUND + id);
        }
        return buildStatuses(order, payment.get(0));
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public OrderDetailStatusDto updateOrderDetailStatus(Long id, OrderDetailStatusRequestDto dto, String uuid) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Payment> payment = paymentRepository.paymentInfo(id);
        if (payment.isEmpty()) {
            throw new PaymentNotFoundException(PAYMENT_NOT_FOUND + id);
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
            } else if (order.getOrderStatus() == OrderStatus.CANCELED) {
                eventService.save(OrderHistory.ORDER_CANCELLED + "  " + order.getCancellationComment(),
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String orderDate = order.getOrderDate().toLocalDate().format(formatter);
        return OrderDetailStatusDto.builder()
            .orderStatus(order.getOrderStatus().name())
            .paymentStatus(payment.getPaymentStatus().name())
            .date(orderDate)
            .build();
    }

    private OrderDetailDto setOrderDetailDto(OrderDetailDto dto, Order order, String language) {
        dto.setAmount(modelMapper.map(order, new TypeToken<List<BagMappingDto>>() {
        }.getType()));

        dto.setCapacityAndPrice(bagRepository.findBagByOrderId(order.getId())
            .stream()
            .map(b -> modelMapper.map(b, BagInfoDto.class))
            .collect(Collectors.toList()));

        dto.setName(bagTranslationRepository.findAllByLanguageOrder(language, order.getId())
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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<ReceivingStation> receivingStation = receivingStationRepository.findAll();
        if (receivingStation.isEmpty()) {
            throw new ReceivingStationNotFoundException(RECEIVING_STATION_NOT_FOUND);
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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<ReceivingStation> receivingStation = receivingStationRepository.findAll();
        if (receivingStation.isEmpty()) {
            throw new ReceivingStationNotFoundException(RECEIVING_STATION_NOT_FOUND);
        }
        if (dto != null) {
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
            order.setReceivingStation(dto.getReceivingStation());
            orderRepository.save(order);
            final String receivingStationValue = order.getReceivingStation();
            final LocalDateTime deliverFrom = order.getDeliverFrom();
            collectEventsAboutOrderExportDetails(receivingStationValue, deliverFrom, order, currentUser);
        }
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

    private ExportDetailsDto buildExportDto(Order order, List<ReceivingStation> receivingStation) {
        return ExportDetailsDto.builder()
            .allReceivingStations(receivingStation.stream().map(ReceivingStation::getName).collect(Collectors.toList()))
            .dateExport(order.getDateOfExport() != null && order.getDeliverFrom() != null ? order.getDateOfExport()
                + "T" + order.getDeliverFrom().toLocalTime() : null)
            .timeDeliveryFrom(order.getDeliverFrom() != null ? order.getDeliverFrom().toString() : null)
            .timeDeliveryTo(order.getDeliverTo() != null ? order.getDeliverTo().toString() : null)
            .receivingStation(order.getReceivingStation())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdditionalBagInfoDto> getAdditionalBagsInfo(Long orderId) {
        User user = userRepository.findUserByOrderId(orderId)
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
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
        Long paymentSum = order.getPayment().stream()
            .filter(x -> x.getPaymentStatus().equals(PaymentStatus.PAID))
            .map(Payment::getAmount)
            .map(a -> a / 100)
            .reduce(Long::sum)
            .orElse(0L);
        return sumToPay - paymentSum < 0 ? Math.abs(paymentSum - sumToPay) : 0L;
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
            .map(Payment::getAmount).map(amount -> amount / 100).reduce(0L, (a, b) -> a + b);
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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));

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
            () -> new PaymentNotFoundException(PAYMENT_NOT_FOUND + paymentId));
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
        double totalAmount = dto.getTotalSumAmount();

        if (paymentsForCurrentOrder > 0 && totalAmount > paymentsForCurrentOrder) {
            order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
        } else if (paymentsForCurrentOrder > 0 && totalAmount >= paymentsForCurrentOrder) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
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
            .orElseThrow(() -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
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
        Map<PositionDto, List<String>> allPositionEmployee = new HashMap<>();
        for (Position position : positions) {
            PositionDto positionDto = PositionDto.builder().id(position.getId()).name(position.getName()).build();
            allPositionEmployee.put(positionDto, employeeRepository.getAllEmployeeByPositionId(position.getId())
                .stream().map(e -> e.getFirstName() + " " + e.getLastName()).collect(Collectors.toList()));
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
                () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + " " + dto.getOrderId()));
        List<EmployeeOrderPosition> employeeOrderPositions = new ArrayList<>();
        for (EmployeeOrderPositionDTO employeeOrderPositionDTO : dto.getEmployeeOrderPositionDTOS()) {
            String[] dtoFirstAndLastName = new String[0];
            try {
                dtoFirstAndLastName = employeeOrderPositionDTO.getName().split(" ");
            } catch (IndexOutOfBoundsException e) {
                throw new EmployeeNotFoundException(EMPLOYEE_DOESNT_EXIST);
            }
            Position position = positionRepository.findById(employeeOrderPositionDTO.getPositionId())
                .orElseThrow(() -> new PositionNotFoundException(POSITION_NOT_FOUND));
            Employee employee = employeeRepository.findByName(dtoFirstAndLastName[0], dtoFirstAndLastName[1])
                .orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND));
            Long oldEmployeePositionId =
                employeeOrderPositionRepository.findPositionOfEmployeeAssignedForOrder(employee.getId());
            if (nonNull(oldEmployeePositionId) && oldEmployeePositionId != 0 && oldEmployeePositionId != 2) {
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
        if (position == 1) {
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
            .orElseThrow(() -> new UnexistingOrderException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        List<String> pictures = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null) {
                pictures.add(fileService.upload(image));
            } else {
                pictures.add(defaultImagePath);
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
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + dto.getOrderId()));
        if (dto.getEmployeesList() != null) {
            for (int i = 0; i < dto.getEmployeesList().size(); i++) {
                AssignForOrderEmployee assignForOrderEmployee = dto.getEmployeesList().get(i);
                boolean isExistEmployee = employeeOrderPositionRepository.existsByOrderIdAndEmployeeId(dto.getOrderId(),
                    assignForOrderEmployee.getEmployeeId());
                if (isExistEmployee) {
                    throw new EmployeeAlreadyAssignedForOrder(
                        EMPLOYEE_ALREADY_ASSIGNED + assignForOrderEmployee.getEmployeeId());
                }
                Employee employeeForAssigning = employeeRepository.findById(assignForOrderEmployee.getEmployeeId())
                    .orElseThrow(() -> new EmployeeNotFoundException(
                        EMPLOYEE_NOT_FOUND + assignForOrderEmployee.getEmployeeId()));
                Long positionForEmployee =
                    employeeRepository.findPositionForEmployee(assignForOrderEmployee.getEmployeeId())
                        .orElseThrow(() -> new PositionNotFoundException(POSITION_NOT_FOUND));
                if (positionForEmployee != 2) {
                    EmployeeOrderPosition employeeOrderPositions = EmployeeOrderPosition.builder()
                        .order(order)
                        .employee(employeeForAssigning)
                        .position(Position.builder().id(positionForEmployee).build())
                        .build();
                    employeeOrderPositionRepository.save(employeeOrderPositions);
                    collectsEventsAboutAssigningEmployees(positionForEmployee, currentUser, order);
                } else {
                    throw new EmployeeIsNotAssigned(ErrorMessage.EMPLOYEE_IS_NOT_ASSIGN);
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
        if (position == 1) {
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

    private BigOrderTableDTO buildBigOrderTableDTO(Order order) {
        Address address = getUbsUserAddress(order);
        return BigOrderTableDTO.builder()
            .id(order.getId())
            .orderStatus(order.getOrderStatus().name())
            .orderPaymentStatus(order.getOrderPaymentStatus().name())
            .orderDate(getOrderDate(order))
            .paymentDate(getPaymentDate(order))
            .clientName(getClientName(order))
            .phoneNumber(getPhoneNumber(order))
            .email(getEmail(order))
            .senderName(getSenderName(order))
            .senderPhone(getSenderPhone(order))
            .senderEmail(getSenderEmail(order))
            .violationsAmount(getViolations(order))
            .region(getRegion(address))
            .settlement(geSettlement(address))
            .district(getDistrict(address))
            .address(getAddress(address))
            .commentToAddressForClient(getCommentToAddreaForClient(address))
            .bagsAmount(getBagsAmount(order))
            .totalOrderSum(getTotalOrderSum(order))
            .orderCertificateCode(getCertificateCode(order))
            .orderCertificatePoints(getCertificatePoints(order))
            .amountDue(getAmountDue(order))
            .commentForOrderByClient(order.getComment())
            .payment(getPayment(order))
            .dateOfExport(getDateOfExport(order))
            .timeOfExport(getTimeOfExport(order))
            .idOrderFromShop(getIdOrderFromShop(order))
            .receivingStation(order.getReceivingStation())
            .responsibleLogicMan(getEmployeeIdByIdPosition(order, 3L))
            .responsibleDriver(getEmployeeIdByIdPosition(order, 5L))
            .responsibleCaller(getEmployeeIdByIdPosition(order, 1L))
            .responsibleNavigator(getEmployeeIdByIdPosition(order, 4L))
            .commentsForOrder(getCommentsForOrder(order))
            .isBlocked(order.isBlocked())
            .blockedBy(getBlockedBy(order))
            .build();
    }

    private long getPaymentSum(Order order) {
        return nonNull(order.getPayment())
            ? order.getPayment().stream().mapToLong(Payment::getAmount).map(payment -> payment / 100).sum()
            : 0;
    }

    private int getCertificatesSum(Order order) {
        return nonNull(order.getCertificates())
            ? order.getCertificates().stream().mapToInt(Certificate::getPoints).sum()
            : 0;
    }

    private Address getUbsUserAddress(Order order) {
        if (nonNull(order.getUbsUser()) && nonNull(order.getUbsUser().getAddress())) {
            return order.getUbsUser().getAddress();
        }
        return new Address();
    }

    private String getClientName(Order order) {
        if (nonNull(order.getUbsUser())) {
            return nonNull(order.getUbsUser().getFirstName()) && nonNull(order.getUbsUser().getLastName())
                ? order.getUbsUser().getFirstName() + " " + order.getUbsUser().getLastName()
                : "-";
        }
        return "-";
    }

    private String getPhoneNumber(Order order) {
        return nonNull(order.getUbsUser()) ? order.getUbsUser().getPhoneNumber()
            : "-";
    }

    private String getEmail(Order order) {
        return nonNull(order.getUbsUser()) ? order.getUbsUser().getEmail()
            : "-";
    }

    private String getSenderName(Order order) {
        return nonNull(order.getUser())
            ? order.getUser().getRecipientName() + " " + order.getUser().getRecipientSurname()
            : "-";
    }

    private String getSenderPhone(Order order) {
        return nonNull(order.getUser()) ? order.getUser().getRecipientPhone()
            : "-";
    }

    private String getSenderEmail(Order order) {
        return nonNull(order.getUser()) ? order.getUser().getRecipientEmail()
            : "-";
    }

    private int getViolations(Order order) {
        return nonNull(order.getUser()) ? order.getUser().getViolations()
            : 0;
    }

    private String getRegion(Address address) {
        return nonNull(address.getRegion()) ? address.getRegion()
            : "-";
    }

    private String geSettlement(Address address) {
        return nonNull(address.getCity()) ? address.getCity()
            : "-";
    }

    private String getDistrict(Address address) {
        return nonNull(address.getDistrict()) ? address.getDistrict()
            : "-";
    }

    private String getCommentToAddreaForClient(Address address) {
        return nonNull(address.getAddressComment()) ? address.getAddressComment()
            : "-";
    }

    private String getOrderDate(Order order) {
        return nonNull(order.getOrderDate()) ? order.getOrderDate().toString()
            : "-";
    }

    private String getPaymentDate(Order order) {
        return nonNull(order.getPayment())
            ? order.getPayment().stream().map(Payment::getSettlementDate).collect(joining(", "))
            : "-";
    }

    private String getAddress(Address address) {
        if (nonNull(address.getStreet())
            && !address.getStreet().isBlank()) {
            StringBuilder addressInfo = new StringBuilder();
            addressInfo.append(address.getStreet());
            if (nonNull(address.getHouseNumber())
                && !address.getHouseNumber().isBlank()) {
                addressInfo.append(", " + address.getHouseNumber());
            }
            if (nonNull(address.getHouseCorpus())
                && !address.getHouseCorpus().isBlank()) {
                addressInfo.append(", " + address.getHouseCorpus());
            }
            if (nonNull(address.getEntranceNumber())
                && !address.getEntranceNumber().isBlank()) {
                addressInfo.append(", " + address.getEntranceNumber());
            }
            return addressInfo.toString();
        }
        return "-";
    }

    private Integer getBagsAmount(Order order) {
        return order.getAmountOfBagsOrdered().values().stream().reduce(0, Integer::sum);
    }

    private long getTotalOrderSum(Order order) {
        return nonNull(order.getSumTotalAmountWithoutDiscounts()) ? order.getSumTotalAmountWithoutDiscounts()
            : 0;
    }

    private String getCertificateCode(Order order) {
        return nonNull(order.getCertificates()) ? order.getCertificates().stream().map(Certificate::getCode)
            .collect(joining("; "))
            : "-";
    }

    private String getCertificatePoints(Order order) {
        return nonNull(order.getCertificates())
            ? order.getCertificates().stream().map(Certificate::getPoints).map(Objects::toString).collect(joining(", "))
            : "-";
    }

    private long getAmountDue(Order order) {
        return getTotalOrderSum(order) - (getPaymentSum(order) + getCertificatesSum(order) + order.getPointsToUse());
    }

    private String getDateOfExport(Order order) {
        return nonNull(order.getDeliverFrom())
            ? String.format(order.getDeliverFrom().toLocalDate().toString())
            : "-";
    }

    private String getTimeOfExport(Order order) {
        return nonNull(order.getDeliverFrom()) && nonNull(order.getDeliverTo())
            ? String.format("from %s to %s", order.getDeliverFrom().toLocalTime().toString(),
                order.getDeliverTo().toLocalTime().toString())
            : "-";
    }

    private String getPayment(Order order) {
        return nonNull(order.getPayment()) ? order.getPayment().stream()
            .map(Payment::getAmount)
            .map(amount -> amount / 100)
            .map(Objects::toString)
            .collect(joining(", "))
            : "-";
    }

    private String getIdOrderFromShop(Order order) {
        return nonNull(order.getAdditionalOrders()) ? order.getAdditionalOrders().stream().collect(joining(", "))
            : "-";
    }

    private String getEmployeeIdByIdPosition(Order order, Long idPosition) {
        return nonNull(order.getEmployeeOrderPositions()) ? order.getEmployeeOrderPositions().stream()
            .filter(employeeOrderPosition -> employeeOrderPosition.getPosition().getId().equals(idPosition))
            .map(EmployeeOrderPosition::getEmployee)
            .map(e -> e.getFirstName() + " " + e.getLastName())
            .reduce("", String::concat)
            : "-";
    }

    private String getCommentsForOrder(Order order) {
        return nonNull(order.getNote()) ? order.getNote()
            : "-";
    }

    private String getBlockedBy(Order order) {
        return nonNull(order.getBlockedByEmployee())
            ? String.format("%s %s", order.getBlockedByEmployee().getFirstName(),
                order.getBlockedByEmployee().getLastName())
            : "-";
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
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + adminCommentDto.getOrderId()));
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
            () -> new OrderNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
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
                        throw new IncorrectEcoNumberFormatException(INCORRECT_ECO_NUMBER);
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
                    lang,
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
            throw new UpdateAdminPageInfoException(e.getMessage());
        }
    }
}