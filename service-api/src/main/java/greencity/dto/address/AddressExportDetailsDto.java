package greencity.dto.address;

import greencity.dto.location.api.DistrictDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Builder
@Data
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
    private List<DistrictDto> addressRegionDistrictList;
}
