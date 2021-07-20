package greencity.service.ubs;

import greencity.client.RestClient;

import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.enums.AddressStatus;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.*;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.util.EncryptionUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static greencity.constant.ErrorMessage.*;

/**
 * Implementation of {@link UBSClientService}.
 */
@Service
@RequiredArgsConstructor
public class UBSClientServiceImpl implements UBSClientService {
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSuserRepository ubsUserRepository;
    private final BagTranslationRepository bagTranslationRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepo;
    private final RestClient restClient;
    private final PaymentRepository paymentRepository;
    @PersistenceContext
    private final EntityManager entityManager;
    @Value("${fondy.payment.key}")
    private String fondyPaymentKey;
    @Value("${merchant.id}")
    private String merchantId;

    @Override
    @Transactional
    public void validatePayment(PaymentResponseDto dto) {
        if (dto.getResponse_status().equals("failure")) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
        if (!EncryptionUtil.checkIfResponseSignatureIsValid(dto, fondyPaymentKey)) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
        String[] ids = dto.getOrder_id().split("_");
        Order order = orderRepository.findById(Long.valueOf(ids[0]))
            .orElseThrow(() -> new PaymentValidationException(PAYMENT_VALIDATION_ERROR));
        Payment orderPayment = order.getPayment().get(order.getPayment().size() - 1);
        if (!orderPayment.getCurrency().equals(dto.getCurrency())
            || !orderPayment.getAmount().equals(Long.valueOf(dto.getAmount()))) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
        if (dto.getOrder_status().equals("approved")) {
            order.setOrderStatus(OrderStatus.PAID);
        }
        orderPayment = modelMapper.map(dto, Payment.class);
        orderPayment.setOrder(order);
        paymentRepository.save(orderPayment);
        orderRepository.save(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPointsAndAllBagsDto getFirstPageData(String uuid, String language) {
        int currentUserPoints = 0;
        User user = userRepository.findByUuid(uuid);
        if (user != null) {
            currentUserPoints = user.getCurrentPoints();
        }

        List<BagTranslationDto> btdList = bagTranslationRepository.findAllByLanguage(language)
            .stream()
            .map(this::buildBagTranslationDto)
            .collect(Collectors.toList());
        return new UserPointsAndAllBagsDto(btdList, currentUserPoints);
    }

    private BagTranslationDto buildBagTranslationDto(BagTranslation bt) {
        return BagTranslationDto.builder()
            .id(bt.getBag().getId())
            .capacity(bt.getBag().getCapacity())
            .price(bt.getBag().getPrice())
            .name(bt.getName())
            .code(bt.getLanguage().getCode())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<PersonalDataDto> getSecondPageData(String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        Long userId = userRepository.findByUuid(uuid).getId();
        List<UBSuser> allByUserId = ubsUserRepository.getAllByUserId(userId);

        if (allByUserId.isEmpty()) {
            return List.of(PersonalDataDto.builder().build());
        }
        return allByUserId.stream().map(u -> modelMapper.map(u, PersonalDataDto.class)).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CertificateDto checkCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code)
            .orElseThrow(() -> new CertificateNotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + code));

        return new CertificateDto(certificate.getCertificateStatus().toString(), certificate.getPoints(),
            certificate.getExpirationDate());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @Transactional
    public String saveFullOrderToDB(OrderResponseDto dto, String uuid) {
        User currentUser = userRepository.findByUuid(uuid);

        checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());

        Map<Integer, Integer> amountOfBagsOrderedMap = new HashMap<>();

        int sumToPay = formBagsToBeSavedAndCalculateOrderSum(amountOfBagsOrderedMap, dto.getBags());

        if (sumToPay < dto.getPointsToUse()) {
            throw new IncorrectValueException(AMOUNT_OF_POINTS_BIGGER_THAN_SUM);
        } else {
            sumToPay -= dto.getPointsToUse();
        }

        Order order = modelMapper.map(dto, Order.class);
        Set<Certificate> orderCertificates = new HashSet<>();
        sumToPay = formCertificatesToBeSavedAndCalculateOrderSum(dto, orderCertificates, order, sumToPay);

        UBSuser userData;
        userData = formUserDataToBeSaved(dto.getPersonalData(), currentUser);

        Address address = addressRepo.findById(dto.getAddressId()).orElseThrow(() -> new NotFoundOrderAddressException(
            ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + dto.getAddressId()));

        checkIfAddressHasBeenDeleted(address);

        checkAddressUser(address, currentUser);
        address.setAddressStatus(AddressStatus.IN_ORDER);

        userData.setAddress(address);

        if (userData.getAddress().getComment() == null) {
            userData.getAddress().setComment(dto.getPersonalData().getAddressComment());
        }

        order = formAndSaveOrder(order, orderCertificates, amountOfBagsOrderedMap, userData, currentUser, sumToPay);

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        PaymentRequestDto paymentRequestDto = formPaymentRequest(order.getId(), sumToPay);
        String html = restClient.getDataFromFondy(paymentRequestDto);

        Document doc = Jsoup.parse(html);

        Elements links = doc.select("a[href]");
        System.out.println(links.attr("href"));
        return links.attr("href");
    }

    private void checkIfAddressHasBeenDeleted(Address address) {
        if (address.getAddressStatus().equals(AddressStatus.DELETED)) {
            throw new NotFoundOrderAddressException(
                ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkAddressUser(Address address, User user) {
        if (!address.getUser().equals(user)) {
            throw new NotFoundOrderAddressException(
                ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkIfUserHaveEnoughPoints(Integer i1, Integer i2) {
        if (i1 < i2) {
            throw new IncorrectValueException(USER_DONT_HAVE_ENOUGH_POINTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto findAllAddressesForCurrentOrder(String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        Long id = userRepository.findByUuid(uuid).getId();
        List<AddressDto> addressDtoList = addressRepo.findAllByUserId(id)
            .stream()
            .sorted(Comparator.comparing(Address::getId))
            .filter(u -> u.getAddressStatus() != AddressStatus.DELETED)
            .map(u -> modelMapper.map(u, AddressDto.class))
            .collect(Collectors.toList());
        return new OrderWithAddressesResponseDto(addressDtoList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto saveCurrentAddressForOrder(OrderAddressDtoRequest dtoRequest, String uuid) {
        createUserByUuidIfUserDoesNotExist(uuid);
        List<Address> addresses = addressRepo.findAllByUserId(userRepository.findByUuid(uuid).getId());
        if (addresses != null) {
            addresses.forEach(u -> {
                u.setActual(false);
                addressRepo.save(u);
            });
        }
        Address address;
        Address forOrderAfterUpdate;

        if (dtoRequest.getId() != 0) {
            address = addressRepo.findById(dtoRequest.getId()).orElse(null);
            forOrderAfterUpdate = modelMapper.map(dtoRequest, Address.class);

            if (address.getAddressStatus().equals(AddressStatus.DELETED)) {
                address = null;
            }
        } else {
            address = null;
            forOrderAfterUpdate = null;
        }

        if (address == null || !address.getUser().equals(userRepository.findByUuid(uuid))) {
            address = modelMapper.map(dtoRequest, Address.class);
            address.setId(null);
            address.setUser(userRepository.findByUuid(uuid));
            address.setActual(true);
            address.setAddressStatus(AddressStatus.NEW);
        } else {
            if (address.getAddressStatus().equals(AddressStatus.IN_ORDER)) {
                forOrderAfterUpdate.setId(null);
                forOrderAfterUpdate.setActual(true);
                forOrderAfterUpdate.setUser(address.getUser());
                forOrderAfterUpdate.setAddressStatus(address.getAddressStatus());
                forOrderAfterUpdate.setComment(address.getComment());

                address.getUbsUsers().forEach(u -> u.setAddress(addressRepo.save(forOrderAfterUpdate)));
            }
            address = modelMapper.map(dtoRequest, Address.class);
            address.setUser(userRepository.findByUuid(uuid));
            address.setActual(true);
            address.setAddressStatus(AddressStatus.NEW);
        }
        addressRepo.save(address);
        return findAllAddressesForCurrentOrder(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto deleteCurrentAddressForOrder(Long addressId, String uuid) {
        Address address = addressRepo.findById(addressId).orElseThrow(
            () -> new NotFoundOrderAddressException(ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));
        if (!address.getUser().equals(userRepository.findByUuid(uuid))) {
            throw new NotFoundOrderAddressException(ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId);
        }
        address.setAddressStatus(AddressStatus.DELETED);
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
    public OrderClientDto cancelFormedOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new OrderNotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (order.getOrderStatus() == OrderStatus.FORMED) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setCertificates(Collections.emptySet());
            order.setPointsToUse(0);
            order.setAmountOfBagsOrdered(Collections.emptyMap());
            order.getPayment().stream().forEach(x -> x.setAmount(0L));
            order = orderRepository.save(order);
            return modelMapper.map(order, OrderClientDto.class);
        } else {
            throw new BadOrderStatusRequestException(ErrorMessage.BAD_ORDER_STATUS_REQUEST + order.getOrderStatus());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<OrderBagDto> makeOrderAgain(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (order.getOrderStatus() == OrderStatus.ON_THE_ROUTE
            || order.getOrderStatus() == OrderStatus.CONFIRMED
            || order.getOrderStatus() == OrderStatus.DONE) {
            return buildOrderBagDto(order);
        } else {
            throw new BadOrderStatusRequestException(ErrorMessage.BAD_ORDER_STATUS_REQUEST + order.getOrderStatus());
        }
    }

    private List<OrderBagDto> buildOrderBagDto(Order order) {
        List<OrderBagDto> build = new ArrayList<>();
        for (Map.Entry<Integer, Integer> pair : order.getAmountOfBagsOrdered().entrySet()) {
            build.add(OrderBagDto.builder()
                .id(pair.getKey())
                .amount(pair.getValue())
                .build());
        }
        return build;
    }

    /**
     * Method returns info about user, ubsUser and user violations by order orderId.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link UserInfoDto};
     * @author Rusanovscaia Nadejda
     */
    @Override
    @Transactional
    public UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new OrderNotFoundException(ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
        }
        Order order = optionalOrder.get();
        return UserInfoDto.builder()
            .customerName(order.getUser().getRecipientName())
            .customerPhoneNumber(order.getUser().getRecipientPhone())
            .customerEmail(order.getUser().getRecipientEmail())
            .totalUserViolations(userRepository.countTotalUsersViolations(order.getUser().getId()))
            .recipientName(order.getUbsUser().getFirstName() + " " + order.getUbsUser().getLastName())
            .recipientPhoneNumber(order.getUbsUser().getPhoneNumber())
            .recipientEmail(order.getUbsUser().getEmail())
            .userViolationForCurrentOrder(
                userRepository.checkIfUserHasViolationForCurrentOrder(order.getUser().getId(), order.getId()))
            .build();
    }

    /**
     * Method updates ubs_user information order in order.
     *
     * @param dtoUpdate of {@link UbsCustomersDtoUpdate} ubs_user_id;
     * @return {@link UbsCustomersDto};
     * @author Rusanovscaia Nadejda
     */
    @Override
    public UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate) {
        Optional<UBSuser> optionalUbsUser = ubsUserRepository.findById(dtoUpdate.getId());
        if (optionalUbsUser.isEmpty()) {
            throw new UBSuserNotFoundException(RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST + dtoUpdate.getId());
        }
        UBSuser user = optionalUbsUser.get();
        ubsUserRepository.save(updateRecipientDataInOrder(user, dtoUpdate));
        return UbsCustomersDto.builder()
            .name(user.getFirstName() + " " + user.getLastName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

    private UBSuser updateRecipientDataInOrder(UBSuser ubSuser, UbsCustomersDtoUpdate dto) {
        ubSuser.setFirstName(dto.getRecipientName().split(" ")[0]);
        ubSuser.setLastName(dto.getRecipientName().split(" ")[1]);
        ubSuser.setPhoneNumber(dto.getRecipientPhoneNumber());
        ubSuser.setEmail(dto.getRecipientEmail());
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

        Payment payment = Payment.builder()
            .amount((long) (sumToPay * 100))
            .orderStatus("created")
            .currency("UAH")
            .order(order).build();
        if (order.getPayment() != null) {
            order.getPayment().add(payment);
        } else {
            ArrayList<Payment> arrayOfPayments = new ArrayList<>();
            arrayOfPayments.add(payment);
            order.setPayment(arrayOfPayments);
        }
        orderRepository.save(order);
        return order;
    }

    private PaymentRequestDto formPaymentRequest(Long orderId, int sumToPay) {
        Order testOrder = orderRepository.findById(orderId).orElseThrow(null);
        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
            .merchantId(Integer.parseInt(merchantId))
            .orderId(orderId.toString() + "_"
                + testOrder.getPayment().get(testOrder.getPayment().size() - 1).getId().toString())
            .orderDescription("ubs courier")
            .currency("UAH")
            .amount(sumToPay * 100).build();

        paymentRequestDto.setSignature(EncryptionUtil
            .formRequestSignature(paymentRequestDto, fondyPaymentKey, merchantId));

        return paymentRequestDto;
    }

    private UBSuser formUserDataToBeSaved(PersonalDataDto dto, User currentUser) {
        UBSuser ubsUserFromDatabaseById = null;
        if (dto.getId() != null) {
            ubsUserFromDatabaseById =
                ubsUserRepository.findById(dto.getId())
                    .orElseThrow(() -> new IncorrectValueException(THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST
                        + dto.getId()));
        }
        UBSuser mappedFromDtoUser = modelMapper.map(dto, UBSuser.class);
        mappedFromDtoUser.setUser(currentUser);
        if (mappedFromDtoUser.getId() == null || !mappedFromDtoUser.equals(ubsUserFromDatabaseById)) {
            mappedFromDtoUser.setId(null);
            ubsUserRepository.save(mappedFromDtoUser);
            currentUser.getUbsUsers().add(mappedFromDtoUser);
            return mappedFromDtoUser;
        } else {
            return ubsUserFromDatabaseById;
        }
    }

    private int formCertificatesToBeSavedAndCalculateOrderSum(OrderResponseDto dto, Set<Certificate> orderCertificates,
        Order order, int sumToPay) {
        if (dto.getCertificates() != null) {
            boolean tooManyCertificates = false;
            for (String temp : dto.getCertificates()) {
                if (tooManyCertificates) {
                    throw new TooManyCertificatesEntered(TOO_MANY_CERTIFICATES);
                }
                Certificate certificate = certificateRepository.findById(temp).orElseThrow(
                    () -> new CertificateNotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + temp));
                validateCertificate(certificate);
                certificate.setOrder(order);
                orderCertificates.add(certificate);
                sumToPay -= certificate.getPoints();
                if (dontSendLinkToFondyIf(sumToPay, certificate, dto)) {
                    sumToPay = 0;
                    tooManyCertificates = true;
                }
            }
        }
        return sumToPay;
    }

    private boolean dontSendLinkToFondyIf(int sumToPay, Certificate certificate, OrderResponseDto orderResponseDto) {
        if (sumToPay <= 0) {
            certificate.setCertificateStatus(CertificateStatus.USED);
            if (orderResponseDto.getPointsToUse() > 0) {
                throw new IncorrectValueException(SUM_IS_COVERED_BY_CERTIFICATES);
            }
            return true;
        }
        return false;
    }

    private int formBagsToBeSavedAndCalculateOrderSum(Map<Integer, Integer> map, List<BagDto> bags) {
        int sumToPay = 0;
        for (BagDto temp : bags) {
            Bag bag = bagRepository.findById(temp.getId())
                .orElseThrow(() -> new BagNotFoundException(BAG_NOT_FOUND + temp.getId()));
            sumToPay += bag.getPrice() * temp.getAmount();
            map.put(temp.getId(), temp.getAmount());
        }
        if (sumToPay < 500) {
            throw new IncorrectValueException(MINIMAL_SUM_VIOLATION);
        }

        return sumToPay;
    }

    private void validateCertificate(Certificate certificate) {
        if (certificate.getCertificateStatus() == CertificateStatus.NEW) {
            throw new CertificateIsNotActivated(CERTIFICATE_IS_NOT_ACTIVATED + certificate.getCode());
        } else if (certificate.getCertificateStatus() == CertificateStatus.USED) {
            throw new CertificateIsUsedException(CERTIFICATE_IS_USED + certificate.getCode());
        } else {
            if (LocalDate.now().isAfter(certificate.getExpirationDate())) {
                throw new CertificateExpiredException(CERTIFICATE_EXPIRED + certificate.getCode());
            }
        }
    }

    private void createRecordInUBStable(String uuid) {
        userRepository.save(User.builder().currentPoints(0).violations(0).uuid(uuid).build());
    }

    @Override
    public AllPointsUserDto findAllCurrentPointsForUser(String uuid) {
        List<Order> allByUserId = orderRepository.findAllOrdersByUserUuid(uuid);
        if (allByUserId.isEmpty()) {
            throw new OrderNotFoundException(ErrorMessage.ORDERS_FOR_UUID_NOT_EXIST);
        }
        List<PointsForUbsUserDto> bonusForUbsUser = allByUserId.stream()
            .filter(a -> a.getPointsToUse() != 0)
            .map(u -> modelMapper.map(u, PointsForUbsUserDto.class))
            .collect(Collectors.toList());
        AllPointsUserDto allBonusesForUserDto = new AllPointsUserDto();
        allBonusesForUserDto.setUserBonuses(sumUserPoints(allByUserId));
        allBonusesForUserDto.setUbsUserBonuses(bonusForUbsUser);
        return allBonusesForUserDto;
    }

    private Integer sumUserPoints(List<Order> allByUserId) {
        return allByUserId.stream().map(Order::getPointsToUse).reduce(0, (x, y) -> x + y);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public UserProfileDto saveProfileData(String uuid, UserProfileDto userProfileDto) {
        UbsTableCreationDto dto = restClient.getDataForUbsTableRecordCreation();
        uuid = dto.getUuid();
        User user = userRepository.findByUuid(uuid);
        setUserDate(user, userProfileDto);
        AddressDto addressDto = userProfileDto.getAddressDto();
        Address address = modelMapper.map(addressDto, Address.class);
        address.setUser(user);
        Address savedAddress = addressRepo.save(address);
        User savedUser = userRepository.save(user);
        AddressDto mapperAddressDto = modelMapper.map(savedAddress, AddressDto.class);
        UserProfileDto mappedUserProfileDto = modelMapper.map(savedUser, UserProfileDto.class);
        mappedUserProfileDto.setAddressDto(mapperAddressDto);
        return mappedUserProfileDto;
    }

    @Override
    public UserProfileDto getProfileData(String uuid) {
        UbsTableCreationDto dto = restClient.getDataForUbsTableRecordCreation();
        uuid = dto.getUuid();
        User user = userRepository.findByUuid(uuid);
        UserProfileDto userProfileDto = modelMapper.map(user, UserProfileDto.class);
        List<Address> allAddress = addressRepo.findAllByUserId(user.getId());
        if (allAddress == null) {
            throw new AddressNotFoundException(NOT_FOUND_ADDRESS_BY_USER_UUID);
        }
        for (Address address : allAddress) {
            AddressDto addressDto = modelMapper.map(address, AddressDto.class);
            setAddressDate(address, addressDto);
            userProfileDto.setAddressDto(addressDto);
        }
        return userProfileDto;
    }

    private User setUserDate(User user, UserProfileDto userProfileDto) {
        user.setRecipientName(userProfileDto.getRecipientName());
        user.setRecipientSurname(userProfileDto.getRecipientSurname());
        user.setRecipientPhone(userProfileDto.getRecipientPhone());
        user.setRecipientEmail(userProfileDto.getRecipientEmail());
        return user;
    }

    private Address setAddressDate(Address address, AddressDto addressDto) {
        address.setCity(addressDto.getCity());
        address.setStreet(addressDto.getStreet());
        address.setDistrict(addressDto.getDistrict());
        address.setHouseNumber(addressDto.getHouseNumber());
        address.setEntranceNumber(addressDto.getEntranceNumber());
        address.setHouseCorpus(addressDto.getHouseCorpus());

        return address;
    }

    private void createUserByUuidIfUserDoesNotExist(String uuid) {
        if (userRepository.findByUuid(uuid) == null) {
            UbsTableCreationDto dto = restClient.getDataForUbsTableRecordCreation();
            uuid = dto.getUuid();
            createRecordInUBStable(uuid);
        }
    }
}
