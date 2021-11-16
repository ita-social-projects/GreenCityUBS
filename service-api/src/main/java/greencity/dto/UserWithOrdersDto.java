package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserWithOrdersDto {
    private List<UserOrdersDto> userOrdersList;
}
