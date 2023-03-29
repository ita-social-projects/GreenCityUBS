package greencity.entity.notifications;

import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationStatus;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "notification_platforms")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "notificationTemplate")
public class NotificationPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id")
    private NotificationTemplate notificationTemplate;

    @Column(name = "body", length = 1500)
    private String body;

    @Column(name = "body_eng", length = 1500)
    private String bodyEng;

    @Column(nullable = false, name = "notification_receiver_type", length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationReceiverType notificationReceiverType;

    @Column(nullable = false, name = "notification_status")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;
}
