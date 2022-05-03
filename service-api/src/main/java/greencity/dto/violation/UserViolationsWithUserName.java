package greencity.dto.violation;

import greencity.dto.pageble.PageableDto;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserViolationsWithUserName {
    String fullName;
    PageableDto<UserViolationsDto> userViolationsDto;
}
