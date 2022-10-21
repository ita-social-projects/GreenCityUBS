package greencity.entity.schedule;

import greencity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Builder
@Entity
@Data
@Accessors(chain = true)
@Table(name = "notification_schedule")
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSchedule {
    @Id
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    private String cron;
}
