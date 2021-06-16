package greencity.filters;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String orderStatus;
    private Integer violationsAmount;
    private String payment;
    private String receivingStation;
    private String orderDate;
    private String district;
}
