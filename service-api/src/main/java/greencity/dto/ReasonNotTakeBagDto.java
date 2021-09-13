package greencity.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class ReasonNotTakeBagDto {
    private String description;
    private LocalDate time;
    private String currentUser;
    private List<String> images;
}
