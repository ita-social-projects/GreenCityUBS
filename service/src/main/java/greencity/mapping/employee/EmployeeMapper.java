package greencity.mapping.employee;

import greencity.dto.LocationsDtos;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.position.PositionDto;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.NotFoundException;
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
            .receivingStations(employee.getTariffInfos().stream()
                .flatMap(tariff -> tariff.getReceivingStationList().stream()
                    .map(station -> ReceivingStationDto.builder()
                        .id(station.getId())
                        .name(station.getName())
                        .createDate(station.getCreateDate())
                        .build()))
                .collect(Collectors.toList()))
            .location(employee.getTariffInfos().stream()
                .flatMap(tariffsInfo -> tariffsInfo.getTariffLocations().stream())
                .map(TariffLocation::getLocation)
                .map(location -> LocationsDtos.builder()
                    .nameEn(location.getNameEn())
                    .nameUk(location.getNameUk())
                    .locationId(location.getId())
                    .build())
                .findFirst().orElseThrow(() -> new NotFoundException("Location is not found")))
            .courier(employee.getTariffInfos().stream()
                .map(TariffsInfo::getCourier)
                .map(courier -> CourierDto.builder()
                    .courierId(courier.getId())
                    .createDate(courier.getCreateDate())
                    .courierStatus(courier.getCourierStatus().name())
                    .nameUk(courier.getNameUk())
                    .nameEn(courier.getNameEn())
                    .build())
                .findFirst().orElseThrow(() -> new NotFoundException("Courier is not found")))
            .build();
    }
}
