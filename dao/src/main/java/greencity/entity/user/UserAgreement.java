package greencity.entity.user;

import greencity.entity.user.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "user_agreements")
@EqualsAndHashCode(exclude = "author")
@Entity
public class UserAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_ua", nullable = false, columnDefinition = "TEXT")
    private String textUa;

    @Column(name = "text_en", nullable = false, columnDefinition = "TEXT")
    private String textEn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Employee author;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now(ZoneId.of("Europe/Kiev")).toLocalDateTime();
    }
}
