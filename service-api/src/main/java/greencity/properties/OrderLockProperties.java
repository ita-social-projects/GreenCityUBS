package greencity.properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Сlass for retrieving configuration values from the runtime environment. Used
 * to access dynamic properties. Provides a flexible alternative to @Value,
 * always getting the latest values without having to restart the application or
 * use /actuator/refresh.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderLockProperties {
    private final Environment environment;

    public int getOrderLockDuration() {
        Integer orderLockDuration = environment.getProperty("order.lock.duration.minutes", Integer.class);
        if (orderLockDuration == null) {
            log.error("OrderLockDuration property is empty");
        }
        return orderLockDuration != null ? orderLockDuration : 5;
    }
}
