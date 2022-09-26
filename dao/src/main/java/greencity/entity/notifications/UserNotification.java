package greencity.entity.notifications;

import greencity.enums.NotificationType;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"order", "user", "parameters", "notificationTime"})
@Entity
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

    @OneToMany(mappedBy = "userNotification")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<NotificationParameter> parameters = new HashSet<>();

    @Column(name = "notification_time")
    private LocalDateTime notificationTime = LocalDateTime.now();
}
