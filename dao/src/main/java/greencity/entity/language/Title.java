package greencity.entity.language;

import lombok.*;

//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@ToString
@Builder
//@Table(name = "translatedTitles")
public class Title {
    //@Id
    private long id;
    //@Column(name = "ukrainian", nullable = false)
    private String ua;
    //@Column(name = "english", nullable = false)
    private String en;
}

