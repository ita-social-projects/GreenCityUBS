package greencity.util;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bot implements Serializable {
    private String type;
    private String link;
}
