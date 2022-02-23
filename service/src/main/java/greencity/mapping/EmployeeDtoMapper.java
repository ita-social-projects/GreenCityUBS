package greencity.mapping;

import greencity.dto.EmployeeDto;
import greencity.entity.enums.EmployeeStatus;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link EmployeeDto} into
 * {@link Employee}.
 */
@Component
public class EmployeeDtoMapper extends AbstractConverter<EmployeeDto, Employee> {
    /**
     * Method convert {@link EmployeeDto} to {@link Employee}.
     *
     * @return {@link Employee}
     */
    @Override
    protected Employee convert(EmployeeDto dto) {
        return Employee.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .imagePath(dto.getImage())
                .employeeStatus(EmployeeStatus.ACTIVE)
                .employeePosition(dto.getEmployeePositions().stream()
                        .map(p -> Position.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .build())
                        .collect(Collectors.toSet()))
                .receivingStation(dto.getReceivingStations().stream()
                        .map(r -> ReceivingStation.builder()
                                .id(r.getId())
                                .name(r.getName())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
