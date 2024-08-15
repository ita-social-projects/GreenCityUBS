package greencity.service.ubs;

import greencity.dto.pageble.PageableDto;
import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import greencity.exceptions.NotFoundException;
import greencity.repository.UserAgreementRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import static greencity.constant.ErrorMessage.USER_AGREEMENT_NOT_FOUND_BY_ID;

/**
 * Implementation of {@link UserAgreementService}.
 */
@Service
@AllArgsConstructor
public class UserAgreementServiceImpl implements UserAgreementService {
    private final UserAgreementRepository repository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<UserAgreementDetailDto> findAll(Pageable pageable) {
        Page<UserAgreement> page = repository.findAll(pageable);

        List<UserAgreementDetailDto> agreements = page.getContent().stream()
            .map(userAgreement -> modelMapper.map(userAgreement, UserAgreementDetailDto.class))
            .toList();

        return new PageableDto<>(
            agreements,
            page.getTotalElements(),
            page.getNumber(),
            page.getSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAgreementDto findLatest() {
        UserAgreement latest = repository.findLatestAgreement()
            .orElseThrow(() -> new NotFoundException("No user agreement found"));
        return modelMapper.map(latest, UserAgreementDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAgreementDetailDto create(UserAgreementDto userAgreementDto) {
        UserAgreement userAgreement = modelMapper.map(userAgreementDto, UserAgreement.class);
        UserAgreement saved = repository.save(userAgreement);
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
    public UserAgreementDetailDto update(Long id, UserAgreementDto userAgreementDto) {
        UserAgreement existingAgreement = findEntity(id);

        existingAgreement.setTextUa(userAgreementDto.getTextUa());
        existingAgreement.setTextEn(userAgreementDto.getTextEn());

        UserAgreement updatedAgreement = repository.save(existingAgreement);
        return modelMapper.map(updatedAgreement, UserAgreementDetailDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long id) {
        findEntity(id);
        repository.deleteById(id);
    }

    private UserAgreement findEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format(USER_AGREEMENT_NOT_FOUND_BY_ID, id)));
    }
}