package greencity.entity.user;

import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.ubs.UBSuser;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import greencity.entity.viber.ViberBot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private Set<UBSuser> ubsUsers;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Order> orders;

    @Column(columnDefinition = "int default 0")
    private Integer currentPoints;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
    private List<ChangeOfPoints> changeOfPointsList;

    @ElementCollection
    @CollectionTable(name = "violations_description_mapping",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "order_id")
    @Column(name = "description")
    private Map<Long, String> violationsDescription;

    @Column
    private Integer violations;

    @Column(nullable = false, columnDefinition = "varchar(60)")
    private String uuid;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private TelegramBot telegramBot;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private ViberBot viberBot;
}
