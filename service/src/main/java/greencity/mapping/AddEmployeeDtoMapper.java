package greencity.mapping;

import greencity.dto.AddEmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link AddEmployeeDto} into
 * {@link Employee}.
 */
@Component
public class AddEmployeeDtoMapper extends AbstractConverter<AddEmployeeDto, Employee> {
    /**
     * Method convert {@link AddEmployeeDto} to {@link Employee}.
     *
     * @return {@link Employee}
     */
    @Override
    protected Employee convert(AddEmployeeDto dto) {
        return Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .employeePosition(dto.getEmployeePositions().stream()
                        .map(p -> Position.builder()
                                .id(p.getId())
                                .position(p.getPosition())
                                .build())
                        .collect(Collectors.toSet()))
                .receivingStation(dto.getReceivingStations().stream()
                        .map(r -> ReceivingStation.builder()
                                .id(r.getId())
                                .receivingStation(r.getReceivingStation())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
