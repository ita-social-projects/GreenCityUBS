package greencity.dto.notification;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record NotificationFullDto(
    Long id,
    Long orderId,
    boolean read,
    String title,
    String body,
    LocalDateTime notificationTime,
    List<String> images) {
}
