package greencity.service.ubs.payment;

import greencity.enums.PaymentSystem;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PaymentStrategyFactory {
    private final Map<PaymentSystem, PaymentStrategy> strategies = new EnumMap<>(PaymentSystem.class);

    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        for (PaymentStrategy strategy : strategyList) {
            strategies.put(strategy.getPaymentSystem(), strategy);
        }
    }

    public PaymentStrategy getPaymentStrategy(PaymentSystem paymentSystem) {
        PaymentStrategy strategy = strategies.get(paymentSystem);
        if (strategy == null) {
            throw new IllegalArgumentException("No payment strategy found for: " + paymentSystem);
        }
        return strategy;
    }
}
