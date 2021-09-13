package greencity.entity.language;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class Title {
    private long id;
    private String ua;
    private String en;
}
