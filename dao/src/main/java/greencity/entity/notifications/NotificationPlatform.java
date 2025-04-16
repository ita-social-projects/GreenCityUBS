package greencity.entity.notifications;

import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    @Column(name = "body")
    private String body;

    @Column(name = "body_eng")
    private String bodyEng;

    @Column(nullable = false, name = "notification_receiver_type")
    @Enumerated(EnumType.STRING)
    private NotificationReceiverType notificationReceiverType;

    @Column(nullable = false, name = "notification_status")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;
}
