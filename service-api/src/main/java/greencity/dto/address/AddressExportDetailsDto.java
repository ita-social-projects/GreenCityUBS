package greencity.dto.address;

import greencity.dto.location.api.DistrictDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Builder
@Data
public class AddressExportDetailsDto {
    private Long id;
    private String cityUk;
    private String cityEn;
    private String districtUk;
    private String districtEn;
    private String regionUk;
    private String regionEn;
    private String entranceNumber;
    private String houseCorpus;
    private String houseNumber;
    private String streetUk;
    private String streetEn;
    private List<DistrictDto> addressRegionDistrictList;
}
