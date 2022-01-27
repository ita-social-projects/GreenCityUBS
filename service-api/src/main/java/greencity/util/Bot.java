package greencity.util;

import lombok.*;

import java.io.Serializable;

@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class Bot implements Serializable {
    private String type;
    private String link;
}
