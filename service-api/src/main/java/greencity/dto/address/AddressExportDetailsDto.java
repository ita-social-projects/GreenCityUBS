package greencity.dto.address;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class AddressExportDetailsDto {
    private Long addressId;
    private String addressCity;
    private String addressCityEng;
    private String addressDistrict;
    private String addressDistrictEng;
    private String addressRegion;
    private String addressRegionEng;
    private String addressEntranceNumber;
    private String addressHouseCorpus;
    private String addressHouseNumber;
    private String addressStreet;
    private String addressStreetEng;
}
