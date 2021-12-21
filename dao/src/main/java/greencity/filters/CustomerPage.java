package greencity.filters;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CustomerPage {
    private int pageNumber = 0;
    private int pageSize = 10;
}
