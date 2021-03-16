package greencity.service.ubs;

import greencity.client.RestClient;
import greencity.dto.BagDto;
import greencity.dto.CertificateDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UbsTableCreationDto;
import greencity.dto.UserPointsAndAllBagsDto;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.BagNotFoundException;
import greencity.exceptions.CertificateExpiredException;
import greencity.exceptions.CertificateIsUsedException;
import greencity.exceptions.CertificateNotFoundException;
import greencity.exceptions.IncorrectValueException;
import greencity.exceptions.TooManyCertificatesEntered;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static greencity.constant.ErrorMessage.*;

/**
 * Implementation of {@link UBSClientService}.
 */
@Service
@AllArgsConstructor
public class UBSClientServiceImpl implements UBSClientService {
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSuserRepository ubsUserRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;
    private final RestClient restClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPointsAndAllBagsDto getFirstPageData(String uuid) {
        int currentUserPoints = 0;
        User user = userRepository.findByUuid(uuid);
        if (user != null) {
            currentUserPoints = user.getCurrentPoints();
        }

        return new UserPointsAndAllBagsDto((List<Bag>) bagRepository.findAll(),
            currentUserPoints);
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
    public void saveFullOrderToDB(OrderResponseDto dto, String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        if (currentUser.getCurrentPoints() < dto.getPointsToUse()) {
            throw new IncorrectValueException(USER_DONT_HAVE_ENOUGH_POINTS);
        }

        Map<Integer, Integer> map = new HashMap<>();
        int sumToPay = formBagsToBeSavedAndCalculateOrderSum(map, dto.getBags());

        Order order = modelMapper.map(dto, Order.class);
        Set<Certificate> orderCertificates = new HashSet<>();
        formCertificatesToBeSaved(dto, orderCertificates, order, sumToPay);

        UBSuser ubsUserFromDatabaseById = null;
        if (dto.getPersonalData().getId() != null) {
            ubsUserFromDatabaseById =
                ubsUserRepository.findById(dto.getPersonalData().getId())
            .orElseThrow(() -> new IncorrectValueException(THE_SET_OF_UBS_USER_DATA_DOES_NOT_EXIST
                + dto.getPersonalData().getId()));
        }

        UBSuser userToBeSaved = modelMapper.map(dto.getPersonalData(), UBSuser.class);
        userToBeSaved.setUser(currentUser);
        if (userToBeSaved.getId() == null || !userToBeSaved.equals(ubsUserFromDatabaseById)) {
            userToBeSaved.setId(null);
            ubsUserRepository.save(userToBeSaved);
            currentUser.getUbsUsers().add(userToBeSaved);
        } else {
            userToBeSaved = ubsUserFromDatabaseById;
        }

        order.setOrderStatus(OrderStatus.FORMED);
        order.setCertificates(orderCertificates);
        order.setAmountOfBagsOrdered(map);
        order.setUbsUser(userToBeSaved);
        order.setUser(currentUser);

        currentUser.getOrders().add(order);
        if (dto.getPointsToUse() != 0) {
            currentUser.setCurrentPoints(currentUser.getCurrentPoints() - dto.getPointsToUse());
            currentUser.getChangeOfPoints().put(order.getOrderDate(), -dto.getPointsToUse());
        }

        userRepository.save(currentUser);
    }

    public void formCertificatesToBeSaved(OrderResponseDto dto, Set<Certificate> orderCertificates,
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
                certificateRepository.save(certificate);
                orderCertificates.add(certificate);

                certPoints += certificate.getPoints();
                if (certPoints > sumToPay) {
                    tooManyCertificates = true;
                    if (dto.getPointsToUse() > 0) {
                        throw new IncorrectValueException(SUM_IS_COVERED_BY_CERTIFICATES);
                    }
                    dto.setPointsToUse(sumToPay - certPoints);
                }
            }
        }
    }

    public int formBagsToBeSavedAndCalculateOrderSum(Map<Integer, Integer> map, List<BagDto> bags) {
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
        if (certificate.getCertificateStatus() != CertificateStatus.ACTIVE) {
            throw new CertificateIsUsedException(CERTIFICATE_IS_USED + certificate.getCode());
        } else {
            LocalDate future = certificate.getExpirationDate().plusYears(1);
            if (future.isBefore(LocalDate.now())) {
                throw new CertificateExpiredException(CERTIFICATE_EXPIRED + certificate.getCode());
            }
        }
    }

    private void createRecordInUBStable(String uuid) {
        userRepository.save(User.builder().currentPoints(0).violations(0).uuid(uuid).build());
    }
}
