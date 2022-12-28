package greencity.mapping.employee;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.GetReceivingStationDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GetEmployeeDtoMapper extends AbstractConverter<Employee, GetEmployeeDto> {

    @Override
    protected GetEmployeeDto convert(Employee source) {
        List<PositionDto> positionDtos = source.getEmployeePosition()
                .stream()
                .map(position -> PositionDto.builder()
                        .id(position.getId())
                        .name(position.getName())
                        .build())
                .collect(Collectors.toList());

        Set<GetTariffInfoForEmployeeDto> tariffInfoForEmployeeDtos = source.getTariffInfos()
                .stream().map(tariffsInfo -> GetTariffInfoForEmployeeDto.builder()
                        .id(tariffsInfo.getId())
                        .courierId(tariffsInfo.getCourier().getId())
                        .courierTranslationDtos(tariffsInfo.getCourier().getCourierTranslationList()
                                .stream()
                                .map(courierTranslation -> CourierTranslationDto.builder()
                                .name(courierTranslation.getName())
                                .nameEng(courierTranslation.getNameEng())
                                .build())
                        .collect(Collectors.toList()))
                        .locationsDtos(tariffsInfo.getTariffLocations()
                                .stream()
                                .map(tariffLocation -> LocationsDtos.builder()
                                        .locationId(tariffLocation.getLocation().getId())
                                        .nameUk(tariffLocation.getLocation().getNameUk())
                                        .nameEn(tariffLocation.getLocation().getNameEn())
                                        .build())
                                        .collect(Collectors.toList()))
                        .receivingStationDtos(tariffsInfo.getReceivingStationList()
                                .stream()
                                .map(receivingStation -> GetReceivingStationDto.builder()
                                        .stationId(receivingStation.getId())
                                        .name(receivingStation.getName())
                                        .build())
                                .collect(Collectors.toList()))
                        .region(tariffsInfo.getTariffLocations().stream()
                                .map(tariffLocation -> RegionDto.builder()
                                        .regionId(tariffLocation.getLocation().getRegion().getId())
                                        .nameEn(tariffLocation.getLocation().getRegion().getEnName())
                                        .nameUk(tariffLocation.getLocation().getRegion().getUkrName())
                                        .build()).collect(Collectors.toList()).get(0))
                        .build()).collect(Collectors.toSet());
        return GetEmployeeDto.builder()
                .id(source.getId())
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .phoneNumber(source.getPhoneNumber())
                .email(source.getEmail())
                .image(source.getImagePath())
                .employeePositions(positionDtos)
                .tariffs(tariffInfoForEmployeeDtos)
                .build();
    }
}
