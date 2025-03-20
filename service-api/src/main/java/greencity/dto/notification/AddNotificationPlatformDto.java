package greencity.dto.notification;

import greencity.enums.NotificationReceiverType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class AddNotificationPlatformDto {
    @NotNull
    @NotBlank
    private String body;

    @NotNull
    @NotBlank
    private String bodyEng;

    @NotNull
    private NotificationReceiverType notificationReceiverType;
}
