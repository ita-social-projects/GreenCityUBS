package greencity.service.ubs;

import greencity.dto.PaymentRequestDtoLiqPay;
import greencity.dto.StatusRequestDtoLiqPay;

import java.util.Map;

public interface LiqPayService {
    /**
     * Method that get Data from Request to LiqPay.
     *
     * @param dto {@link PaymentRequestDtoLiqPay}
     * @return {@link String}
     * @author Vadym Makitra.
     */
    String getCheckoutResponse(PaymentRequestDtoLiqPay dto);

    /**
     * Method for getting info about LiqPay Status.
     * 
     * @param dto {@link StatusRequestDtoLiqPay}
     * @return {@link Map}
     * @author Vadym Makitra
     */
    Map<String, Object> getPaymentStatus(StatusRequestDtoLiqPay dto);
}
