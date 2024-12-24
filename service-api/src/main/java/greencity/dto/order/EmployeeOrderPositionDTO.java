package greencity.dto.order;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EmployeeOrderPositionDTO implements Serializable {
    private String name;
    private Long positionId;
}