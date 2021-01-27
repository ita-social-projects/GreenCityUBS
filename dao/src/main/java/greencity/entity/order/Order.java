package greencity.entity.order;

import java.time.LocalDateTime;
import java.util.Map;
import javax.persistence.*;

import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "orders")
@ToString(exclude = {"id", "user", "ubs_user"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "ubs_user_id")
    private UBSuser ubsUser;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @ElementCollection
    @CollectionTable(name = "order_bag_mapping",
        joinColumns = {@JoinColumn(name = "order_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "bag_id")
    @Column(name = "amount")
    private Map<Integer, Integer> amountOfBagsOrdered;

    @Column(name = "additional_order")
    private String additionalOrder;

    @Column
    private String comment;

    @Column(columnDefinition = "int default 0")
    private Integer pointsToUse;

    @JoinColumn(name = "certificate_code")
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Certificate certificate;
}
