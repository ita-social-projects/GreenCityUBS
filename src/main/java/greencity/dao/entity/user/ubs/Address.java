package greencity.dao.entity.user.ubs;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"comment", "ubs_user", "id"})
@Getter
@Setter
@Builder
@Table(name = "address")
public class Address {
    @OneToOne(mappedBy = "userAddress")
    private UBSuser ubs_user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(12) default 'Kyiv'", nullable = false, length = 20)
    private String city;

    @Column(nullable = false, length = 50)
    private String street;

    @Column(nullable = false, length = 30)
    private String district;

    @Column(name = "house_number", nullable = false, length = 5)
    private String houseNumber;

    @Column(length = 5)
    private String houseCorpus;

    @Column(name = "entrance_number", nullable = false, length = 4)
    private String entranceNumber;

    @Column
    private String comment;



}
