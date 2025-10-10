package greencity.service.ubs.user;

import static greencity.constant.ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import greencity.dto.AllActiveLocationsDto;
import greencity.dto.LocationWithTariffInfoDto;
import greencity.dto.OrderCourierPopUpDto;
import greencity.dto.RegionDto;
import greencity.dto.TariffInfoDto;
import greencity.dto.courier.CourierDto;
import greencity.entity.order.Order;
import greencity.entity.user.Location;
import greencity.exceptions.NotFoundException;
import greencity.repository.CourierRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TariffsInfoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourierServiceImpl implements CourierService {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final LocationRepository locationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final ModelMapper modelMapper;

    @Override
    public OrderCourierPopUpDto getInfoForCourierOrderingByCourierId(String uuid, Optional<String> changeLoc,
        Long courierId) {
        if (!courierRepository.existsCourierById(courierId)) {
            throw new NotFoundException(COURIER_IS_NOT_FOUND_BY_ID + courierId);
        }

        OrderCourierPopUpDto orderCourierPopUpDto = new OrderCourierPopUpDto();
        if (changeLoc.isPresent()) {
            orderCourierPopUpDto.setOrderIsPresent(false);
            orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocationsByCourierId(courierId));
            return orderCourierPopUpDto;
        }
        Optional<Order> lastOrder = orderRepository.getLastOrderOfUserByUUIDIfExists(uuid);
        orderCourierPopUpDto.setOrderIsPresent(lastOrder.isPresent());
        orderCourierPopUpDto.setAllActiveLocationsDtos(getAllActiveLocationsByCourierId(courierId));
        return orderCourierPopUpDto;
    }

    @Override
    public List<CourierDto> getAllActiveCouriers() {
        return courierRepository.getAllActiveCouriers().stream()
            .map(courier -> modelMapper.map(courier, CourierDto.class))
            .toList();
    }

    private List<AllActiveLocationsDto> getAllActiveLocationsByCourierId(Long courierId) {
        List<Location> locations = locationRepository.findAllActiveLocationsByCourierId(courierId);
        return getAllActiveLocationsDtos(locations, courierId);
    }

    private List<AllActiveLocationsDto> getAllActiveLocationsDtos(List<Location> locations, Long courierId) {
        Map<RegionDto, List<LocationWithTariffInfoDto>> map = locations.stream()
            .collect(toMap(x -> modelMapper.map(x, RegionDto.class),
                x -> new ArrayList<>(List.of(LocationWithTariffInfoDto.builder()
                    .locationId(x.getId())
                    .nameUk(x.getNameUk())
                    .nameEn(x.getNameEn())
                    .tariffInfoDto(modelMapper.map(
                        tariffsInfoRepository
                            .findTariffInfoByLocationIdAndCourierId(x.getId(), courierId)
                            .orElse(null),
                        TariffInfoDto.class))
                    .build())),
                (x, y) -> {
                    x.addAll(y);
                    return new ArrayList<>(x).stream().distinct().collect(toList());
                }));

        return map.entrySet().stream()
            .map(x -> AllActiveLocationsDto.builder()
                .regionId(x.getKey().getRegionId())
                .nameEn(x.getKey().getNameEn())
                .nameUk(x.getKey().getNameUk())
                .locations(x.getValue())
                .build())
            .toList();
    }
}
