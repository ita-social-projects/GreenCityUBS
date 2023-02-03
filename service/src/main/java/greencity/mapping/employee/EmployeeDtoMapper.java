package greencity.mapping.employee;

import greencity.dto.employee.EmployeeDto;
import greencity.entity.order.Courier;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.CourierStatus;
import greencity.enums.EmployeeStatus;
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
            .tariffInfos(dto.getTariffs().stream()
                .map(t -> TariffsInfo.builder()
                    .id(t.getId())
                    .min(t.getMin())
                    .max(t.getMax())
                    .courierLimit(t.getCourierLimit())
                    .courier(Courier.builder()
                        .id(t.getCourier().getCourierId())
                        .courierStatus(CourierStatus.ACTIVE)
                        .nameUk(t.getCourier().getNameUk())
                        .nameEn(t.getCourier().getNameEn())
                        .build())
                    .receivingStationList(t.getReceivingStations().stream()
                        .map(station -> ReceivingStation.builder()
                            .id(station.getId())
                            .name(station.getName())
                            .createDate(station.getCreateDate())
                            .build())
                        .collect(Collectors.toSet()))
                    .tariffLocations(t.getTariffLocations())
                    .build())
                .collect(Collectors.toSet()))
            .build();
    }
}
