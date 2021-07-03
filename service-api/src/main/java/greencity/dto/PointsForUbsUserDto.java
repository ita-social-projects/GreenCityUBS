package greencity.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder

public class PointsForUbsUserDto {
    private LocalDateTime dateOfEnrollment;
    private Long numberOfOrder;
    private Integer amount;
}
