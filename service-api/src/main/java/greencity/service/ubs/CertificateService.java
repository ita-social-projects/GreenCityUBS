package greencity.service.ubs;

import greencity.dto.certificate.CertificateDto;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.pageble.PageableDto;
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
     * Method delete a certificates.
     *
     * @author Hlazova Nataliia
     */
    void deleteCertificate(String code);

    /**
     * Method returns all certificates with filtering and sorting data.
     *
     * @return List of {@link greencity.entity.order.Certificate} lists.
     * @author Sikhovskiy Rostyslav
     */
    PageableDto<CertificateDtoForSearching> getCertificatesWithFilter(CertificatePage certificatePage,
        CertificateFilterCriteria certificateFilterCriteria);

    /**
     * Method returns the status of the entered certificate.
     *
     * @param code {@link String} code of certificate.
     * @param userUuid {@link String} uuid of the user.
     * @return {@link CertificateDto} which contains status.
     * @author Oleh Bilonizhka
     */
    CertificateDto checkCertificate(String code, String userUuid);
}
