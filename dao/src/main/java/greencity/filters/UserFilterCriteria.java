package greencity.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
