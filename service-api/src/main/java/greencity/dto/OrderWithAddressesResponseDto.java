package greencity.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderWithAddressesResponseDto {
    private List<AddressDto> addressList;
}
