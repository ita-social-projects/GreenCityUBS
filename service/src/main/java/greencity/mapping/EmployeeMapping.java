package greencity.mapping;

import greencity.dto.EmployeeDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link Employee} into
 * {@link EmployeeDto}.
 */
@Component
public class EmployeeMapping extends AbstractConverter<Employee, EmployeeDto> {
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
              /*  .employeePositions(employee.getEmployeePosition()
                        .stream().map(Position::getPosition)
                        .collect(Collectors.toList()))*/
                .receivingStations(employee.getReceivingStation()
                        .stream().map(ReceivingStation::getReceivingStation)
                        .collect(Collectors.toList()))
                .build();
    }
}
