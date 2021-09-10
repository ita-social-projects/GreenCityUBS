package greencity.entity.language;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@ToString
@Builder
public class Title {
    private long id;
    private String ua;
    private String en;
}