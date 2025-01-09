package greencity.dto.notification;

import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPlatformDto {
    @NotNull
    private Long id;
    @NotNull
    private NotificationReceiverType receiverType;
    @NotNull
    private String nameEng;
    @NotNull
    private String body;
    @NotNull
    private String bodyEng;
    @NotNull
    private NotificationStatus status;
}
