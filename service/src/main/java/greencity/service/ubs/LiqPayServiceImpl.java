package greencity.service.ubs;

import com.liqpay.LiqPay;
import greencity.dto.payment.PaymentRequestDtoLiqPay;
import greencity.dto.payment.StatusRequestDtoLiqPay;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link LiqPayService}.
 */
@Service
@RequiredArgsConstructor
public class LiqPayServiceImpl implements LiqPayService {
    private final LiqPay liqPay;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCheckoutResponse(PaymentRequestDtoLiqPay dto) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", dto.getAction());
        params.put("amount", dto.getAmount().toString());
        params.put("currency", dto.getCurrency());
        params.put("description", dto.getDescription());
        params.put("order_id", dto.getOrderId());
        params.put("version", dto.getVersion().toString());
        params.put("public_key", dto.getPublicKey());
        params.put("language", dto.getLanguage());
        params.put("result_url", dto.getResultUrl());
        params.put("paytypes", dto.getPaytypes());
        return liqPay.cnb_form(params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Map<String, Object> getPaymentStatus(StatusRequestDtoLiqPay dto) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", dto.getAction());
        params.put("version", dto.getVersion().toString());
        params.put("order_id", dto.getOrderId());
        return liqPay.api("request", params);
    }
}
