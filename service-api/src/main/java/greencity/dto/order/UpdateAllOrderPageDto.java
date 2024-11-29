package greencity.dto.order;

import greencity.dto.employee.UpdateResponsibleEmployeeDto;
import lombok.*;

import javax.validation.Valid;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateAllOrderPageDto {
    private List<Long> orderId;
    @Valid
    private ExportDetailsDtoUpdate exportDetailsDto;
    private List<UpdateResponsibleEmployeeDto> updateResponsibleEmployeeDto;
}
