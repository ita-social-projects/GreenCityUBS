package greencity.entity.notifications;

import greencity.entity.enums.NotificationType;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Certificate;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import lombok.Data;
import org.hibernate.annotations.Cascade;

import javax.management.Notification;
import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "user_notifications")
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @Column(nullable = false, name = "notification_type", length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @OneToMany(mappedBy = "userNotification")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<NotificationParameter> parameters;
}
