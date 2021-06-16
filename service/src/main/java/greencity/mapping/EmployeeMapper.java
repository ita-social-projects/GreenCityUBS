package greencity.mapping;

import greencity.dto.EmployeeDto;
import greencity.dto.PositionDto;
import greencity.dto.ReceivingStationDto;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link Employee} into
 * {@link EmployeeDto}.
 */
@Component
public class EmployeeMapper extends AbstractConverter<Employee, EmployeeDto> {
    /**
     * Method convert {@link Employee} to {@link EmployeeDto}.
     *
     * @return {@link EmployeeDto}
     */
    @Override
    protected EmployeeDto convert(Employee employee) {
        return EmployeeDto.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .phoneNumber(employee.getPhoneNumber())
            .email(employee.getEmail())
            .image(employee.getImagePath())
            .employeePositions(employee.getEmployeePosition().stream()
                .map(p -> PositionDto.builder()
                    .id(p.getId())
                    .position(p.getPosition())
                    .build())
                .collect(Collectors.toList()))
            .receivingStations(employee.getReceivingStation().stream()
                .map(r -> ReceivingStationDto.builder()
                    .id(r.getId())
                    .receivingStation(r.getReceivingStation())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
