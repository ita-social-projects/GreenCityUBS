package greencity.service.ubs;

import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.ManualPaymentResponseDto;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.dto.refund.RefundDto;
import greencity.entity.order.Order;
import greencity.exceptions.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public interface PaymentService {
    /**
     * Method returns payment info.
     *
     * @param orderId  of {@link Long} order id
     * @param sumToPay of {@link Double} sum to pay
     * @return {@link PaymentTableInfoDto};
     * @author Struk Nazar
     */
    PaymentTableInfoDto getPaymentInfo(long orderId, Double sumToPay);

    /**
     * Method that saves manual payment and returns response with required fields.
     *
     * @param orderId           of {@link Long} order id;
     * @param paymentRequestDto of {@link ManualPaymentRequestDto} manual payment
     *                          request dto;
     * @param image             {@link MultipartFile} image of receipt.
     * @param email             {@link String}.
     * @return {@link ManualPaymentResponseDto }
     * @author Denys Kisliak
     */
    ManualPaymentResponseDto saveNewManualPayment(Long orderId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String email);

    /**
     * Method that deletes manual payment.
     *
     * @param paymentId of {@link Long} payment id;
     * @param uuid      {@link String}.
     * @author Denys Kisliak
     */
    void deleteManualPayment(Long paymentId, String uuid);

    /**
     * Method that updates manual payment and returns response with required fields.
     *
     * @param paymentId         of {@link Long} payment id;
     * @param paymentRequestDto of {@link ManualPaymentRequestDto} manual payment
     *                          request dto;
     * @param image             {@link MultipartFile} image of receipt.
     * @param uuid              {@link String}.
     * @return {@link ManualPaymentResponseDto }
     * @author Denys Kisliak
     */
    ManualPaymentResponseDto updateManualPayment(Long paymentId, ManualPaymentRequestDto paymentRequestDto,
        MultipartFile image, String uuid);

    /**
     * Processes a refund for an order.
     *
     * @param order         {@link Order} the order to process.
     * @param refundDto     {@link RefundDto} the details of the refund request.
     * @param employeeEmail {@link String} the email of the employee processing the
     *                      order.
     * @return {@code true} if the refund was successfully processed for orders with
     *         status {@code CANCELED} or {@code DONE}; {@code false} if the order
     *         status is {@code BROUGHT_IT_HIMSELF} and the refund wasn't processed.
     * @throws BadRequestException if the refund request is invalid or cannot be
     *                             processed due to the current order state or
     *                             refund details.
     * @author Volodymyr Lukovskyi
     */
    boolean processRefundForOrder(Order order, RefundDto refundDto,
        String employeeEmail);
}
