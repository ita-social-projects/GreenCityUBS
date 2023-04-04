package greencity.entity.notifications;

import greencity.enums.*;
import lombok.*;
import lombok.experimental.Accessors;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notification_templates")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
@Accessors(chain = true)
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(
        mappedBy = "notificationTemplate",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true)
    @Setter(AccessLevel.PRIVATE)
    private List<NotificationPlatform> notificationPlatforms = new ArrayList<>();

    @Column(nullable = false, name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(nullable = false, name = "trigger")
    @Enumerated(EnumType.STRING)
    private NotificationTrigger trigger;

    @Column(nullable = false, name = "time")
    @Enumerated(EnumType.STRING)
    private NotificationTime time;

    @Column(name = "schedule")
    private String schedule;

    @Column(nullable = false, name = "notification_status")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Column(name = "title")
    private String title;

    @Column(name = "title_eng")
    private String titleEng;

    /**
     * helper method, that allows to save NotificationPlatform entity in database
     * correctly.
     *
     * @param platform notification platform for NotificationTemplate
     *                 {@link NotificationPlatform}
     * @author Safarov Renat
     */
    public void addNotificationPlatform(NotificationPlatform platform) {
        platform.setNotificationTemplate(this);
        notificationPlatforms.add(platform);
    }

    /**
     * helper method, that allows to remove NotificationPlatform entity from
     * database correctly.
     *
     * @param platform notification platform for NotificationTemplate
     *                 {@link NotificationPlatform}
     * @author Safarov Renat
     */
    public void removeNotificationPlatform(NotificationPlatform platform) {
        platform.setNotificationTemplate(null);
        notificationPlatforms.remove(platform);
    }
}