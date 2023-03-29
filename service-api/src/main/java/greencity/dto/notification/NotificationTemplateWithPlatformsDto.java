package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateWithPlatformsDto {
    @NotNull
    private NotificationTemplateMainInfoDto notificationTemplateMainInfoDto;
    @NotNull
    private List<NotificationPlatformDto> platforms = new ArrayList<>();
}
