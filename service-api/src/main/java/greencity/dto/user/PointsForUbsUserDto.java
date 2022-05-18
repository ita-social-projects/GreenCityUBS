package greencity.dto.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode

public class PointsForUbsUserDto {
    private LocalDateTime dateOfEnrollment;
    private Long numberOfOrder;
    private Integer amount;
}
