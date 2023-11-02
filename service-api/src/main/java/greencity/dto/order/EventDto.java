package greencity.dto.order;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EventDto {
    @NotNull
    @Length(min = 1)
    private Long id;
    private LocalDateTime eventDate;
    private String eventName;
    private String authorName;
}
