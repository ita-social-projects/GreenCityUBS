package greencity.dto.notification;

import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
