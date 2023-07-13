package greencity.dto.order;

import greencity.dto.address.AddressWithDistrictsDto;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class OrderWithAddressesResponseDto {
    private List<AddressWithDistrictsDto> addressList;
}
