package greencity.service.ubs.calculator;

import greencity.dto.certificate.CertificateDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import java.util.List;
import java.util.Set;

/**
 * Service for calculating and applying certificate discounts to orders.
 * Provides methods to determine how certificates affect the final sum to pay.
 */
public interface CertificateCalculatorService {
    /**
     * Calculates the order amount in coins after applying all certificates linked
     * to the order (but before applying client-specific certificates).
     *
     * @param orderId           the order id to process
     * @param sumToPayInCoins initial amount to pay in coins (before certificates)
     * @return adjusted amount after applying certificates
     */
    long getCertificateSumToPayInCoins(Long orderId, long sumToPayInCoins);

    /**
     * Applies client-specific certificates to the order and updates the amount to
     * pay. Certificates are applied after points, since points must reduce the sum
     * before certificates. Certificates are marked as used if fully consumed. If a
     * certificate's value exceeds the remaining sum to pay, its balance is reduced
     * accordingly and the unused amount remains available for future orders.
     *
     * @param dto             request DTO containing payment/order details from
     *                        client
     * @param orderId           the order id to update
     * @param sumToPayInCoins current sum to pay in coins
     * @return adjusted amount after applying client certificates
     */
    long applyCertificatesForClientOrder(OrderWayForPayClientDto dto,
        Long orderId,
        long sumToPayInCoins);

    /**
     * Applies provided certificates to the order and recalculates the sum to pay.
     * Certificates are marked as used if fully consumed. If a certificate's value
     * exceeds the remaining sum to pay, its balance is reduced accordingly and the
     * unused amount remains available for future orders.
     *
     * @param dto               response DTO representing order details
     * @param orderCertificates set of certificates to apply
     * @param orderId             the order id to update
     * @param sumToPayInCoins   current sum to pay in coins
     * @return adjusted amount after applying the given certificates
     */
    long applyCertificatesToOrder(OrderResponseDto dto, Set<CertificateDto> orderCertificates,
        Long orderId, long sumToPayInCoins);

    /**
     * Counts the total bonus amount available from the given certificates.
     *
     * @param certificateDtos list of certificate DTOs
     * @return total bonus value as integer
     */
    Integer countCertificatesBonuses(List<CertificateDto> certificateDtos);
}
