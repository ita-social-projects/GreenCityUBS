package greencity.entity.order;

import greencity.entity.user.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Min(1)
    @Max(99_999_999)
    @Column(nullable = false)
    private Long price;

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
    @JoinColumn(nullable = false, name = "created_by")
    private Employee createdBy;

    @Column
    private LocalDate editedAt;

    @ManyToOne
    @JoinColumn(name = "edited_by")
    private Employee editedBy;

    @OneToOne
    private TariffsInfo tariffsInfo;
}
