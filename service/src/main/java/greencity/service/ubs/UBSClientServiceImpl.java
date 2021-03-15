package greencity.service.ubs;

import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
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
            .orElseThrow(() -> new CertificateNotFoundException(ErrorMessage.CERTIFICATE_NOT_FOUND_BY_CODE + code));

        return new CertificateDto(certificate.getCertificateStatus().toString(), certificate.getPoints(),
            certificate.getDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveFullOrderToDB(OrderResponseDto dto, String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        if (currentUser.getCurrentPoints() < dto.getPointsToUse()) {
            throw new IncorrectValueException("User doesn't have enough bonus points. The amount of user's points "
                + currentUser.getCurrentPoints() + ". Entered value " + dto.getPointsToUse() + ".");
        }

        Map<Integer, Integer> map = new HashMap<>();
        double sumToPay = 0d;
        for (BagDto temp : dto.getBags()) {
            Bag bag = bagRepository.findById(temp.getId())
                .orElseThrow(() -> new BagNotFoundException(ErrorMessage.BAG_NOT_FOUND + temp.getId()));
            sumToPay += bag.getPrice() * temp.getAmount();
            map.put(temp.getId(), temp.getAmount());
        }

        if (sumToPay < 500) {
            throw new IncorrectValueException("Payment sum is too small to perform the order. "
                + "Minimal order sum is 500UAH.");
        }

        Order order = modelMapper.map(dto, Order.class);
        Set<Certificate> orderCertificates = new HashSet<>();
        if (dto.getCertificates() != null) {
            boolean tooManyCertificates = false;
            int certPoints = 0;
            for (String temp : dto.getCertificates()) {
                if (tooManyCertificates) {
                    throw new TooManyCertificatesEntered("Too many certificates was entered.");
                }
                Certificate certificate = certificateRepository.findById(temp).orElseThrow(
                    () -> new CertificateNotFoundException(ErrorMessage.CERTIFICATE_NOT_FOUND_BY_CODE + temp));
                validateCertificate(certificate);
                certificate.setCertificateStatus(CertificateStatus.USED);
                certificate.setOrder(order);
                orderCertificates.add(certificate);

                certPoints += certificate.getPoints();
                if (certPoints > sumToPay) {
                    tooManyCertificates = true;
                    if (dto.getPointsToUse() > 0) {
                        throw new IncorrectValueException("Bonus points shouldn't be used if sum to pay is "
                            + "covered by certificates.");
                    }
                    dto.setPointsToUse((int) (sumToPay - certPoints));
                }
            }
        }

        UBSuser ubsUserFromDatabaseById = null;
        if (dto.getPersonalData().getId() != null) {
            ubsUserFromDatabaseById = ubsUserRepository.findById(dto.getPersonalData().getId()).orElseThrow(
                () -> new IncorrectValueException("The set of user data with id " + dto.getPersonalData().getId()
                    + " does not exist."));
        }

        UBSuser enteredUbsUser = modelMapper.map(dto.getPersonalData(), UBSuser.class);
        enteredUbsUser.setUser(currentUser);
        if (enteredUbsUser.getId() == null || !enteredUbsUser.equals(ubsUserFromDatabaseById)) {
            enteredUbsUser.setId(null);
            ubsUserRepository.save(enteredUbsUser);
            currentUser.getUbsUsers().add(enteredUbsUser);
        } else {
            enteredUbsUser = ubsUserFromDatabaseById;
        }

        order.setOrderStatus(OrderStatus.NEW);
        order.setCertificates(orderCertificates);
        order.setAmountOfBagsOrdered(map);
        order.setUbsUser(enteredUbsUser);
        order.setUser(currentUser);

        currentUser.getOrders().add(order);
        if (dto.getPointsToUse() != 0) {
            currentUser.setCurrentPoints(currentUser.getCurrentPoints() - dto.getPointsToUse());
            currentUser.getChangeOfPoints().put(order.getOrderDate(), -dto.getPointsToUse());
        }

        userRepository.save(currentUser);
    }

    private void validateCertificate(Certificate certificate) {
        if (certificate.getCertificateStatus() != CertificateStatus.ACTIVE) {
            throw new CertificateIsUsedException(ErrorMessage.CERTIFICATE_IS_USED + certificate.getCode());
        } else {
            LocalDate future = certificate.getDate().plusYears(1);
            if (future.isBefore(LocalDate.now())) {
                throw new CertificateExpiredException(ErrorMessage.CERTIFICATE_EXPIRED + certificate.getCode());
            }
        }
    }

    private void createRecordInUBStable(String uuid) {
        userRepository.save(User.builder().currentPoints(0).violations(0).uuid(uuid).build());
    }
}
