package greencity.dto.notification;

import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

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
    private String nameEn;
    @NotNull
    private String bodyUk;
    @NotNull
    private String bodyEn;
    @NotNull
    private NotificationStatus status;
}
