package greencity.service.ubs;

import greencity.client.RestClient;

import static greencity.constant.ErrorMessage.AMOUNT_OF_POINTS_BIGGER_THAN_SUM;
import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.CERTIFICATE_EXPIRED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_NOT_ACTIVATED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_USED;
import static greencity.constant.ErrorMessage.CERTIFICATE_NOT_FOUND_BY_CODE;
import static greencity.constant.ErrorMessage.MINIMAL_SUM_VIOLATION;
import static greencity.constant.ErrorMessage.PAYMENT_VALIDATION_ERROR;
import static greencity.constant.ErrorMessage.SUM_IS_COVERED_BY_CERTIFICATES;
import static greencity.constant.ErrorMessage.THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TOO_MANY_CERTIFICATES;
import static greencity.constant.ErrorMessage.USER_DONT_HAVE_ENOUGH_POINTS;

import greencity.dto.*;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.*;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.BagNotFoundException;
import greencity.exceptions.CertificateExpiredException;
import greencity.exceptions.CertificateIsNotActivated;
import greencity.exceptions.CertificateIsUsedException;
import greencity.exceptions.CertificateNotFoundException;
import greencity.exceptions.IncorrectValueException;
import greencity.exceptions.PaymentValidationException;
import greencity.exceptions.TooManyCertificatesEntered;
import greencity.repository.*;
import greencity.util.EncryptionUtil;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UBSClientService}.
 */
@Service
@AllArgsConstructor
public class UBSClientServiceImpl implements UBSClientService {
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSuserRepository ubsUserRepository;
    private final BagTranslationRepository bagTranslationRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final OrderRepository orderRepository;
    private final RestClient restClient;
    private final String password = "test";
    private final String merchantId = "1396424";

    @Override
    @Transactional
    public void validatePayment(PaymentResponseDto dto) {
        if (dto.getResponse_status().equals("failure")) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
        if (!EncryptionUtil.checkIfResponseSignatureIsValid(dto, password)) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
        Order order = orderRepository.findById(Long.valueOf(dto.getOrder_id()))
            .orElseThrow(() -> new PaymentValidationException(PAYMENT_VALIDATION_ERROR));
        Payment orderPayment = order.getPayment();
        if (orderPayment.getCurrency() != dto.getCurrency()
            || orderPayment.getAmount() != Long.valueOf(dto.getAmount())) {
            throw new PaymentValidationException(PAYMENT_VALIDATION_ERROR);
        }
        if (dto.getOrder_status().equals("approved")) {
            order.setOrderStatus(OrderStatus.PAID);
        }
        orderPayment = modelMapper.map(dto, Payment.class);
        order.setPayment(orderPayment);

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
        if (userRepository.findByUuid(uuid) == null) {
            UbsTableCreationDto dto = restClient.getDataForUbsTableRecordCreation();
            uuid = dto.getUuid();
            createRecordInUBStable(uuid);
        }
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
     */
    @Override
    @Transactional
    public PaymentRequestDto saveFullOrderToDB(OrderResponseDto dto, String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        if (currentUser.getCurrentPoints() < dto.getPointsToUse()) {
            throw new IncorrectValueException(USER_DONT_HAVE_ENOUGH_POINTS);
        }

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

        UBSuser userData = formUserDataToBeSaved(dto.getPersonalData(), currentUser);

        order = formAndSaveOrder(order, orderCertificates, amountOfBagsOrderedMap, userData, currentUser, sumToPay);

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        return formPaymentRequest(order.getId(), sumToPay);
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
        order.setPayment(payment);
        orderRepository.save(order);

        return order;
    }

    private PaymentRequestDto formPaymentRequest(Long orderId, int sumToPay) {
        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
            .orderId(orderId.toString())
            .orderDescription("ubs courier")
            .currency("UAH")
            .amount(sumToPay * 100).build();

        paymentRequestDto.setSignature(EncryptionUtil.formRequestSignature(paymentRequestDto, password, merchantId));

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
            int certPoints = 0;
            for (String temp : dto.getCertificates()) {
                if (tooManyCertificates) {
                    throw new TooManyCertificatesEntered(TOO_MANY_CERTIFICATES);
                }
                Certificate certificate = certificateRepository.findById(temp).orElseThrow(
                    () -> new CertificateNotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + temp));
                validateCertificate(certificate);
                certificate.setCertificateStatus(CertificateStatus.USED);
                certificate.setOrder(order);
                orderCertificates.add(certificate);
                sumToPay -= certificate.getPoints();

                certPoints += certificate.getPoints();
                if (certPoints >= sumToPay) {
                    sumToPay = 0;
                    tooManyCertificates = true;
                    if (dto.getPointsToUse() > 0) {
                        throw new IncorrectValueException(SUM_IS_COVERED_BY_CERTIFICATES);
                    }
                }
            }
        }

        return sumToPay;
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
}
