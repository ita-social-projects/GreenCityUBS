package greencity.entity.order;

import greencity.entity.user.employee.Employee;
import greencity.enums.ServiceStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"tariffsInfo", "editedBy", "editedAt"})
@ToString(exclude = {"tariffsInfo"})
@Table(name = "service")

public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nameEng;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String descriptionEng;

    @Column(nullable = false)
    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;

    @Column
    private LocalDate editedAt;

    @ManyToOne
    @JoinColumn(name = "edited_by")
    private Employee editedBy;

    @OneToOne
    private TariffsInfo tariffsInfo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceStatus status;
}
