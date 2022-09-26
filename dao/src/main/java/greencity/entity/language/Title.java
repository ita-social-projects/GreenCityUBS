package greencity.entity.language;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Title {
    private long id;
    private String belonging;
    private String ua;
    private String en;
}
