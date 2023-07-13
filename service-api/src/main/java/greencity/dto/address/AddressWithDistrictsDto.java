package greencity.dto.address;

import greencity.dto.location.api.DistrictDto;
import greencity.entity.coords.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

import static greencity.constant.ValidationConstant.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressWithDistrictsDto implements Serializable {
    private AddressDto addressDto;
    private List<DistrictDto> addressRegionDistrictList;
}