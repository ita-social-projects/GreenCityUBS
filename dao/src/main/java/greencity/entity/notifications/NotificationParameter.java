package greencity.entity.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@EqualsAndHashCode
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_parameters")
public class NotificationParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private UserNotification userNotification;

    @Column(nullable = false, name = "key", length = 50)
    private String key;

    @Column(nullable = false, name = "value", length = 100)
    private String value;

}
