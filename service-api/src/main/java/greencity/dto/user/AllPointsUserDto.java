package greencity.dto.user;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AllPointsUserDto {
    private Integer userBonuses;
    private List<PointsForUbsUserDto> ubsUserBonuses;
}
