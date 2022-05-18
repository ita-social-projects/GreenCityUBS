package greencity.dto.bag;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class BagTransDto {
    @NonNull
    String name;
}
