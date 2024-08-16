package greencity.entity.user;

import greencity.entity.user.employee.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
        createdAt = LocalDateTime.now();
    }
}
