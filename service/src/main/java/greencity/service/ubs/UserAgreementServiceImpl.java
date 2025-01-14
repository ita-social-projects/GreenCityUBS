package greencity.service.ubs;

import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.NotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.UserAgreementRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND_BY_EMAIL;
import static greencity.constant.ErrorMessage.USER_AGREEMENT_NOT_FOUND_BY_ID;

/**
 * Implementation of {@link UserAgreementService}.
 */
@Service
@AllArgsConstructor
public class UserAgreementServiceImpl implements UserAgreementService {
    private final EmployeeRepository employeeRepository;
    private final UserAgreementRepository userAgreementrepository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> findAllIdSortedByAsc() {
        List<UserAgreement> agreements = userAgreementrepository.findAllSortedByAsc();
        return agreements.stream()
            .map(UserAgreement::getId)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAgreementDto findLatest() {
        UserAgreement latest = userAgreementrepository.findLatestAgreement()
            .orElseThrow(() -> new NotFoundException("No user agreement found"));
        return modelMapper.map(latest, UserAgreementDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAgreementDetailDto create(UserAgreementDto userAgreementDto, String authorEmail) {
        UserAgreement userAgreement = modelMapper.map(userAgreementDto, UserAgreement.class);
        Employee employee = findEmployeeByEmail(authorEmail);
        userAgreement.setAuthor(employee);

        UserAgreement saved = userAgreementrepository.save(userAgreement);
        return modelMapper.map(saved, UserAgreementDetailDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAgreementDetailDto read(Long id) {
        UserAgreement userAgreement = findEntity(id);
        return modelMapper.map(userAgreement, UserAgreementDetailDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long id) {
        findEntity(id);
        userAgreementrepository.deleteById(id);
    }

    private UserAgreement findEntity(Long id) {
        return userAgreementrepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format(USER_AGREEMENT_NOT_FOUND_BY_ID, id)));
    }

    private Employee findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND_BY_EMAIL));
    }
}