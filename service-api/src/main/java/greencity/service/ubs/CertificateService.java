package greencity.service.ubs;

import greencity.dto.certificate.CertificateDto;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import java.util.List;
import java.util.Set;

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

    long formCertificatesToBeSavedAndCalculateOrderSumClient(OrderWayForPayClientDto dto,
        Order order,
        long sumToPayInCoins);

    long formCertificatesToBeSavedAndCalculateOrderSum(OrderResponseDto dto, Set<Certificate> orderCertificates,
                                                       Order order, long sumToPayInCoins);

    Integer countCertificatesBonuses(List<CertificateDto> certificateDtos);
}
