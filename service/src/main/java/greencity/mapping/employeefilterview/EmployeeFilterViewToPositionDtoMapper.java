package greencity.mapping.employeefilterview;

import greencity.dto.position.PositionDto;
import greencity.entity.user.employee.EmployeeFilterView;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EmployeeFilterViewToPositionDtoMapper extends AbstractConverter<EmployeeFilterView, PositionDto> {
    @Override
    protected PositionDto convert(EmployeeFilterView employeeFilterView) {
        return PositionDto.builder()
            .id(employeeFilterView.getPositionId())
            .name(employeeFilterView.getPositionName())
            .nameEn(employeeFilterView.getPositionNameEn())
            .build();
    }
}
