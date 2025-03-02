package greencity.dto.order;

import greencity.dto.employee.UpdateResponsibleEmployeeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateAllOrderPageDto {
    private List<Long> orderId;
    private ExportDetailsDtoUpdate exportDetailsDto;
    private List<UpdateResponsibleEmployeeDto> updateResponsibleEmployeeDto;
}
