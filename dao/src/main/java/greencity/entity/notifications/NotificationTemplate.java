package greencity.entity.notifications;

import greencity.enums.*;
import lombok.*;
import lombok.experimental.Accessors;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "notification_templates")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "notificationPlatforms")
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
    private List<NotificationPlatform> notificationPlatforms;

    @Column(nullable = false, name = "notification_type", length = 50)
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

    @Column(name = "title", length = 300)
    private String title;

    @Column(name = "title_eng", length = 300)
    private String titleEng;
}