package greencity.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPage {
    private int pageNumber = 0;
    private int pageSize = 10;
}
