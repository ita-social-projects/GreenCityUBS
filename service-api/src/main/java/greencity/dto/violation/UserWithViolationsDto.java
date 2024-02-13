package greencity.dto.violation;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class UserWithViolationsDto {
    private String username;
    private Long numberOfViolations;
    private List<UserViolationsDto> userViolationsList;
}
