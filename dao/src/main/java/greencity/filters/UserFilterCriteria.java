package greencity.filters;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterCriteria {
    private String[] userRegistrationDate;
    private String[] orderDate;
    private String[] numberOfOrders;
    private String[] numberOfViolations;
    private String[] numberOfBonuses;
    private String search;
}
