package greencity.entity.language;

import lombok.*;

//@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class Title {
    private long id;
    private String belonging;
    private String ua;
    private String en;
}
