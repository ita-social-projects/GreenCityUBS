package greencity.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderResponseDto implements Serializable {
    @Valid
    private List<BagDto> bags;
    @NotNull

    @NotNull
    @Min(0)
    private Integer pointsToUse;

    @NotNull
    private Long addressId;

    private Set<@Pattern(regexp = "(\\d{4}-\\d{4})|(^$)",
        message = "This certificate code is not valid") String> certificates;

    private Set<@Length(max = 10) String> additionalOrders;

    @Length(max = 170)
    private String orderComment;

    @Valid
    private PersonalDataDto personalData;

    @NotNull
    Long minAmountOfBigBags;
}
