package greencity.filters;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPage {
    private int pageNumber = 0;
    private int pageSize = 10;
}
