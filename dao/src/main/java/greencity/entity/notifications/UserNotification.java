package greencity.entity.notifications;

import greencity.enums.NotificationType;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"order", "user", "parameters", "notificationTime"})
@ToString(exclude = {"order", "user", "parameters", "notificationTime"})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_notifications")
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private boolean read;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @Column(nullable = false, name = "notification_type", length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "template_id", columnDefinition = "bigint")
    private Long templateId;

    @OneToMany(mappedBy = "userNotification")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<NotificationParameter> parameters = new HashSet<>();

    @Column(name = "notification_time")
    private LocalDateTime notificationTime = getCurrentLocalTime();

    private static LocalDateTime getCurrentLocalTime() {
        return ZonedDateTime.now(ZoneId.of("Europe/Kiev")).toLocalDateTime();
    }
}
