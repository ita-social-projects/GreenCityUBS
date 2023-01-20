package greencity.dto.employee;

import greencity.dto.position.PositionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeAuthoritiesDto {
    String email;
    List<PositionDto> positions;
}
