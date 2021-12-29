package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDto {
    private Long id;
    private String notificationType;
    private String title;
    private String body;
    private String notificationReceiverType;
    private String language;
}
