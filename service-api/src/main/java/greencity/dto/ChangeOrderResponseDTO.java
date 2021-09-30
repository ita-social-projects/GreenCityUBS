package greencity.dto;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ChangeOrderResponseDTO {
    private HttpStatus httpStatus;
    private List<Long> unresolvedGoalsOrderId;
}
