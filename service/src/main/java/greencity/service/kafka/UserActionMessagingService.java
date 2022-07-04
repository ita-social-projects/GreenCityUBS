package greencity.service.kafka;

import greencity.dto.useraction.UserActionMessage;
import greencity.entity.enums.ActionContextType;
import greencity.entity.enums.UserActionType;
import greencity.entity.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class UserActionMessagingService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${kafka.enable}")
    private boolean enabled;
    @Value("${kafka.topic.user.actions}")
    private String topic;

    private void sendMessage(UserActionMessage message) {
        if (enabled) {
            kafkaTemplate.send(topic, message);
        }
    }

    /**
     * Sends a {@link UserActionMessage} in the {@code greencity.user.actions} topic
     * with {@link UserActionType} {@code UBS_ORDER_DONE}.
     *
     * @param order {@link Order} that has been done.
     */
    public void sendOrderDoneEvent(Order order) {
        sendMessage(UserActionMessage.builder()
            .userEmail(order.getUser().getRecipientEmail())
            .actionType(UserActionType.UBS_ORDER_DONE)
            .contextType(ActionContextType.UBS_ORDER)
            .contextId(order.getId())
            .timestamp(ZonedDateTime.now()).build());
    }
}
