package greencity.filters;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCriteria {
    private String[] userName;
    private String[] userEmail;
    private String registeredFrom;
    private String registeredTo;
    private int numberOfOrders;
}
