package greencity.dto.bag;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class MinBagAmountDto {
    private Long minAmountOfBags;
}
