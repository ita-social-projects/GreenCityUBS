package greencity.service;

import greencity.dto.CertificateDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UserPointsAndAllBagsDto;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link UBSService}.
 */
@Service
@AllArgsConstructor
public class UBSServiceImpl implements UBSService {
    private final UserRepository userRepository;
    private final BagRepository bagRepository;
    private final UBSuserRepository ubsUserRepository;
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPointsAndAllBagsDto getFirstPageData(Long userId) {
        return new UserPointsAndAllBagsDto(
            userRepository.findById(userId).get().getCurrentPoints(),
            (List<Bag>) bagRepository.findAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<PersonalDataDto> getSecondPageData(Long userId) {
        List<UBSuser> allByUserId = ubsUserRepository.getAllByUserId(userId);
        if (allByUserId.isEmpty()) {
            return List.of(PersonalDataDto.builder().email(userRepository.findById(userId).get().getEmail()).build());
        }
        List<PersonalDataDto> personalDataDtoList =
            allByUserId.stream().map(u -> modelMapper.map(u, PersonalDataDto.class)).collect(Collectors.toList());

        return personalDataDtoList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CertificateDto checkCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code).orElse(null);

        return certificate == null ? new CertificateDto("")
            : new CertificateDto(certificate.getCertificateStatus().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void processOrder(OrderResponseDto dto, Long userId) {
        User currentUser = userRepository.findById(userId).get();

        Long ubsUserId = dto.getPersonalData().getId();
        UBSuser ubsUser;
        if (ubsUserId != null) {
            ubsUser = ubsUserRepository.findById(ubsUserId).get();
        } else {
            ubsUser = modelMapper.map(dto.getPersonalData(), UBSuser.class);
            ubsUser.setUser(currentUser);
            ubsUserRepository.save(ubsUser);
            currentUser.getUbsUsers().add(ubsUser);
        }

        Order order = modelMapper.map(dto, Order.class);
        order.setUbsUser(ubsUser);
        order.setUser(currentUser);

        currentUser.getOrders().add(order);
        currentUser.setCurrentPoints(currentUser.getCurrentPoints() - dto.getPointsToUse());
        currentUser.getChangeOfPoints().put(LocalDateTime.now(), -dto.getPointsToUse());

        Certificate certificate = order.getCertificate();
        if (certificate != null) {
            certificate.setCertificateStatus(CertificateStatus.USED);
        }

        userRepository.save(currentUser);
    }
}
