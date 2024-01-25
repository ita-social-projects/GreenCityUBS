package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationShortDto {
    private Long id;
    private Long orderId;
    private boolean read;
    private String title;
    private LocalDateTime notificationTime;
}
