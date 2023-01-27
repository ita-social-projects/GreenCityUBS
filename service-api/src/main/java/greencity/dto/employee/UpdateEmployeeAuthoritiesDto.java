package greencity.dto.employee;

import greencity.dto.position.PositionDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class UpdateEmployeeAuthoritiesDto {
    String email;
    List<PositionDto> positions;
}
