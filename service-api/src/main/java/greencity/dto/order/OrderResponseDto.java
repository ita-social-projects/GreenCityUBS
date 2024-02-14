package greencity.dto.order;

import greencity.dto.bag.BagDto;
import greencity.dto.user.PersonalDataDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
