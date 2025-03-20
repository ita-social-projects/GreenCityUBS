package greencity.dto.address;

import greencity.dto.location.api.DistrictDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Builder
@Data
public class AddressExportDetailsDto {
    private Long id;
    private String city;
    private String cityEn;
    private String district;
    private String districtEn;
    private String region;
    private String regionEn;
    private String entranceNumber;
    private String houseCorpus;
    private String houseNumber;
    private String street;
    private String streetEn;
    private List<DistrictDto> addressRegionDistrictList;
}
