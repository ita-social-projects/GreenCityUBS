package greencity.mapping;

import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.position.PositionDto;
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
                    .name(p.getName())
                    .build())
                .collect(Collectors.toList()))
//            .receivingStations(employee.getReceivingStation().stream()
//                .map(r -> ReceivingStationDto.builder()
//                    .id(r.getId())
//                    .name(r.getName())
//                    .build())
//                .collect(Collectors.toList()))
            .build();
    }
}
