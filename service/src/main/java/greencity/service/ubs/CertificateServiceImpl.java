package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.pageble.PageableDto;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.enums.MinAmountOfBag;
import greencity.entity.order.Certificate;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.repository.CertificateCriteriaRepo;
import greencity.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository certificateRepository;
    private final CertificateCriteriaRepo certificateCriteriaRepo;
    private final ModelMapper modelMapper;

    @Override
    public void addCertificate(CertificateDtoForAdding add) {
        Certificate certificate = modelMapper.map(add, Certificate.class);
        certificateRepository.save(certificate);
    }

    @Override
    public void deleteCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code)
            .orElseThrow(() -> new NotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + code));
        if (certificate.getCertificateStatus().equals(CertificateStatus.EXPIRED)
            || certificate.getCertificateStatus().equals(CertificateStatus.USED)) {
            throw new BadRequestException(ErrorMessage.CERTIFICATE_STATUS);
        }
        certificateRepository.delete(certificate);
    }

    @Override
    public PageableDto<CertificateDtoForSearching> getCertificatesWithFilter(CertificatePage certificatePage,
        CertificateFilterCriteria certificateFilterCriteria) {
        Page<Certificate> certificates =
            certificateCriteriaRepo.findAllWithFilter(certificatePage, certificateFilterCriteria);
        return getAllCertificatesTranslationDto(certificates);
    }

    private PageableDto<CertificateDtoForSearching> getAllCertificatesTranslationDto(Page<Certificate> pages) {
        List<CertificateDtoForSearching> certificateForSearchingDTOS = pages
            .stream()
            .map(certificatesTranslations -> modelMapper.map(certificatesTranslations,
                CertificateDtoForSearching.class))
            .collect(Collectors.toList());
        return new PageableDto<>(
            certificateForSearchingDTOS,
            pages.getTotalElements(),
            pages.getPageable().getPageNumber(),
            pages.getTotalPages());
    }
}
