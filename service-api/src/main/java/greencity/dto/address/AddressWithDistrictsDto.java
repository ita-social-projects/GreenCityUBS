package greencity.dto.address;

import greencity.dto.location.api.DistrictDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressWithDistrictsDto implements Serializable {
    private AddressDto addressDto;
    private List<DistrictDto> addressRegionDistrictList;
}