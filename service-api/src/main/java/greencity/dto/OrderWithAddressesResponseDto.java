package greencity.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OrderWithAddressesResponseDto {
    private List<AddressDto> addressList;
}
