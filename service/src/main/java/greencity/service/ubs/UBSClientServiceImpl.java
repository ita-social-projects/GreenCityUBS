package greencity.service.ubs;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.location.LocationSummaryDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.OrderAddress;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;

import greencity.client.FondyClient;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.AllActiveLocationsDto;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.LocationsDtos;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.RegionDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.AddressInfoDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagOrderDto;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.notification.SenderInfoDto;
import greencity.dto.order.EventDto;
import greencity.dto.order.FondyOrderResponse;
import greencity.dto.order.LiqPayOrderResponse;
import greencity.dto.order.MakeOrderAgainDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderClientDto;
import greencity.dto.order.OrderFondyClientDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderStatusPageDto;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.FondyPaymentResponse;
import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentRequestDtoLiqPay;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseDtoLiqPay;
import greencity.dto.payment.StatusRequestDtoLiqPay;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.PointsForUbsUserDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.enums.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.certificate.CertificateIsNotActivated;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.*;
import greencity.service.google.GoogleApiService;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.util.Bot;
import greencity.util.EncryptionUtil;
import greencity.util.OrderUtils;

import static greencity.constant.ErrorMessage.*;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

/**
 * Implementation of {@link UBSClientService}.
 */
@Service
@Data
public class UBSClientServiceImpl implements UBSClientService {
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSuserRepository ubsUserRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final AddressRepository addressRepo;
    private final OrderAddressRepository orderAddressRepository;
    private final LiqPayService liqPayService;
    private final UserRemoteClient userRemoteClient;
    private final FondyClient fondyClient;
    private final PaymentRepository paymentRepository;
    private final EncryptionUtil encryptionUtil;
    private final EventRepository eventRepository;
    private final OrdersForUserRepository ordersForUserRepository;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final GoogleApiService googleApiService;
    private final EventService eventService;
    private final LocationRepository locationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final RegionRepository regionRepository;
    @Lazy
    @Autowired
    private UBSManagementService ubsManagementService;
    @Value("${greencity.payment.fondy-payment-key}")
    private String fondyPaymentKey;
    @Value("${greencity.payment.merchant-id}")
    private String merchantId;
    @Value("${greencity.payment.liq-pay-public-key}")
    private String publicKey;
    @Value("${greencity.payment.liq-pay-private-key}")
    private String privateKey;
    @Value("${greencity.bots.viber-bot-uri}")
    private String viberBotUri;
    @Value("${greencity.bots.ubs-bot-name}")
    private String telegramBotName;
    @Value("${greencity.redirect.result-url-liq-pay}")
    private String resultUrlLiqpay;
    @Value("${greencity.redirect.result-url-fondy-personal-cabinet}")
    private String resultUrlForPersonalCabinetOfUser;
    @Value("${greencity.redirect.result-url-fondy}")
    private String resultUrlFondy;
    private static final Integer BAG_CAPACITY = 120;
    private static final String FAILED_STATUS = "failure";
    private static final String APPROVED_STATUS = "approved";
    private static final String TELEGRAM_PART_1_OF_LINK = "t.me/";
    private static final String VIBER_PART_1_OF_LINK = "viber://pa?chatURI=";
    private static final String VIBER_PART_3_OF_LINK = "&context=";
    private static final String TELEGRAM_PART_3_OF_LINK = "?start=";
    private static final Integer MAXIMUM_NUMBER_OF_ADDRESSES = 4;

    @Override
    @Transactional
    public void validatePayment(PaymentResponseDto dto) {
        Payment orderPayment = mapPayment(dto);
        String[] ids = dto.getOrder_id().split("_");
        Order order = orderRepository.findById(Long.valueOf(ids[0]))
            .orElseThrow(() -> new BadRequestException(PAYMENT_VALIDATION_ERROR));
        checkResponseStatusFailure(dto, orderPayment, order);
        checkResponseValidationSignature(dto);
        checkOrderStatusApproved(dto, orderPayment, order);
    }

    private Payment mapPayment(PaymentResponseDto dto) {
        if (dto.getFee() == null) {
            dto.setFee(0);
        }
        return Payment.builder()
            .id(Long.valueOf(dto.getOrder_id().substring(dto.getOrder_id().indexOf("_") + 1)))
            .currency(dto.getCurrency())
            .amount(Long.valueOf(dto.getAmount()))
            .orderStatus(dto.getOrder_status())
            .responseStatus(dto.getResponse_status())
            .senderCellPhone(dto.getSender_cell_phone())
            .senderAccount(dto.getSender_account())
            .maskedCard(dto.getMasked_card())
            .cardType(dto.getCard_type())
            .responseCode(dto.getResponse_code())
            .responseDescription(dto.getResponse_description())
            .orderTime(dto.getOrder_time())
            .settlementDate(parseFondySettlementDate(dto.getSettlement_date()))
            .fee(Long.valueOf(dto.getFee()))
            .paymentSystem(dto.getPayment_system())
            .senderEmail(dto.getSender_email())
            .paymentId(String.valueOf(dto.getPayment_id()))
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }

