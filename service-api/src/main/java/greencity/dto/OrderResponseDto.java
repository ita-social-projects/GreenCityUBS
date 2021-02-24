package greencity.dto;

import javax.validation.constraints.Pattern;
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

    @Pattern(regexp = "(\\d{4}-\\d{4})|(^$)", message = "This sertifacate code is not valid")
    private String cerfiticate;

    @Length(max = 11)
    private String additionalOrder;

    @Length(max = 170)
    private String orderComment;

    @Valid
    private PersonalDataDto personalData;
}
