package greencity.dto;

import java.util.Set;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

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
    private Integer pointsToUse;

    @NotNull
    private Integer sumToPay;

    private Set<String> cerfiticates;

    private Set<String> additionalOrders;

    @Length(max = 170)
    private String orderComment;

    @Valid
    private PersonalDataDto personalData;
}
