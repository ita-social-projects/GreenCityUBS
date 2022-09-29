package greencity.filters;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilterCriteria {
    private String[] userRegistrationDate;
    private String[] orderDate;
    private String[] numberOfOrders;
    private String[] numberOfViolations;
    private String[] numberOfBonuses;
    private String search;
}
