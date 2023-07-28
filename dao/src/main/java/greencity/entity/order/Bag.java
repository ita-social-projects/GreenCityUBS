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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 30)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private TariffsInfo tariffsInfo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BagStatus status;
}
