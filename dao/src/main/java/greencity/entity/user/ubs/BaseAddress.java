package greencity.entity.user.ubs;

import greencity.enums.AddressStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseAddress {
    @Size(min = 1, max = 30, message = "Invalid region name")
    @Column(columnDefinition = "varchar(30)", nullable = false, name = "region_uk")
    private String regionUk;

    @Size(min = 1, max = 30, message = "Invalid city name")
    @Column(columnDefinition = "varchar(30) default 'Kyiv'", nullable = false, name = "city_uk")
    private String cityUk;

    @Size(min = 1, max = 50)
    @Column(nullable = false, name = "street_uk")
    private String streetUk;

    @Size(min = 1, max = 30)
    @Column(nullable = false, name = "district_uk")
    private String districtUk;

    @Size(min = 1, max = 10)
    @Column(name = "house_number", nullable = false)
    private String houseNumber;

    @Size(max = 5, message = "Invalid house corpus")
    @Column(name = "house_corpus")
    private String houseCorpus;

    @Size(max = 4, message = "Invalid entrance number")
    @Column(name = "entrance_number", nullable = false)
    private String entranceNumber;

    @Column(name = "address_comment", nullable = false)
    private String addressComment;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean actual;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AddressStatus addressStatus;

    @Column(nullable = false)
    private String cityEn;

    @Column(nullable = false)
    private String regionEn;

    @Column(nullable = false)
    private String streetEn;

    @Column(nullable = false)
    private String districtEn;
}
