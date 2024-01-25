package greencity.entity.notifications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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