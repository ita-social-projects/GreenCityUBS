package greencity.service.ubs.calculator;

import greencity.dto.certificate.CertificateDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import java.util.List;
import java.util.Set;

//TODO add docs
//TODO add tests
public interface CertificateCalculatorService {
    long getCertificateSumToPayInCoins(Order order, long sumToPayInCoins);

    long formCertificatesToBeSavedAndCalculateOrderSumClient(OrderWayForPayClientDto dto,
                                                             Order order,
                                                             long sumToPayInCoins);

    long formCertificatesToBeSavedAndCalculateOrderSum(OrderResponseDto dto, Set<Certificate> orderCertificates,
                                                       Order order, long sumToPayInCoins);

    Integer countCertificatesBonuses(List<CertificateDto> certificateDtos);
}
