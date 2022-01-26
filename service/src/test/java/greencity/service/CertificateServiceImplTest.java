package greencity.service;

import greencity.dto.CertificateDtoForAdding;
import greencity.entity.order.Certificate;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.repository.CertificateCriteriaRepo;
import greencity.repository.CertificateRepository;
import greencity.service.ubs.CertificateServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceImplTest {

    @InjectMocks
    CertificateServiceImpl certificateService;

    @Mock
    CertificateCriteriaRepo certificateCriteriaRepo;

    @Mock
    CertificateRepository certificateRepository;

    @Mock(lenient = true)
    private ModelMapper modelMapper;

    @Test
    void addCertificateTest() {
        CertificateDtoForAdding certificateDtoForAdding = new CertificateDtoForAdding("1111-1234", 5, 100);
        Certificate certificate = new Certificate();
        when(modelMapper.map(certificateDtoForAdding, Certificate.class)).thenReturn(certificate);
        certificateService.addCertificate(certificateDtoForAdding);
        verify(certificateRepository, times(1)).save(certificate);
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
