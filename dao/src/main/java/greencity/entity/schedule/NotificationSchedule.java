package greencity.entity.schedule;

import greencity.entity.enums.NotificationType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
@Table(name = "notification_schedule")
public class NotificationSchedule {
    @Id
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    private String cron;
}
