package greencity.dto;

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
