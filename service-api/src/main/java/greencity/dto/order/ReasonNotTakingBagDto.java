package greencity.dto.order;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class ReasonNotTakingBagDto {
    private String description;
    private List<String> images;
}
