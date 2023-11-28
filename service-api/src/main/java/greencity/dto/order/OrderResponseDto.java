package greencity.dto.order;

import greencity.dto.bag.BagDto;
import greencity.dto.user.PersonalDataDto;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
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
    @NotEmpty
    private List<BagDto> bags;

    @NotNull
    @Min(0)
    private Integer pointsToUse;

    @NotNull
    private Long addressId;

    private Set<@Pattern(regexp = "(\\d{4}-\\d{4})|(^$)",
        message = "This certificate code is not valid") String> certificates;

    private Set<@Pattern(regexp = "\\d{4,10}") String> additionalOrders;

    @Length(max = 255)
    private String orderComment;

    @Valid
    private PersonalDataDto personalData;

    @NotNull
    private boolean shouldBePaid;

    @NotNull
    private Long locationId;
}
