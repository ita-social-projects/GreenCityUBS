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

    /**
     * Constructor.
     */
    public NotificationParameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private UserNotification userNotification;

    @Column(nullable = false, name = "key", length = 50)
    private String key;

    @Column(nullable = false, name = "value", length = 100)
    private String value;
}