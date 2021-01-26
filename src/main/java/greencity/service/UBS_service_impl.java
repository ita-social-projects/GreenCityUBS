package greencity.service;

import greencity.dao.entity.enums.CertificateStatus;
import greencity.dao.entity.order.Bag;
import greencity.dao.entity.order.Certificate;
import greencity.dao.entity.order.Order;
import greencity.dao.entity.user.User;
import greencity.dao.entity.user.ubs.UBSuser;
import greencity.dao.repository.BagRepository;
import greencity.dao.repository.CertificateRepository;
import greencity.dao.repository.UBSuserRepository;
import greencity.dao.repository.UserRepository;
import greencity.dto.CertificateDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UserPointsAndAllBagsDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link UBS_service}.
 */
@Service
@AllArgsConstructor
public class UBS_service_impl implements UBS_service {
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
                (List<Bag>) bagRepository.findAll()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<PersonalDataDto> getSecondPageData(Long userId) {
        List<UBSuser> ubs_users = ubsUserRepository.getAllByUserId(userId);
        if (ubs_users.isEmpty()) {
            return List.of(PersonalDataDto.builder().email(userRepository.findById(userId).get().getEmail()).build());
        }
        List<PersonalDataDto> personalDataDtoList =
                ubs_users.stream().map(u -> modelMapper.map(u, PersonalDataDto.class)).collect(Collectors.toList());

        return personalDataDtoList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CertificateDto checkCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code).orElse(null);

        return certificate == null ? new CertificateDto("") : new CertificateDto(certificate.getCertificate_status().toString()) ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void processOrder(OrderResponseDto dto, Long userId) {
        User currentUser = userRepository.findById(userId).get();

        Long ubs_user_id = dto.getPersonalData().getId();
        UBSuser ubs_user;
        if (ubs_user_id != null) {
            ubs_user = ubsUserRepository.findById(ubs_user_id).get();
        } else {
            ubs_user = modelMapper.map(dto.getPersonalData(), UBSuser.class);
            ubs_user.setUser(currentUser);
            ubsUserRepository.save(ubs_user);
            currentUser.getUbs_users().add(ubs_user);
        }

        Order order = modelMapper.map(dto, Order.class);
        order.setUbs_user(ubs_user);
        order.setUser(currentUser);

        currentUser.getOrders().add(order);
        currentUser.setCurrentPoints(currentUser.getCurrentPoints() - dto.getPointsToUse());
        currentUser.getChangeOfPoints().put(LocalDateTime.now(), -dto.getPointsToUse());

        Certificate certificate = order.getCertificate();
        if (certificate != null) {
            certificate.setCertificate_status(CertificateStatus.USED);
        }

        userRepository.save(currentUser);
    }
}
