package greencity.mapping.employee;

import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.position.PositionDto;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EmployeeUpdateDtoMapper extends AbstractConverter<Employee, EmployeeWithTariffsIdDto> {
    @Override
    protected EmployeeWithTariffsIdDto convert(Employee employee) {
        return EmployeeWithTariffsIdDto.builder()
            .employeeDto(EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .image(employee.getImagePath())
                .employeePositions(employee.getEmployeePosition().stream()
                    .map(position -> PositionDto.builder()
                        .id(position.getId())
                        .name(position.getName())
                        .nameEN(position.getNameEN())
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .tariffId(employee.getTariffInfos()
                .stream()
                .map(TariffsInfo::getId)
                .collect(Collectors.toList()))
            .build();
    }
}
