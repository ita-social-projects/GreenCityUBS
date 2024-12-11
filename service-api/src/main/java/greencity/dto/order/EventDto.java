package greencity.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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