package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.enums.CertificateStatus;
import greencity.entity.order.Certificate;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.repository.CertificateCriteriaRepo;
import greencity.repository.CertificateRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    @InjectMocks
    CertificateServiceImpl certificateService;

    @Mock
    CertificateCriteriaRepo certificateCriteriaRepo;

    @Mock
    CertificateRepository certificateRepository;

    @Mock
    private ModelMapper modelMapper;

    @Test
    void addCertificateTest() {
        CertificateDtoForAdding certificateDtoForAdding = new CertificateDtoForAdding("1111-1234", 5, 100, 100);
        Certificate certificate = new Certificate();
        when(modelMapper.map(certificateDtoForAdding, Certificate.class)).thenReturn(certificate);
        certificateService.addCertificate(certificateDtoForAdding);
        verify(certificateRepository, times(1)).save(certificate);
    }

    @Test
    void addCertificateBadRequestException() {
        CertificateDtoForAdding certificate = ModelUtils.getCertificateDtoForAdding();
        when(certificateRepository.existsCertificateByCode("1111-1234")).thenReturn(true);

        assertThrows(BadRequestException.class,
            () -> certificateService.addCertificate(certificate));

        verify(certificateRepository).existsCertificateByCode("1111-1234");
        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    @Test
    void deleteCertificateTest() {
        Certificate certificate = new Certificate();
        certificate.setCode("1111-1234")
            .setCertificateStatus(CertificateStatus.ACTIVE);
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(certificate));
        certificateService.deleteCertificate("1111-1234");
        verify(certificateRepository, times(1)).delete(certificate);
    }

    @Test
    void deleteCertificateNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> certificateService.deleteCertificate("1111-1234"));
    }

    @Test
    void deleteCertificateBadRequest() {
        Certificate certificate = new Certificate();
        certificate.setCode("1111-1234")
            .setCertificateStatus(CertificateStatus.EXPIRED);
        when(certificateRepository.findById("1111-1234")).thenReturn(Optional.of(certificate));
        Assertions.assertThrows(BadRequestException.class, () -> certificateService.deleteCertificate("1111-1234"));
    }

    @Test
    void getCertificatesWithFilter() {
        CertificateFilterCriteria certificateFilterCriteria = new CertificateFilterCriteria();
        CertificatePage certificatePage = new CertificatePage();
        List<Certificate> certificateList = Arrays.asList(new Certificate(), new Certificate());
        Pageable pageable = PageRequest.of(certificatePage.getPageNumber(), certificatePage.getPageSize());
        Page<Certificate> certificates = new PageImpl<>(certificateList, pageable, 1L);

        when(certificateCriteriaRepo.findAllWithFilter(certificatePage, certificateFilterCriteria))
            .thenReturn(certificates);

        certificateService.getCertificatesWithFilter(certificatePage, certificateFilterCriteria);
        verify(certificateCriteriaRepo).findAllWithFilter(certificatePage, certificateFilterCriteria);
        assertEquals(certificateCriteriaRepo.findAllWithFilter(certificatePage, certificateFilterCriteria),
            certificates);
    }
}
