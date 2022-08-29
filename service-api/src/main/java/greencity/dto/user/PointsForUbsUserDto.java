package greencity.dto.user;

import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode

public class PointsForUbsUserDto {
    private ZonedDateTime dateOfEnrollment;
    private Long numberOfOrder;
    private Integer amount;
}
