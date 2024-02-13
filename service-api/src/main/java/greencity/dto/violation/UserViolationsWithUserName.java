package greencity.dto.violation;

import greencity.dto.pageble.PageableDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
