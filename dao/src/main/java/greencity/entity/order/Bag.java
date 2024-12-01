package greencity.entity.order;

import greencity.entity.user.employee.Employee;
import greencity.enums.BagStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = {"tariffsInfo"})
@ToString(exclude = {"tariffsInfo"})
@Builder
@Table(name = "bag")
public class Bag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Min(1)
    @Max(999)
    @Column(nullable = false)
    private Integer capacity;

    @Min(1)
    @Max(99_999_999)
    @Column(nullable = false)
    private Long price;

    @Min(0)
    @Max(99_999_999)
    @Column(nullable = false)
    private Long commission;

    @Min(1)
    @Column(nullable = false)
    private Long fullPrice;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nameEng;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String descriptionEng;

    @Column(nullable = false)
    private Boolean limitIncluded;

    @Column(nullable = false)
    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Employee createdBy;

    @Column
    private LocalDate editedAt;

    @ManyToOne
    @JoinColumn
    private Employee editedBy;

    @ManyToOne(cascade = CascadeType.REMOVE,
        fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private TariffsInfo tariffsInfo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BagStatus status;
}
