package greencity.entity.user;

import greencity.enums.ViolationLevel;
import greencity.entity.order.Order;
import greencity.enums.ViolationStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "violations_description_mapping")
@EqualsAndHashCode(exclude = {"description", "violationLevel", "violationDate",})
@Entity
public class Violation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "description")
    private String description;

    @Column(name = "violation_date")
    private LocalDateTime violationDate;

    @ElementCollection
    @CollectionTable(name = "violation_images",
        joinColumns = {@JoinColumn(name = "violation_id", referencedColumnName = "id")})
    @Column(name = "image")
    private List<String> images;

    @Column(nullable = false, name = "violation_level", length = 15)
    @Enumerated(EnumType.STRING)
    private ViolationLevel violationLevel;

    @ManyToOne
    @JoinColumn(name = "added_by_user_id")
    private User addedByUser;

    @Column(nullable = false, name = "violation_status", length = 15)
    @Enumerated(EnumType.STRING)
    private ViolationStatus violationStatus;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;
}
