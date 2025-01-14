package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class UserWithOrdersDto {
    private String username;
    private List<UserOrdersDto> userOrdersList;
}
