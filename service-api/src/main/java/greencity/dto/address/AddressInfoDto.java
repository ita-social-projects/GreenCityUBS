package greencity.dto.address;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AddressInfoDto {
    private String addressCity;
    private String addressCityEng;
    private String addressRegion;
    private String addressRegionEng;
    private String addressStreet;
    private String addressStreetEng;
    private String addressDistinct;
    private String addressDistinctEng;
    private String addressComment;
    private String houseNumber;
    private String houseCorpus;
    private String entranceNumber;
}