    private String parseFondySettlementDate(String settlementDate) {
        return settlementDate.isEmpty()
            ? LocalDate.now().toString()
            : LocalDate.parse(settlementDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPointsAndAllBagsDto getFirstPageData(String uuid, Optional<Long> locationId) {
        User user = userRepository.findByUuid(uuid);
        int currentUserPoints = user.getCurrentPoints();
        List<BagTranslationDto> btdList = bagRepository.findAll()
            .stream()
            .map(this::buildBagTranslationDto)
            .collect(Collectors.toList());

        if (locationId.isPresent()) {
            btdList = btdList.stream().filter(obj -> obj.getLocationId().equals(locationId.get()))
                .collect(Collectors.toList());
        }

        return new UserPointsAndAllBagsDto(btdList, currentUserPoints);
    }

    private BagTranslationDto buildBagTranslationDto(Bag bag) {
        return BagTranslationDto.builder()
            .id(bag.getId())
            .capacity(bag.getCapacity())
            .price(bag.getFullPrice())
            .name(bag.getName())
            .nameEng(bag.getNameEng())
            .locationId(bag.getLocation().getId())
            .limitedIncluded(bag.getLimitIncluded())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PersonalDataDto getSecondPageData(String uuid) {
        User currentUser = createUserByUuidIfUserDoesNotExist(uuid);
        List<UBSuser> ubsUser = ubsUserRepository.findUBSuserByUser(currentUser);
        if (ubsUser.isEmpty()) {
            ubsUser.add(UBSuser.builder().id(null).build());
        }
        PersonalDataDto dto = modelMapper.map(currentUser, PersonalDataDto.class);
        dto.setUbsUserId(ubsUser.get(0).getId());
        if (currentUser.getAlternateEmail() != null
            && !currentUser.getAlternateEmail().isEmpty()) {
            dto.setEmail(currentUser.getAlternateEmail());
        }
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CertificateDto checkCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code)
            .orElseThrow(() -> new NotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + code));

        return modelMapper.map(certificate, CertificateDto.class);
    }

    private void checkSumIfCourierLimitBySumOfOrder(TariffsInfo courierLocation, Integer sumWithoutDiscount) {
        if (CourierLimit.LIMIT_BY_SUM_OF_ORDER.equals(courierLocation.getCourierLimit())
            && sumWithoutDiscount < courierLocation.getMin()) {
            throw new BadRequestException(PRICE_OF_ORDER_LOWER_THAN_LIMIT + courierLocation.getMin());
        } else if (CourierLimit.LIMIT_BY_SUM_OF_ORDER.equals(courierLocation.getCourierLimit())
            && sumWithoutDiscount > courierLocation.getMax()) {
            throw new BadRequestException(
                ErrorMessage.PRICE_OF_ORDER_GREATER_THAN_LIMIT + courierLocation.getMax());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @Transactional
    public FondyOrderResponse saveFullOrderToDB(OrderResponseDto dto, String uuid, Long orderId) {
        User currentUser = userRepository.findByUuid(uuid);
        TariffsInfo tariffsInfo =
            tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(1L, dto.getLocationId())
                .orElseThrow(() -> new greencity.exceptions.NotFoundException("Tariff for courier with id " + 1L
                    + " and location with id " + dto.getLocationId() + " does not exist"));
        Map<Integer, Integer> amountOfBagsOrderedMap = new HashMap<>();

        if (!dto.isShouldBePaid()) {
            dto.setCertificates(Collections.emptySet());
            dto.setPointsToUse(0);
        }

        int sumToPayWithoutDiscount = formBagsToBeSavedAndCalculateOrderSum(amountOfBagsOrderedMap, dto.getBags(),
            tariffsInfo);
        checkSumIfCourierLimitBySumOfOrder(tariffsInfo, sumToPayWithoutDiscount);
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        int sumToPay = reduceOrderSumDueToUsedPoints(sumToPayWithoutDiscount, dto.getPointsToUse());

        Order order = isExistOrder(dto, orderId);
        order.setTariffsInfo(tariffsInfo);
        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPay);

        UBSuser userData;
        userData = formUserDataToBeSaved(dto.getPersonalData(), dto.getAddressId(), currentUser);

        getOrder(dto, currentUser, amountOfBagsOrderedMap, sumToPay, order, orderCertificates, userData);

        eventService.save(OrderHistory.ORDER_FORMED, OrderHistory.CLIENT, order);
        if (sumToPay <= 0 || !dto.isShouldBePaid()) {
            return getPaymentRequestDto(order, null);
        } else {
            PaymentRequestDto paymentRequestDto = formPaymentRequest(order.getId(), sumToPay);
            String link = getLinkFromFondyCheckoutResponse(fondyClient.getCheckoutResponse(paymentRequestDto));
            return getPaymentRequestDto(order, link);
        }
    }

    private Order isExistOrder(OrderResponseDto dto, Long orderId) {
        if (orderId != null) {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
            checkIsOrderPaid(order.getOrderPaymentStatus());
            order.setPointsToUse(dto.getPointsToUse())
                .setAdditionalOrders(dto.getAdditionalOrders())
                .setComment(dto.getOrderComment());
            return order;
        } else {
            return modelMapper.map(dto, Order.class);
        }
    }

    private FondyOrderResponse getPaymentRequestDto(Order order, String link) {
        return FondyOrderResponse.builder()
            .orderId(order.getId())
            .link(link)
            .build();
    }

    @Override
    public FondyPaymentResponse getPaymentResponseFromFondy(Long id, String uuid) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_PAYMENT_STATUS);
        }
        if (order.getPayment().isEmpty()) {
            throw new NotFoundException(PAYMENT_NOT_FOUND + id);
        }
        return getFondyPaymentResponse(order);
    }

    private FondyPaymentResponse getFondyPaymentResponse(Order order) {
        Payment payment = order.getPayment().get(order.getPayment().size() - 1);
        return FondyPaymentResponse.builder()
            .paymentStatus(payment.getResponseStatus())
            .build();
    }

    private void checkIfAddressHasBeenDeleted(Address address) {
        if (address.getAddressStatus().equals(AddressStatus.DELETED)) {
            throw new NotFoundException(
                ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkAddressUser(Address address, User user) {
        if (!address.getUser().equals(user)) {
            throw new NotFoundException(
                ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkIfUserHaveEnoughPoints(Integer i1, Integer i2) {
        if (i1 < i2) {
            throw new BadRequestException(USER_DONT_HAVE_ENOUGH_POINTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto findAllAddressesForCurrentOrder(String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        Long id = userRepository.findByUuid(uuid).getId();
        List<AddressDto> addressDtoList = addressRepo.findAllNonDeletedAddressesByUserId(id)
            .stream()
            .sorted(Comparator.comparing(Address::getId))
            .map(u -> modelMapper.map(u, AddressDto.class))
            .collect(Collectors.toList());
        return new OrderWithAddressesResponseDto(addressDtoList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto saveCurrentAddressForOrder(CreateAddressRequestDto addressRequestDto,
        String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        User currentUser = userRepository.findByUuid(uuid);
        List<Address> addresses = addressRepo.findAllNonDeletedAddressesByUserId(currentUser.getId());

        if (addresses.size() == MAXIMUM_NUMBER_OF_ADDRESSES) {
            throw new BadRequestException(ErrorMessage.NUMBER_OF_ADDRESSES_EXCEEDED);
        }

        OrderAddressDtoRequest dtoRequest =
            getLocationDto(googleApiService.getResultFromGeoCode(addressRequestDto.getSearchAddress()));
        OrderAddressDtoRequest addressRequestDtoForNullCheck =
            modelMapper.map(addressRequestDto, OrderAddressDtoRequest.class);
        addressRequestDtoForNullCheck.setId(0L);
        checkNullFieldsOnGoogleResponse(dtoRequest, addressRequestDtoForNullCheck);

        checkIfAddressExist(addresses, dtoRequest);

        addresses.forEach(addressItem -> {
            addressItem.setActual(false);
            addressRepo.save(addressItem);
        });

        Address address = modelMapper.map(dtoRequest, Address.class);

        address.setUser(currentUser);
        address.setActual(true);
        address.setAddressStatus(AddressStatus.NEW);
        addressRepo.save(address);

        return findAllAddressesForCurrentOrder(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto updateCurrentAddressForOrder(OrderAddressDtoRequest addressRequestDto,
        String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        User currentUser = userRepository.findByUuid(uuid);
        List<Address> addresses = addressRepo.findAllNonDeletedAddressesByUserId(currentUser.getId());

        OrderAddressDtoRequest dtoRequest;
        if (addressRequestDto.getSearchAddress() != null) {
            dtoRequest = getLocationDto(googleApiService.getResultFromGeoCode(addressRequestDto.getSearchAddress()));
            checkNullFieldsOnGoogleResponse(dtoRequest, addressRequestDto);
        } else {
            dtoRequest = addressRequestDto;
        }

        if (addresses != null) {
            checkIfAddressExist(addresses, dtoRequest);
        }

        Address address = addressRepo.findById(dtoRequest.getId())
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + dtoRequest.getId()));
        if (AddressStatus.DELETED.equals(address.getAddressStatus())) {
            address = null;
        }

        final AddressStatus addressStatus = address != null ? address.getAddressStatus() : null;
        final User addressUser = address != null ? address.getUser() : null;
        final Boolean addressActual = address != null ? address.getActual() : null;

        address = modelMapper.map(dtoRequest, Address.class);

        if (!currentUser.equals(addressUser)) {
            address.setId(null);
        }

        address.setUser(addressUser);
        address.setActual(true);
        address.setAddressStatus(addressStatus);
        address.setActual(addressActual);
        addressRepo.save(address);

        return findAllAddressesForCurrentOrder(uuid);
    }

    private void checkIfAddressExist(List<Address> addresses, OrderAddressDtoRequest dtoRequest) {
        boolean exist = addresses.stream()
            .map(address -> modelMapper.map(address, OrderAddressDtoRequest.class))
            .anyMatch(addressDto -> addressDto.equals(dtoRequest));

        if (exist) {
            throw new NotFoundException(ADDRESS_ALREADY_EXISTS);
        }
    }

    private Map<AddressComponentType, Consumer<String>> initializeUkrainianGeoCodingResult(
        OrderAddressDtoRequest dtoRequest) {
        return Map.of(
            AddressComponentType.LOCALITY, dtoRequest::setCity,
            AddressComponentType.ROUTE, dtoRequest::setStreet,
            AddressComponentType.STREET_NUMBER, dtoRequest::setHouseNumber,
            AddressComponentType.SUBLOCALITY, dtoRequest::setDistrict,
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1, dtoRequest::setRegion);
    }

    private Map<AddressComponentType, Consumer<String>> initializeEnglishGeoCodingResult(
        OrderAddressDtoRequest dtoRequest) {
        return Map.of(
            AddressComponentType.LOCALITY, dtoRequest::setCityEn,
            AddressComponentType.ROUTE, dtoRequest::setStreetEn,
            AddressComponentType.SUBLOCALITY, dtoRequest::setDistrictEn,
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1, dtoRequest::setRegionEn);
    }

    private void initializeGeoCodingResults(Map<AddressComponentType, Consumer<String>> initializedMap,
        GeocodingResult geocodingResult) {
        initializedMap
            .forEach((key, value) -> Arrays.stream(geocodingResult.addressComponents)
                .forEach(addressComponent -> Arrays.stream(addressComponent.types)
                    .filter(componentType -> componentType.equals(key))
                    .forEach(componentType -> value.accept(addressComponent.longName))));
    }

    private OrderAddressDtoRequest getLocationDto(List<GeocodingResult> geocodingResults) {
        OrderAddressDtoRequest orderAddressDtoRequest = new OrderAddressDtoRequest();
        initializeGeoCodingResults(initializeUkrainianGeoCodingResult(orderAddressDtoRequest), geocodingResults.get(0));
        initializeGeoCodingResults(initializeEnglishGeoCodingResult(orderAddressDtoRequest), geocodingResults.get(1));

        double latitude = geocodingResults.get(0).geometry.location.lat;
        double longitude = geocodingResults.get(0).geometry.location.lng;
        orderAddressDtoRequest.setCoordinates(new Coordinates(latitude, longitude));

        return orderAddressDtoRequest;
    }

    private void checkNullFieldsOnGoogleResponse(OrderAddressDtoRequest dtoRequest,
        OrderAddressDtoRequest addressRequestDto) {
        dtoRequest.setRegion(
            Objects.isNull(dtoRequest.getRegion()) ? addressRequestDto.getRegion() : dtoRequest.getRegion());

        dtoRequest.setRegionEn(
            Objects.isNull(dtoRequest.getRegionEn()) ? addressRequestDto.getRegionEn() : dtoRequest.getRegionEn());

        dtoRequest.setDistrict(
            Objects.isNull(dtoRequest.getDistrict()) ? addressRequestDto.getDistrict() : dtoRequest.getDistrict());

        dtoRequest.setDistrictEn(
            Objects.isNull(dtoRequest.getDistrictEn()) ? addressRequestDto.getDistrictEn()
                : dtoRequest.getDistrictEn());

        dtoRequest.setHouseNumber(
            Objects.isNull(dtoRequest.getHouseNumber()) ? addressRequestDto.getHouseNumber()
                : dtoRequest.getHouseNumber());

        dtoRequest.setEntranceNumber(addressRequestDto.getEntranceNumber());
        dtoRequest.setHouseCorpus(addressRequestDto.getHouseCorpus());
        dtoRequest.setAddressComment(addressRequestDto.getAddressComment());
        dtoRequest.setId(addressRequestDto.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto deleteCurrentAddressForOrder(Long addressId, String uuid) {
        Address address = addressRepo.findById(addressId).orElseThrow(
            () -> new NotFoundException(ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));
        if (AddressStatus.DELETED.equals(address.getAddressStatus())) {
            throw new NotFoundException(ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId);
        }
        if (!address.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_DELETE_ADDRESS);
        }
        address.setAddressStatus(AddressStatus.DELETED);
        address.setActual(false);
        addressRepo.save(address);
        return findAllAddressesForCurrentOrder(uuid);
    }

    private void formAndSaveUser(User currentUser, int pointsToUse, Order order) {
        currentUser.getOrders().add(order);
        if (pointsToUse != 0) {
            currentUser.setCurrentPoints(currentUser.getCurrentPoints() - pointsToUse);
            currentUser.getChangeOfPointsList().add(ChangeOfPoints.builder()
                .amount(-pointsToUse)
                .date(order.getOrderDate())
                .user(currentUser)
                .order(order)
                .build());
        }
        userRepository.save(currentUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderClientDto> getAllOrdersDoneByUser(String uuid) {
        return orderRepository.getAllOrdersOfUser(uuid).stream()
            .sorted(Comparator.comparing(Order::getOrderDate))
            .map(order -> modelMapper.map(order, OrderClientDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MakeOrderAgainDto makeOrderAgain(Locale locale, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE
            || order.getOrderStatus() == OrderStatus.CONFIRMED
            || order.getOrderStatus() == OrderStatus.DONE) {
            List<Bag> bags = bagRepository.findAllByOrder(orderId);
            return buildOrderBagDto(order, bags);
        } else {
            throw new BadRequestException(ErrorMessage.BAD_ORDER_STATUS_REQUEST + order.getOrderStatus());
        }
    }

    /**
     * {@inheritDoc}
     */

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

    /**
     * {@inheritDoc}
     */

    @Override
    public OrdersDataForUserDto getOrderForUser(String uuid, Long id) {
        Order order = ordersForUserRepository.getAllByUserUuidAndId(uuid, id);
        if (order == null) {
            throw new NotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }

        return getOrdersData(order);
    }

    private OrdersDataForUserDto getOrdersData(Order order) {
        List<Payment> payments = order.getPayment();
        List<BagForUserDto> bagForUserDtos = bagForUserDtosBuilder(order);
        OrderStatusTranslation orderStatusTranslation = orderStatusTranslationRepository
            .getOrderStatusTranslationById((long) order.getOrderStatus().getNumValue())
            .orElse(orderStatusTranslationRepository.getOne(1L));
        OrderPaymentStatusTranslation paymentStatusTranslation = orderPaymentStatusTranslationRepository
            .getById(
                (long) order.getOrderPaymentStatus().getStatusValue());

        Double fullPrice = Double.valueOf(bagForUserDtos.stream()
            .map(BagForUserDto::getTotalPrice)
            .reduce(0, Integer::sum));

        List<CertificateDto> certificateDtos = order.getCertificates().stream()
            .map(certificate -> modelMapper.map(certificate, CertificateDto.class))
            .collect(Collectors.toList());

        Double amountBeforePayment =
            fullPrice - order.getPointsToUse() - countPaidAmount(payments) - countCertificatesBonuses(certificateDtos);

        return OrdersDataForUserDto.builder()
            .id(order.getId())
            .dateForm(order.getOrderDate())
            .datePaid(order.getOrderDate())
            .orderStatus(orderStatusTranslation.getName())
            .orderStatusEng(orderStatusTranslation.getNameEng())
            .orderComment(order.getComment())
            .bags(bagForUserDtos)
            .additionalOrders(order.getAdditionalOrders())
            .amountBeforePayment(amountBeforePayment)
            .paidAmount(countPaidAmount(payments).doubleValue())
            .orderFullPrice(fullPrice)
            .certificate(certificateDtos)
            .bonuses(order.getPointsToUse().doubleValue())
            .sender(senderInfoDtoBuilder(order))
            .address(addressInfoDtoBuilder(order))
            .paymentStatus(paymentStatusTranslation.getTranslationValue())
            .paymentStatusEng(paymentStatusTranslation.getTranslationsValueEng())
            .build();
    }

    private Integer countCertificatesBonuses(List<CertificateDto> certificateDtos) {
        return certificateDtos.stream()
            .map(CertificateDto::getPoints)
            .reduce(0, Integer::sum);
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
        var address = order.getUbsUser().getAddress();
        return AddressInfoDto.builder()
            .addressCity(address.getCity())
            .addressCityEng(address.getCityEn())
            .addressComment(address.getAddressComment())
            .addressDistinct(address.getDistrict())
            .addressDistinctEng(address.getDistrictEn())
            .addressRegion(address.getRegion())
            .addressRegionEng(address.getRegionEn())
            .addressStreet(address.getStreet())
            .addressStreetEng(address.getStreetEn())
            .houseCorpus(address.getHouseCorpus())
            .houseNumber(address.getHouseNumber())
            .entranceNumber(address.getEntranceNumber())
            .build();
    }

    private List<BagForUserDto> bagForUserDtosBuilder(Order order) {
        Map<Integer, Integer> actualBagAmounts = getActualBagAmountsForOrder(order);
        List<Bag> bagsForOrder = bagRepository.findBagsByOrderId(order.getId());
        return bagsForOrder.stream()
            .filter(bag -> actualBagAmounts.containsKey(bag.getId()))
            .map(bag -> buildBagForUserDto(bag, actualBagAmounts.get(bag.getId())))
            .collect(Collectors.toList());
    }

    private Map<Integer, Integer> getActualBagAmountsForOrder(Order order) {
        if (MapUtils.isNotEmpty(order.getExportedQuantity())) {
            return order.getExportedQuantity();
        }
        if (MapUtils.isNotEmpty(order.getConfirmedQuantity())) {
            return order.getConfirmedQuantity();
        }
        if (MapUtils.isNotEmpty(order.getAmountOfBagsOrdered())) {
            return order.getAmountOfBagsOrdered();
        }
        return new HashMap<>();
    }

    private BagForUserDto buildBagForUserDto(Bag bag, int count) {
        BagForUserDto bagDto = modelMapper.map(bag, BagForUserDto.class);
        bagDto.setService(bag.getName());
        bagDto.setServiceEng(bag.getNameEng());
        bagDto.setCount(count);
        bagDto.setTotalPrice(count * bag.getFullPrice());
        return bagDto;
    }

    private Long countPaidAmount(List<Payment> payments) {
        return payments.stream()
            .filter(payment -> PaymentStatus.PAID.equals(payment.getPaymentStatus()))
            .map(Payment::getAmount)
            .map(amount -> amount / 100)
            .reduce(0L, Long::sum);
    }

    private MakeOrderAgainDto buildOrderBagDto(Order order, List<Bag> bags) {
        List<BagOrderDto> bagOrderDtoList = new ArrayList<>();
        for (Bag bag : bags) {
            bagOrderDtoList.add(BagOrderDto.builder()
                .bagId(bag.getId())
                .name(bag.getName())
                .nameEng(bag.getNameEng())
                .capacity(bag.getCapacity())
                .price(bag.getPrice())
                .bagAmount(order.getAmountOfBagsOrdered().get(bag.getId()))
                .build());
        }
        return MakeOrderAgainDto.builder()
            .orderId(order.getId())
            .orderAmount(order.getPayment().stream()
                .flatMapToLong(p -> LongStream.of(p.getAmount()))
                .reduce(Long::sum).orElse(0L))
            .bagOrderDtoList(bagOrderDtoList)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId, String uuid) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        User user = userRepository.findByUuid(uuid);
        if (!order.getUser().equals(user)) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }
        UserInfoDto userInfoDto = UserInfoDto.builder()
            .customerName(order.getUser().getRecipientName())
            .customerSurName(order.getUser().getRecipientSurname())
            .customerPhoneNumber(order.getUser().getRecipientPhone())
            .customerEmail(order.getUser().getRecipientEmail())
            .totalUserViolations(userRepository.countTotalUsersViolations(order.getUser().getId()))
            .recipientId(order.getUbsUser().getId())
            .userViolationForCurrentOrder(
                userRepository.checkIfUserHasViolationForCurrentOrder(order.getUser().getId(), order.getId()))
            .build();
        if (order.getUbsUser().getSenderFirstName() != null && !order.getUbsUser().getSenderFirstName().isEmpty()
            && order.getUbsUser().getSenderLastName() != null && !order.getUbsUser().getSenderLastName().isEmpty()
            && order.getUbsUser().getSenderPhoneNumber() != null
            && !order.getUbsUser().getSenderPhoneNumber().isEmpty()) {
            return userInfoDto.setRecipientName(order.getUbsUser().getSenderFirstName())
                .setRecipientSurName(order.getUbsUser().getSenderLastName())
                .setRecipientEmail(order.getUbsUser().getSenderEmail())
                .setRecipientPhoneNumber(order.getUbsUser().getSenderPhoneNumber());
        } else {
            return userInfoDto.setRecipientName(order.getUbsUser().getFirstName())
                .setRecipientSurName(order.getUbsUser().getLastName())
                .setRecipientEmail(order.getUbsUser().getEmail())
                .setRecipientPhoneNumber(order.getUbsUser().getPhoneNumber());
        }
    }

    /**
     * Method updates ubs_user information order in order.
     *
     * @param dtoUpdate of {@link UbsCustomersDtoUpdate} ubs_user_id;
     * @return {@link UbsCustomersDto};
     * @author Rusanovscaia Nadejda
     */
    @Override
    public UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String email) {
        Optional<UBSuser> optionalUbsUser = ubsUserRepository.findById(dtoUpdate.getRecipientId());
        if (optionalUbsUser.isEmpty()) {
            throw new UBSuserNotFoundException(RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST + dtoUpdate.getRecipientId());
        }
        UBSuser user = optionalUbsUser.get();
        ubsUserRepository.save(updateRecipientDataInOrder(user, dtoUpdate));
        eventService.saveEvent(OrderHistory.CHANGED_SENDER, email, optionalUbsUser.get().getOrders().get(0));
        return UbsCustomersDto.builder()
            .name(user.getFirstName() + " " + user.getLastName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

    private UBSuser updateRecipientDataInOrder(UBSuser ubSuser, UbsCustomersDtoUpdate dto) {
        if (nonNull(dto.getRecipientEmail())) {
            ubSuser.setEmail(dto.getRecipientEmail());
        }
        if (nonNull(dto.getRecipientName())) {
            ubSuser.setFirstName(dto.getRecipientName());
        }
        if (nonNull(dto.getRecipientSurName())) {
            ubSuser.setLastName(dto.getRecipientSurName());
        }
        if (nonNull(dto.getRecipientPhoneNumber())) {
            ubSuser.setPhoneNumber(dto.getRecipientPhoneNumber());
        }

        return ubSuser;
    }

    private Order formAndSaveOrder(Order order, Set<Certificate> orderCertificates,
        Map<Integer, Integer> amountOfBagsOrderedMap, UBSuser userData,
        User currentUser, int sumToPay) {
        order.setOrderStatus(OrderStatus.FORMED);
        order.setCertificates(orderCertificates);
        order.setAmountOfBagsOrdered(amountOfBagsOrderedMap);
        order.setUbsUser(userData);
        order.setUser(currentUser);
        order.setSumTotalAmountWithoutDiscounts(
            (long) formBagsToBeSavedAndCalculateOrderSumClient(amountOfBagsOrderedMap));
        setOrderPaymentStatus(order, sumToPay);

        Payment payment = Payment.builder()
            .amount(sumToPay * 100L)
            .orderStatus("created")
            .currency("UAH")
            .paymentStatus(PaymentStatus.UNPAID)
            .settlementDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .order(order).build();

        if (order.getPayment() == null) {
            order.setPayment(new ArrayList<>());
        }
        order.getPayment().add(payment);

        orderRepository.save(order);
        return order;
    }

    private void setOrderPaymentStatus(Order order, int sumToPay) {
        if (sumToPay <= 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        } else {
            order.setOrderPaymentStatus(
                order.getPointsToUse() > 0 || CollectionUtils.isNotEmpty(order.getCertificates())
                    ? OrderPaymentStatus.HALF_PAID
                    : OrderPaymentStatus.UNPAID);
        }
    }

    private PaymentRequestDto formPaymentRequest(Long orderId, int sumToPay) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
            .merchantId(Integer.parseInt(merchantId))
            .orderId(orderId + "_"
                + order.getPayment().get(order.getPayment().size() - 1).getId().toString())
            .orderDescription("ubs courier")
            .currency("UAH")
            .amount(sumToPay * 100)
            .responseUrl(resultUrlFondy)
            .build();

        paymentRequestDto.setSignature(encryptionUtil
            .formRequestSignature(paymentRequestDto, fondyPaymentKey, merchantId));

        return paymentRequestDto;
    }

    private UBSuser formUserDataToBeSaved(PersonalDataDto dto, Long addressId, User currentUser) {
        UBSuser ubsUserFromDatabaseById = null;
        if (dto.getUbsUserId() != null) {
            ubsUserFromDatabaseById =
                ubsUserRepository.findById(dto.getUbsUserId())
                    .orElseThrow(() -> new BadRequestException(THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST
                        + dto.getUbsUserId()));
        }
        UBSuser mappedFromDtoUser = modelMapper.map(dto, UBSuser.class);
        mappedFromDtoUser.setUser(currentUser);
        mappedFromDtoUser.setPhoneNumber(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(mappedFromDtoUser.getPhoneNumber()));
        if (mappedFromDtoUser.getId() == null || !mappedFromDtoUser.equals(ubsUserFromDatabaseById)) {
            mappedFromDtoUser.setId(null);
            mappedFromDtoUser.setAddress(getSavedOrderAddress(addressId, currentUser));
            if (mappedFromDtoUser.getAddress().getAddressComment() == null) {
                mappedFromDtoUser.getAddress().setAddressComment(dto.getAddressComment());
            }
            ubsUserRepository.save(mappedFromDtoUser);
            currentUser.getUbsUsers().add(mappedFromDtoUser);

            currentUser.setRecipientSurname(dto.getLastName());
            currentUser.setRecipientName(dto.getFirstName());
            currentUser.setRecipientPhone(dto.getPhoneNumber());

            userRepository.save(currentUser);
            return mappedFromDtoUser;
        } else {
            return ubsUserFromDatabaseById;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderPaymentDetailDto getOrderPaymentDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return buildOrderPaymentDetailDto(order);
    }

    private OrderPaymentDetailDto buildOrderPaymentDetailDto(Order order) {
        int certificatePoints = order.getCertificates().stream()
            .flatMapToInt(c -> IntStream.of(c.getPoints()))
            .reduce(Integer::sum).orElse(0) * 100;
        int pointsToUse = order.getPointsToUse() * 100;
        long amount = order.getPayment().stream()
            .flatMapToLong(p -> LongStream.of(p.getAmount()))
            .reduce(Long::sum).orElse(0);
        String currency = order.getPayment().isEmpty() ? "UAH" : order.getPayment().get(0).getCurrency();
        return OrderPaymentDetailDto.builder()
            .amount(amount != 0 ? amount + certificatePoints + pointsToUse : 0)
            .certificates(-certificatePoints)
            .pointsToUse(-pointsToUse)
            .amountToPay(amount)
            .currency(currency)
            .build();
    }

    private int formCertificatesToBeSavedAndCalculateOrderSum(OrderResponseDto dto, Set<Certificate> orderCertificates,
        Order order, int sumToPay) {
        if (sumToPay != 0 && dto.getCertificates() != null) {
            for (String temp : dto.getCertificates()) {
                if (dto.getCertificates().size() > 5) {
                    throw new BadRequestException(TOO_MANY_CERTIFICATES);
                }
                Certificate certificate = certificateRepository.findById(temp).orElseThrow(
                    () -> new NotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + temp));
                validateCertificate(certificate);
                certificate.setOrder(order);
                orderCertificates.add(certificate);
                sumToPay -= certificate.getPoints();
                certificate.setCertificateStatus(CertificateStatus.USED);
                certificate.setDateOfUse(LocalDate.now());
                if (dontSendLinkToFondyIf(sumToPay, certificate)) {
                    sumToPay = 0;
                }
            }
        }
        return sumToPay;
    }

    private boolean dontSendLinkToFondyIf(int sumToPay, Certificate certificate) {
        if (sumToPay <= 0) {
            certificate.setCertificateStatus(CertificateStatus.USED);
            certificate.setPoints(certificate.getPoints() + sumToPay);
            return true;
        }
        return false;
    }

    private void checkAmountOfBagsIfCourierLimitByAmountOfBag(TariffsInfo courierLocation, Integer countOfBigBag) {
        if (CourierLimit.LIMIT_BY_AMOUNT_OF_BAG.equals(courierLocation.getCourierLimit())
            && courierLocation.getMin() > countOfBigBag) {
            throw new BadRequestException(
                NOT_ENOUGH_BIG_BAGS_EXCEPTION + courierLocation.getMin());
        } else if (CourierLimit.LIMIT_BY_AMOUNT_OF_BAG.equals(courierLocation.getCourierLimit())
            && courierLocation.getMax() < countOfBigBag) {
            throw new BadRequestException(TO_MUCH_BIG_BAG_EXCEPTION + courierLocation.getMax());
        }
    }

    private int formBagsToBeSavedAndCalculateOrderSumClient(
        Map<Integer, Integer> getOrderBagsAndQuantity) {
        int sumToPay = 0;

        for (Map.Entry<Integer, Integer> temp : getOrderBagsAndQuantity.entrySet()) {
            Integer amount = getOrderBagsAndQuantity.get(temp.getKey());
            Bag bag = bagRepository.findById(temp.getKey())
                .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + temp.getKey()));
            sumToPay += bag.getFullPrice() * amount;
        }
        return sumToPay;
    }

    private int formBagsToBeSavedAndCalculateOrderSum(
        Map<Integer, Integer> map, List<BagDto> bags, TariffsInfo courierLocation) {
        int sumToPay = 0;
        int bigBagCounter = 0;

        for (BagDto temp : bags) {
            Bag bag = bagRepository.findById(temp.getId())
                .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + temp.getId()));
            if (bag.getCapacity() >= BAG_CAPACITY) {
                bigBagCounter += temp.getAmount();
            }
            sumToPay += bag.getFullPrice() * temp.getAmount();
            map.put(temp.getId(), temp.getAmount());
        }

        checkAmountOfBagsIfCourierLimitByAmountOfBag(courierLocation, bigBagCounter);
        return sumToPay;
    }

    private void validateCertificate(Certificate certificate) {
        if (certificate.getCertificateStatus() == CertificateStatus.NEW) {
            throw new CertificateIsNotActivated(CERTIFICATE_IS_NOT_ACTIVATED + certificate.getCode());
        } else if (certificate.getCertificateStatus() == CertificateStatus.USED) {
            throw new BadRequestException(CERTIFICATE_IS_USED + certificate.getCode());
        } else {
            if (LocalDate.now().isAfter(certificate.getExpirationDate())) {
                throw new BadRequestException(CERTIFICATE_EXPIRED + certificate.getCode());
            }
        }
    }

    @Override
    public AllPointsUserDto findAllCurrentPointsForUser(String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Integer userBonuses = currentUser.getCurrentPoints();
        if (userBonuses == null) {
            userBonuses = 0;
        }
        List<ChangeOfPoints> changeOfPointsList = currentUser.getChangeOfPointsList();
        List<PointsForUbsUserDto> bonusForUbsUser = new ArrayList<>();
        if (nonNull(changeOfPointsList)) {
            bonusForUbsUser = changeOfPointsList.stream()
                .sorted(Comparator.comparing(ChangeOfPoints::getDate).reversed())
                .map(m -> modelMapper.map(m, PointsForUbsUserDto.class))
                .collect(Collectors.toList());
        }
        AllPointsUserDto allBonusesForUserDto = new AllPointsUserDto();
        allBonusesForUserDto.setUserBonuses(userBonuses);
        allBonusesForUserDto.setUbsUserBonuses(bonusForUbsUser);
        return allBonusesForUserDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EventDto> getAllEventsForOrder(Long orderId, String email) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }
        List<Event> orderEvents = eventRepository.findAllEventsByOrderId(orderId);
        if (orderEvents.isEmpty()) {
            throw new NotFoundException(ErrorMessage.EVENTS_NOT_FOUND_EXCEPTION + orderId);
        }
        return orderEvents
            .stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfileUpdateDto updateProfileData(String uuid, UserProfileUpdateDto userProfileUpdateDto) {
        createUserByUuidIfUserDoesNotExist(uuid);
        User user = userRepository.findByUuid(uuid);
        setUserData(user, userProfileUpdateDto);
        List<Address> addressList =
            userProfileUpdateDto.getAddressDto().stream().map(a -> modelMapper.map(a, Address.class))
                .collect(Collectors.toList());

        for (Address address : addressList) {
            address.setUser(user);
        }
        user.setAddresses(addressList);
        User savedUser = userRepository.save(user);
        List<AddressDto> mapperAddressDto =
            addressList.stream().map(a -> modelMapper.map(a, AddressDto.class)).collect(Collectors.toList());
        UserProfileUpdateDto mappedUserProfileDto = modelMapper.map(savedUser, UserProfileUpdateDto.class);
        UserProfileUpdateDto.builder().addressDto(mapperAddressDto).build();
        return mappedUserProfileDto;
    }

    @Override
    public UserProfileDto getProfileData(String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        User user = userRepository.findByUuid(uuid);
        List<Address> allAddress = addressRepo.findAllNonDeletedAddressesByUserId(user.getId());
        UserProfileDto userProfileDto = modelMapper.map(user, UserProfileDto.class);
        List<Bot> botList = getListOfBots(user.getUuid());
        List<AddressDto> addressDto =
            allAddress.stream()
                .map(a -> modelMapper.map(a, AddressDto.class))
                .collect(Collectors.toList());
        userProfileDto.setAddressDto(addressDto);
        userProfileDto.setBotList(botList);
        userProfileDto.setHasPassword(userRemoteClient.getPasswordStatus().isHasPassword());
        return userProfileDto;
    }

    private User setUserData(User user, UserProfileUpdateDto userProfileUpdateDto) {
        user.setRecipientName(userProfileUpdateDto.getRecipientName());
        user.setRecipientSurname(userProfileUpdateDto.getRecipientSurname());
        user.setAlternateEmail(userProfileUpdateDto.getAlternateEmail());
        user.setRecipientPhone(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(userProfileUpdateDto.getRecipientPhone()));
        return user;
    }

    private User createUserByUuidIfUserDoesNotExist(String uuid) {
        User user = userRepository.findByUuid(uuid);
        if (user == null) {
            UbsCustomersDto ubsCustomersDto = userRemoteClient.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Such UUID have not been found"));
            return userRepository.save(User.builder().currentPoints(0).violations(0).uuid(uuid)
                .recipientEmail(ubsCustomersDto.getEmail()).recipientName("")
                .dateOfRegistration(LocalDate.now()).build());
        }
        return user;
    }

    @Override
    public void markUserAsDeactivated(Long id) {
        User user =
            userRepository.findById(id).orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        userRemoteClient.markUserDeactivated(user.getUuid());
    }

    @Override
    public OrderCancellationReasonDto getOrderCancellationReason(final Long orderId, String uuid) {
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
    public OrderCancellationReasonDto updateOrderCancellationReason(
        long id, OrderCancellationReasonDto dto, String uuid) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_ORDER_CANCELLATION_REASON);
        }
        order.setCancellationReason(dto.getCancellationReason());
        order.setCancellationComment(dto.getCancellationComment());
        order.setId(id);
        orderRepository.save(order);
        return dto;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @Transactional
    public LiqPayOrderResponse saveFullOrderToDBFromLiqPay(OrderResponseDto dto, String uuid, Long orderId) {
        User currentUser = userRepository.findByUuid(uuid);
        TariffsInfo tariffsInfo =
            tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(1L, dto.getLocationId())
                .orElseThrow(() -> new NotFoundException(
                    ErrorMessage.TARIFF_FOR_LOCATION_NOT_EXIST + dto.getLocationId()));
        Map<Integer, Integer> amountOfBagsOrderedMap = new HashMap<>();

        int sumToPayWithoutDiscount = formBagsToBeSavedAndCalculateOrderSum(amountOfBagsOrderedMap, dto.getBags(),
            tariffsInfo);
        checkSumIfCourierLimitBySumOfOrder(tariffsInfo, sumToPayWithoutDiscount);
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        int sumToPay = reduceOrderSumDueToUsedPoints(sumToPayWithoutDiscount, dto.getPointsToUse());

        Order order = isExistOrder(dto, orderId);
        order.setTariffsInfo(tariffsInfo);
        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPay);

        final UBSuser userData = formUserDataToBeSaved(dto.getPersonalData(), dto.getAddressId(), currentUser);

        getOrder(dto, currentUser, amountOfBagsOrderedMap, sumToPay, order, orderCertificates, userData);

        eventService.save(OrderHistory.ORDER_FORMED, OrderHistory.CLIENT, order);
        if (sumToPay == 0 || !dto.isShouldBePaid()) {
            return buildOrderResponseWithoutButton(order);
        } else {
            PaymentRequestDtoLiqPay paymentRequestDto = formLiqPayPaymentRequest(order.getId(), sumToPay);
            String liqPayData = liqPayService.getCheckoutResponse(paymentRequestDto);
            return buildOrderResponse(order, liqPayData
                .replace("\"", "")
                .replace("\n", ""));
        }
    }

    private int reduceOrderSumDueToUsedPoints(int sumToPay, int pointsToUse) {
        if (sumToPay >= pointsToUse) {
            sumToPay -= pointsToUse;
        }
        return sumToPay;
    }

    private void getOrder(OrderResponseDto dto, User currentUser, Map<Integer, Integer> amountOfBagsOrderedMap,
        int sumToPay, Order order, Set<Certificate> orderCertificates, UBSuser userData) {
        formAndSaveOrder(order, orderCertificates, amountOfBagsOrderedMap, userData, currentUser, sumToPay);

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);
    }

    private OrderAddress getSavedOrderAddress(Long addressId, User currentUser) {
        Address address = addressRepo.findById(addressId).orElseThrow(() -> new NotFoundException(
            ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));

        checkIfAddressHasBeenDeleted(address);

        checkAddressUser(address, currentUser);

        OrderAddress orderAddress = modelMapper.map(address, OrderAddress.class);

        orderAddressRepository.save(orderAddress);

        return orderAddress;
    }

    private LiqPayOrderResponse buildOrderResponse(Order order, String button) {
        return LiqPayOrderResponse.builder()
            .orderId(order.getId())
            .liqPayButton(button)
            .build();
    }

    private LiqPayOrderResponse buildOrderResponseWithoutButton(Order order) {
        return LiqPayOrderResponse.builder()
            .orderId(order.getId())
            .build();
    }

    private PaymentRequestDtoLiqPay formLiqPayPaymentRequest(Long orderId, int sumToPay) {
        Order order = orderRepository.findById(orderId).orElseThrow(null);

        return PaymentRequestDtoLiqPay.builder()
            .publicKey(publicKey)
            .version(3)
            .action("pay")
            .amount(sumToPay)
            .currency("UAH")
            .description("ubs courier")
            .orderId(orderId + "_" + order.getPayment()
                .get(order.getPayment().size() - 1).getId().toString())
            .language("en")
            .paytypes("card")
            .resultUrl(resultUrlLiqpay)
            .build();
    }

    @Override
    public void validateLiqPayPayment(PaymentResponseDtoLiqPay dto) {
        if (!encryptionUtil.formingResponseSignatureLiqPay(dto.getData(), privateKey)
            .equals(dto.getSignature())) {
            throw new BadRequestException(PAYMENT_VALIDATION_ERROR);
        }
    }

    @Override
    public OrderStatusPageDto getOrderInfoForSurcharge(Long orderId, String uuid) {
        OrderStatusPageDto orderStatusPageDto = ubsManagementService.getOrderStatusData(orderId, uuid);
        Map<Integer, Integer> amountBagsOrder = orderStatusPageDto.getAmountOfBagsOrdered();
        Map<Integer, Integer> amountBagsOrderExported = orderStatusPageDto.getAmountOfBagsExported();
        amountBagsOrderExported.replaceAll((id, quantity) -> quantity = quantity - amountBagsOrder.get(id));
        orderStatusPageDto.setAmountOfBagsExported(amountBagsOrderExported);
        Double exportedPrice = orderStatusPageDto.getOrderExportedDiscountedPrice();
        Double initialPrice = orderStatusPageDto.getOrderDiscountedPrice();
        orderStatusPageDto.setOrderExportedDiscountedPrice(exportedPrice - initialPrice);
        return orderStatusPageDto;
    }

    private StatusRequestDtoLiqPay getStatusFromLiqPay(Order order) {
        Long orderId = order.getId();

        Long paymentId = 0L;
        try {
            paymentId = order.getPayment().get(order.getPayment().size() - 1).getId();
        } catch (IndexOutOfBoundsException e) {
            throw new BadRequestException(ORDER_WITH_CURRENT_ID_NOT_FOUND);
        }

        return StatusRequestDtoLiqPay.builder()
            .publicKey(publicKey)
            .action("status")
            .orderId(orderId + "_" + paymentId.toString())
            .version(3)
            .build();
    }

    @Override
    public Map<String, Object> getLiqPayStatus(Long orderId, String uuid) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (!order.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_PAYMENT_STATUS);
        }
        StatusRequestDtoLiqPay dto = getStatusFromLiqPay(order);
        Map<String, Object> response = liqPayService.getPaymentStatus(dto);
        @Nullable
        Payment payment = converterMapToEntity(response, order);
        if (payment == null) {
            throw new BadRequestException(LIQPAY_PAYMENT_WITH_SELECTED_ID_NOT_FOUND);
        }
        paymentRepository.save(payment);
        orderRepository.save(order);
        return response;
    }

    private Payment converterMapToEntity(Map<String, Object> map, Order order) {
        if (map == null) {
            return null;
        }
        List<Payment> payments = paymentRepository.findAllByOrder(order);
        Payment payment = payments.get(payments.size() - 1);
        String status = (String) map.get("status");
        if (status.equals("success")) {
            payment.setResponseStatus(status);
            payment.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            setPaymentInfo(map, payment);
            eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
            eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM + payment.getPaymentId(),
                OrderHistory.SYSTEM, order);
        } else if (status.equals(FAILED_STATUS)) {
            payment.setResponseStatus(status);
            payment.setPaymentStatus(PaymentStatus.UNPAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
            setPaymentInfo(map, payment);
        }
        return payment;
    }

    private void setPaymentInfo(Map<String, Object> map, Payment payment) {
        payment.setMaskedCard((String) map.get("sender_card_mask2"));
        payment.setCurrency((String) map.get("currency"));
        payment.setPaymentSystem("LiqPay");
        payment.setCardType((String) map.get("sender_card_type"));
        payment.setResponseDescription((String) map.get("err_description"));
        payment.setPaymentId(String.valueOf(map.get("payment_id")));
        payment.setComment((String) map.get("description"));
        payment.setPaymentType(PaymentType.AUTO);
        payment.setSenderCellPhone((String) map.get("sender_phone"));
        String orderTime = convertMillisecondToLocalDateTime((long) map.get("create_date"));
        payment.setOrderTime(orderTime);
        String endDate = convertMillisecondToLocalDate((Long) map.get("end_date"));
        payment.setSettlementDate(endDate);
        double fees = (double) map.get("sender_commission");
        payment.setFee((long) fees);
        double amount = (double) map.get("amount");
        payment.setAmount((long) amount * 100);
    }

    private String convertMillisecondToLocalDateTime(long millis) {
        LocalDateTime date =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(date);
        return new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(timestamp);
    }

    private String convertMillisecondToLocalDate(long millis) {
        LocalDate date =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateFormatter.format(date);
    }

    @Override
    public void deleteOrder(String uuid, Long id) {
        Order order = ordersForUserRepository.getAllByUserUuidAndId(uuid, id);
        if (order == null) {
            throw new NotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }
        orderRepository.delete(order);
    }

    @Override
    public FondyOrderResponse processOrderFondyClient(OrderFondyClientDto dto, String uuid) {
        Order order = findByIdOrderForClient(dto);
        checkIsOrderPaid(order.getOrderPaymentStatus());
        User currentUser = findByIdUserForClient(uuid);
        checkForNullCounter(order);

        Integer sumToPay = calculateSumToPay(dto, order, currentUser);

        transferUserPointsToOrder(order, dto.getPointsToUse());
        paymentVerification(sumToPay, order);

        if (sumToPay == 0) {
            return getPaymentRequestDto(order, null);
        } else {
            String link = formedLink(order, sumToPay);
            return getPaymentRequestDto(order, link);
        }
    }

    private Integer calculateSumToPay(OrderFondyClientDto dto, Order order, User currentUser) {
        List<BagForUserDto> bagForUserDtos = bagForUserDtosBuilder(order);
        Integer sumToPay = bagForUserDtos.stream()
            .map(BagForUserDto::getTotalPrice)
            .reduce(0, Integer::sum);

        List<Payment> payments = order.getPayment();
        List<CertificateDto> certificateDtos = order.getCertificates().stream()
            .map(certificate -> modelMapper.map(certificate, CertificateDto.class))
            .collect(Collectors.toList());

        sumToPay =
            sumToPay - order.getPointsToUse() - countPaidAmount(payments).intValue()
                - countCertificatesBonuses(certificateDtos);

        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        sumToPay = reduceOrderSumDueToUsedPoints(sumToPay, dto.getPointsToUse());
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSumClient(dto, order, sumToPay);

        return sumToPay;
    }

    private void checkIsOrderPaid(OrderPaymentStatus orderPaymentStatus) {
        if (OrderPaymentStatus.PAID.equals(orderPaymentStatus)) {
            throw new BadRequestException(ORDER_ALREADY_PAID);
        }
    }

    private void transferUserPointsToOrder(Order order, int amountToTransfer) {
        if (amountToTransfer <= 0) {
            return;
        }

        User user = order.getUser();
        checkIfUserHaveEnoughPoints(user.getCurrentPoints(), amountToTransfer);

        int maxPointsToTransfer = countAmountToPayForOrder(order);
        if (amountToTransfer > maxPointsToTransfer) {
            throw new BadRequestException(ErrorMessage.TOO_MUCH_POINTS_FOR_ORDER + maxPointsToTransfer);
        }

        order.setPointsToUse(order.getPointsToUse() + amountToTransfer);
        user.setCurrentPoints(user.getCurrentPoints() - amountToTransfer);
        user.getChangeOfPointsList()
            .add(ChangeOfPoints.builder()
                .user(user)
                .amount(-amountToTransfer)
                .date(LocalDateTime.now())
                .order(order)
                .build());

        orderRepository.save(order);
    }

    private int countAmountToPayForOrder(Order order) {
        int certificatesAmount = nonNull(order.getCertificates())
            ? order.getCertificates().stream()
                .map(Certificate::getPoints)
                .reduce(0, Integer::sum)
            : 0;
        return order.getSumTotalAmountWithoutDiscounts().intValue() - order.getPointsToUse() - certificatesAmount;
    }

    private void paymentVerification(Integer sumToPay, Order order) {
        if (sumToPay <= 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_CONFIRMED, OrderHistory.SYSTEM, order);
        }
    }

    private void checkForNullCounter(Order order) {
        if (order.getCounterOrderPaymentId() == null) {
            order.setCounterOrderPaymentId(0L);
        }
    }

    private Order findByIdOrderForClient(OrderFondyClientDto dto) {
        return orderRepository.findById(dto.getOrderId())
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
    }

    private User findByIdUserForClient(String uuid) {
        return userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
    }

    private String formedLink(Order order, Integer sumToPay) {
        Order increment = incrementCounter(order);
        PaymentRequestDto paymentRequestDto = formPayment(increment.getId(), sumToPay);
        return getLinkFromFondyCheckoutResponse(fondyClient.getCheckoutResponse(paymentRequestDto));
    }

    private String getLinkFromFondyCheckoutResponse(String fondyResponse) {
        JSONObject json = new JSONObject(fondyResponse);
        if (!json.has("response")) {
            throw new BadRequestException("Wrong response");
        }
        JSONObject response = json.getJSONObject("response");
        if ("success".equals(response.getString("response_status"))) {
            return response.getString("checkout_url");
        }
        throw new BadRequestException(response.getString("error_message"));
    }

    private Order incrementCounter(Order order) {
        order.setCounterOrderPaymentId(order.getCounterOrderPaymentId() + 1);
        orderRepository.save(order);
        return order;
    }

    private PaymentRequestDto formPayment(Long orderId, int sumToPay) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));

        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
            .merchantId(Integer.parseInt(merchantId))
            .orderId(OrderUtils.generateOrderIdForPayment(orderId, order))
            .orderDescription("courier")
            .currency("UAH")
            .amount(sumToPay * 100)
            .responseUrl(resultUrlForPersonalCabinetOfUser)
            .build();
        paymentRequestDto.setSignature(encryptionUtil
            .formRequestSignature(paymentRequestDto, fondyPaymentKey, merchantId));
        return paymentRequestDto;
    }

    private int formCertificatesToBeSavedAndCalculateOrderSumClient(OrderFondyClientDto dto,
        Order order, int sumToPay) {
        if (sumToPay != 0 && dto.getCertificates() != null) {
            Set<Certificate> certificates =
                certificateRepository.findAllByCodeAndCertificateStatus(new ArrayList<>(dto.getCertificates()),
                    CertificateStatus.ACTIVE);
            if (certificates.isEmpty()) {
                throw new NotFoundException(ErrorMessage.CERTIFICATE_NOT_FOUND);
            }
            checkValidationCertificates(certificates, dto);
            for (Certificate temp : certificates) {
                Certificate certificate = getCertificateForClient(temp, order);
                sumToPay -= certificate.getPoints();

                if (dontSendLinkToFondyIfClient(sumToPay)) {
                    certificate.setCertificateStatus(CertificateStatus.USED);
                    certificate.setPoints(certificate.getPoints() + sumToPay);
                    sumToPay = 0;
                }
            }
        }
        return sumToPay;
    }

    private void checkValidationCertificates(Set<Certificate> certificates, OrderFondyClientDto dto) {
        if (certificates.size() != dto.getCertificates().size()) {
            String validCertification = certificates.stream().map(Certificate::getCode).collect(joining(", "));
            throw new NotFoundException(ErrorMessage.SOME_CERTIFICATES_ARE_INVALID + validCertification);
        }
    }

    private Certificate getCertificateForClient(Certificate certificate,
        Order order) {
        certificate.setOrder(order);
        certificate.setCertificateStatus(CertificateStatus.USED);
        certificate.setDateOfUse(LocalDate.now());
        return certificate;
    }

    private boolean dontSendLinkToFondyIfClient(int sumToPay) {
        return sumToPay <= 0;
    }

    @Override
    public LiqPayOrderResponse proccessOrderLiqpayClient(OrderFondyClientDto dto, String uuid) {
        Order order = orderRepository.findById(dto.getOrderId()).orElseThrow();
        checkIsOrderPaid(order.getOrderPaymentStatus());
        User currentUser = findByIdUserForClient(uuid);

        Map<Integer, Integer> amountOfBagsOrderedMap = order.getAmountOfBagsOrdered();
        checkForNullCounter(order);
        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());

        Integer sumToPay = formBagsToBeSavedAndCalculateOrderSumClient(amountOfBagsOrderedMap);
        sumToPay = reduceOrderSumDueToUsedPoints(sumToPay, dto.getPointsToUse());
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSumClient(dto, order, sumToPay);

        currentUser.setCurrentPoints(currentUser.getCurrentPoints() - dto.getPointsToUse());
        userRepository.save(currentUser);

        paymentVerification(sumToPay, order);

        if (sumToPay == 0) {
            return buildOrderResponse(order, null);
        } else {
            return linkButton(order, sumToPay);
        }
    }

    private LiqPayOrderResponse linkButton(Order order, Integer sumToPay) {
        Order order1 = incrementCounter(order);
        PaymentRequestDtoLiqPay paymentRequestDtoLiqPay = formLiqPayPayment(order1.getId(), sumToPay);
        return buildOrderResponse(order1, liqPayService.getCheckoutResponse(paymentRequestDtoLiqPay)
            .replace("\"", "")
            .replace("\n", ""));
    }

    private PaymentRequestDtoLiqPay formLiqPayPayment(Long orderId, int sumToPay) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        return PaymentRequestDtoLiqPay.builder()
            .publicKey(publicKey)
            .version(3)
            .action("pay")
            .amount(sumToPay)
            .currency("UAH")
            .description("сourier")
            .orderId(
                orderId + "_" + order.getCounterOrderPaymentId().toString() + "_" + order.getPayment().get(0).getId())
            .language("en")
            .paytypes("card")
            .resultUrl(resultUrlLiqpay)
            .build();
    }

    @Override
    @Transactional
    public void validatePaymentClient(PaymentResponseDto dto) {
        Payment orderPayment = mapPaymentClient(dto);
        String[] id = orderIdInfo(dto);
        Order order = orderRepository.findById(Long.valueOf(id[0]))
            .orElseThrow(() -> new BadRequestException(PAYMENT_VALIDATION_ERROR));

        checkResponseStatusFailure(dto, orderPayment, order);
        checkResponseValidationSignature(dto);
        checkOrderStatusApproved(dto, orderPayment, order);
    }

    private Payment mapPaymentClient(PaymentResponseDto dto) {
        String[] idClient = orderIdInfo(dto);
        Order order = orderRepository.findById(Long.valueOf(idClient[0]))
            .orElseThrow(() -> new BadRequestException(PAYMENT_VALIDATION_ERROR));
        int lastNumber = order.getPayment().size() - 1;
        checkDtoFee(dto);

        return Payment.builder()
            .id(Long.valueOf(idClient[0] + order.getCounterOrderPaymentId()
                + order.getPayment().get(lastNumber).getId()))
            .currency(dto.getCurrency())
            .amount(Long.valueOf(dto.getAmount()))
            .orderStatus(dto.getOrder_status())
            .responseStatus(dto.getResponse_status())
            .senderCellPhone(dto.getSender_cell_phone())
            .senderAccount(dto.getSender_account())
            .maskedCard(dto.getMasked_card())
            .cardType(dto.getCard_type())
            .responseCode(dto.getResponse_code())
            .responseDescription(dto.getResponse_description())
            .orderTime(dto.getOrder_time())
            .settlementDate(parseFondySettlementDate(dto.getSettlement_date()))
            .fee(Optional.ofNullable(dto.getFee()).map(Long::valueOf).orElse(0L))
            .paymentSystem(dto.getPayment_system())
            .senderEmail(dto.getSender_email())
            .paymentId(String.valueOf(dto.getPayment_id()))
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }

    private String[] orderIdInfo(PaymentResponseDto dto) {
        return dto.getOrder_id().split("_");
    }

    private void checkResponseStatusFailure(PaymentResponseDto dto, Payment orderPayment, Order order) {
        if (dto.getResponse_status().equals(FAILED_STATUS)) {
            orderPayment.setPaymentStatus(PaymentStatus.UNPAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
        }
    }

    private void checkResponseValidationSignature(PaymentResponseDto dto) {
        if (!encryptionUtil.checkIfResponseSignatureIsValid(dto, fondyPaymentKey)) {
            throw new BadRequestException(PAYMENT_VALIDATION_ERROR);
        }
    }

    private void checkOrderStatusApproved(PaymentResponseDto dto, Payment orderPayment, Order order) {
        if (dto.getOrder_status().equals(APPROVED_STATUS)) {
            orderPayment.setPaymentId(String.valueOf(dto.getPayment_id()));
            orderPayment.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            orderPayment.setOrder(order);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
            eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM + orderPayment.getPaymentId(),
                OrderHistory.SYSTEM, order);
        }
    }

    private void checkDtoFee(PaymentResponseDto dto) {
        if (dto.getFee() == null) {
            dto.setFee(0);
        }
    }

    @Override
    public UserPointDto getUserPoint(String uuid) {
        User user = userRepository.findByUuid(uuid);
        int currentUserPoints = user.getCurrentPoints();

        return new UserPointDto(currentUserPoints);
    }

    private List<Bot> getListOfBots(String uuid) {
        return EnumSet.allOf(BotType.class)
            .stream()
            .map(type -> new Bot(type.name(), createLink(type, uuid)))
            .collect(Collectors.toList());
    }

    private String createLink(BotType type, String uuid) {
        String linkTemplate = null;
        if ("TELEGRAM".equals(type.name())) {
            linkTemplate = String.format("%s%s%s%s",
                TELEGRAM_PART_1_OF_LINK, telegramBotName, TELEGRAM_PART_3_OF_LINK, uuid);
        }
        if ("VIBER".equals(type.name())) {
            linkTemplate = String.format("%s%s%s%s",
                VIBER_PART_1_OF_LINK, viberBotUri, VIBER_PART_3_OF_LINK, uuid);
        }
        return linkTemplate;
    }

    private List<AllActiveLocationsDto> getAllActiveLocations() {
        List<Location> locations = locationRepository.findAllActive();
        Map<RegionDto, List<LocationsDtos>> map = locations.stream()
            .collect(Collectors.toMap(x -> modelMapper.map(x, RegionDto.class),
                x -> new ArrayList<>(List.of(modelMapper.map(x, LocationsDtos.class))),
                (x, y) -> {
                    x.addAll(y);
                    return new ArrayList<>(x).stream().distinct().collect(Collectors.toList());
                }));

        return map.entrySet().stream()
            .map(x -> AllActiveLocationsDto.builder()
                .regionId(x.getKey().getRegionId())
                .nameEn(x.getKey().getNameEn())
                .nameUk(x.getKey().getNameUk())
                .locations(x.getValue())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public OrderCourierPopUpDto getInfoForCourierOrdering(String uuid, Optional<String> changeLoc) {
        OrderCourierPopUpDto orderCourierPopUpDto = new OrderCourierPopUpDto();
        if (changeLoc.isPresent()) {
            orderCourierPopUpDto.setOrderIsPresent(false);
            orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocations());
            return orderCourierPopUpDto;
        }
        Optional<Order> lastOrder = orderRepository.getLastOrderOfUserByUUIDIfExists(uuid);
        if (lastOrder.isPresent()) {
            orderCourierPopUpDto.setOrderIsPresent(true);
            orderCourierPopUpDto.setTariffsForLocationDto(
                modelMapper.map(tariffsInfoRepository.findTariffsInfoByOrdersId(lastOrder.get().getId()),
                    TariffsForLocationDto.class));
        } else {
            orderCourierPopUpDto.setOrderIsPresent(false);
            orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocations());
        }
        return orderCourierPopUpDto;
    }

    private TariffsInfo findTariffsInfoByCourierAndLocationId(Long courierId, Long locationId) {
        return tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(courierId, locationId)
            .orElseThrow(
                () -> new NotFoundException(TARIFF_FOR_LOCATION_NOT_EXIST + locationId));
    }

    @Override
    public OrderCourierPopUpDto getTariffInfoForLocation(Long locationId) {
        return OrderCourierPopUpDto.builder()
            .orderIsPresent(true)
            .tariffsForLocationDto(modelMapper.map(
                findTariffsInfoByCourierAndLocationId(1L, locationId), TariffsForLocationDto.class))
            .build();
    }

    @Override
    public TariffsForLocationDto getTariffForOrder(Long id) {
        Optional<TariffsInfo> tariffsInfo = tariffsInfoRepository.findByOrdersId(id);
        if (tariffsInfo.isPresent()) {
            return modelMapper.map(tariffsInfo.get(), TariffsForLocationDto.class);
        } else {
            throw new EntityNotFoundException(ErrorMessage.TARIFF_FOR_ORDER_NOT_EXIST + id);
        }
    }

    @Override
    public Set<String> getAllAuthorities(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_DOESNT_EXIST));
        return userRemoteClient.getAllAuthorities(employee.getEmail());
    }

    @Override
    public void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto, String email) {
        Employee employee = employeeRepository.findByEmail(dto.getEmployeeEmail())
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_DOESNT_EXIST));
        userRemoteClient.updateEmployeesAuthorities(dto, email);
    }

    @Override
    public List<LocationSummaryDto> getLocationSummary() {
        return regionRepository.findAll().stream()
            .map(location -> modelMapper.map(location, LocationSummaryDto.class))
            .collect(Collectors.toList());
    }
}