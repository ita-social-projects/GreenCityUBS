package greencity.service.ubs;

import greencity.dto.CertificateDtoForAdding;
import greencity.dto.CertificateDtoForSearching;
import greencity.dto.PageableDto;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;

public interface CertificateService {
    /**
     * Method add a certificates.
     *
     * @author Nazar Struk
     */
    void addCertificate(CertificateDtoForAdding add);

    /**
     * Method returns all certificates with filtering and sorting data.
     *
     * @return List of {@link greencity.entity.order.Certificate} lists.
     * @author Sikhovskiy Rostyslav
     */
    PageableDto<CertificateDtoForSearching> getCertificatesWithFilter(CertificatePage certificatePage,
        CertificateFilterCriteria certificateFilterCriteria);
}
