package greencity.mapping.employeefilterview;

import greencity.dto.courier.GetReceivingStationDto;
import greencity.entity.user.employee.EmployeeFilterView;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EmployeeFilterViewToGetReceivingStationDtoMapper
    extends AbstractConverter<EmployeeFilterView, GetReceivingStationDto> {
    @Override
    protected GetReceivingStationDto convert(EmployeeFilterView employeeFilterView) {
        return GetReceivingStationDto.builder()
            .stationId(employeeFilterView.getReceivingStationId())
            .name(employeeFilterView.getReceivingStationName())
            .build();
    }
}
