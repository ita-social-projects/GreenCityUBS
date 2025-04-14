package greencity.dto.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AddressInfoDto {
    private String addressCityUk;
    private String addressCityEn;
    private String addressRegionUk;
    private String addressRegionEn;
    private String addressStreetUk;
    private String addressStreetEn;
    private String addressDistinctUk;
    private String addressDistinctEn;
    private String addressComment;
    private String houseNumber;
    private String houseCorpus;
    private String entranceNumber;
}