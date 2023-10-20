package greencity.entity.notifications;

import lombok.*;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude = {"userNotification"})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification_parameters")
public class NotificationParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    @ToString.Exclude
    private UserNotification userNotification;

    @Column(nullable = false, name = "key", length = 50)
    private String key;

    @Column(nullable = false, name = "value", length = 100)
    private String value;
}