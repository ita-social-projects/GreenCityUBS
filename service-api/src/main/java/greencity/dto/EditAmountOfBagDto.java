package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditAmountOfBagDto {
    @NotNull
    private Long minAmountOfBigBags;
    @NotNull
    private Long maxAmountOfBigBags;
    @NotNull
    private Long languageId;
}
