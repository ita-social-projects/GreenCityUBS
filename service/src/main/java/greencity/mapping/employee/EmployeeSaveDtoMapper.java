package greencity.mapping.employee;

import greencity.dto.employee.SaveEmployeeDto;
import greencity.dto.position.PositionDto;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EmployeeSaveDtoMapper extends AbstractConverter<Employee, SaveEmployeeDto> {
    @Override
    protected SaveEmployeeDto convert(Employee employee) {
        return SaveEmployeeDto.builder()
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .phoneNumber(employee.getPhoneNumber())
            .employeePositions(employee.getEmployeePosition().stream().map(
                position -> PositionDto.builder()
                    .id(position.getId())
                    .name(position.getName())
                    .build())
                .collect(Collectors.toList()))
            .image(employee.getImagePath())
            .tariffId(employee.getTariffInfos().stream().map(TariffsInfo::getId)
                .collect(Collectors.toList()))
            .build();
    }
}
