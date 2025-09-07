package greencity.service.ubs;

import static greencity.constant.ErrorMessage.CERTIFICATE_EXIST;
import static greencity.constant.ErrorMessage.CERTIFICATE_EXPIRED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_NOT_ACTIVATED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_USED;
import static greencity.constant.ErrorMessage.CERTIFICATE_NOT_FOUND;
import static greencity.constant.ErrorMessage.CERTIFICATE_NOT_FOUND_BY_CODE;
import static greencity.constant.ErrorMessage.SOME_CERTIFICATES_ARE_INVALID;
import static greencity.constant.ErrorMessage.TOO_MANY_CERTIFICATES;
import static java.util.stream.Collectors.joining;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.enums.CertificateStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.certificate.CertificateIsNotActivated;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.repository.CertificateCriteriaRepo;
import greencity.repository.CertificateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

@org.springframework.stereotype.Service
@Data
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository certificateRepository;
    private final CertificateCriteriaRepo certificateCriteriaRepo;
    private final ModelMapper modelMapper;

    @Override
    public void addCertificate(CertificateDtoForAdding add) {
        if (certificateRepository.existsCertificateByCode(add.getCode())) {
            throw new BadRequestException(CERTIFICATE_EXIST);
        } else {
            add.setPoints(add.getInitialPointsValue());
            Certificate certificate = modelMapper.map(add, Certificate.class);
            certificateRepository.save(certificate);
        }
    }

    @Override
    public void deleteCertificate(String code) {
        Certificate certificate = certificateRepository.findById(code)
            .orElseThrow(() -> new NotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + code));
        if (CertificateStatus.EXPIRED.equals(certificate.getCertificateStatus())
            || CertificateStatus.USED.equals(certificate.getCertificateStatus())) {
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
