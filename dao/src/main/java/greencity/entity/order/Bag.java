package greencity.entity.order;

import greencity.entity.user.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Builder;

import javax.persistence.*;
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

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer commission;

    @Column(nullable = false)
    private Integer fullPrice;

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

    @ManyToOne
    @JoinColumn(nullable = false)
    private TariffsInfo tariffsInfo;
}
