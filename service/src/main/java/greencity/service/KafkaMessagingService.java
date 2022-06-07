package greencity.service;

import greencity.dto.useraction.UserActionMessage;
import greencity.entity.enums.UserActionType;
import greencity.entity.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class KafkaMessagingService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String USER_ACTIONS_TOPIC = "greencity.user.actions";

    /**
     * Sends a {@link UserActionMessage} in the {@code greencity.user.actions} topic
     * with {@link UserActionType} {@code UBS_ORDER_DONE}.
     *
     * @param order {@link Order} that has been done.
     */
    public void sendOrderDoneEvent(Order order) {
        kafkaTemplate.send(USER_ACTIONS_TOPIC,
            UserActionMessage.builder()
                .userEmail(order.getUser().getRecipientEmail())
                .actionType(UserActionType.UBS_ORDER_DONE)
                .actionId(order.getId())
                .timestamp(ZonedDateTime.now().toString()).build());
    }
}
