package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateDto {
    private Long id;
    private String notificationType;
    private String title;
    private String body;
    private String notificationReceiverType;
    private String language;
}
