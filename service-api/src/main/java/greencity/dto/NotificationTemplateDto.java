package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDto {
    @NotNull
    private Long id;
    private String notificationType;
    @NotNull
    private String title;
    @NotNull
    private String body;
    private String notificationReceiverType;
}
