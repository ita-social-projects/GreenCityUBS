package greencity.entity.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"order", "bag"})
@ToString(exclude = {"order", "bag"})
@Getter
@Setter
@Builder
@Table(name = "order_bag_mapping")
public class OrderBag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bag_id")
    private Bag bag;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "confirmed_quantity")
    private Integer confirmedQuantity;

    @Column(name = "exported_quantity")
    private Integer exportedQuantity;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Long price;

    @NotBlank
    @Size(min = 1, max = 30)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Size(min = 1, max = 30)
    @Column(nullable = false)
    private String nameEng;
}
