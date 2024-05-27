package greencity.entity.notifications;

import greencity.enums.NotificationStatus;
import greencity.enums.NotificationTime;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
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

    @Column(unique = true, name = "template_uuid", columnDefinition = "varchar(60)")
    private String templateUuid;

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

    @Builder.Default
    @Column(name = "is_schedule_update_forbidden", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isScheduleUpdateForbidden = false;

    public void addPlatforms(List<NotificationPlatform> notificationPlatforms) {
        notificationPlatforms.forEach(platform -> platform.setNotificationTemplate(this));
        this.notificationPlatforms.addAll(notificationPlatforms);
    }
}