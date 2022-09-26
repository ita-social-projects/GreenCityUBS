package greencity.entity.notifications;

import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationType;
import lombok.*;
import lombok.experimental.Accessors;
import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "notification_templates")
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "notification_type", length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "title", length = 300)
    private String title;

    @Column(name = "body", length = 1500)
    private String body;

    @Column(name = "language_code")
    private String languageCode;

    @Column(nullable = false, name = "notification_receiver_type", length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationReceiverType notificationReceiverType;
}