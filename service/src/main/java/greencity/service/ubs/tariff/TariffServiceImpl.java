package greencity.service.ubs.tariff;

import static greencity.constant.ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_ORDER_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND_BY_LOCATION_ID;
import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.exceptions.NotFoundException;
import greencity.repository.CourierRepository;
import greencity.repository.LocationRepository;
import greencity.repository.TariffsInfoRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TariffServiceImpl implements TariffService {
    private final CourierRepository courierRepository;
    private final LocationRepository locationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final ModelMapper modelMapper;

    @Override
    public TariffInfoByLocationDto getTariffInfoForLocation(Long courierId, Long locationId) {
        if (!courierRepository.existsCourierById(courierId)) {
            throw new NotFoundException(COURIER_IS_NOT_FOUND_BY_ID + courierId);
        }
        if (!locationRepository.existsById(locationId)) {
            throw new NotFoundException(LOCATION_DOESNT_FOUND_BY_ID + locationId);
        }
        return TariffInfoByLocationDto.builder()
            .orderIsPresent(true)
            .tariffsForLocationDto(modelMapper.map(
                findTariffsInfoByCourierAndLocationId(courierId, locationId), TariffsForLocationDto.class))
            .build();
    }

    @Override
    public TariffsForLocationDto getTariffForOrder(Long id) {
        Optional<TariffsInfo> tariffsInfo = tariffsInfoRepository.findByOrdersId(id);
        if (tariffsInfo.isPresent()) {
            return modelMapper.map(tariffsInfo.get(), TariffsForLocationDto.class);
        } else {
            throw new NotFoundException(TARIFF_FOR_ORDER_NOT_EXIST + id);
        }
    }

    @Override
    public boolean checkIfTariffExistsById(Long tariffInfoId) {
        return tariffsInfoRepository.existsById(tariffInfoId);
    }

    @Override
    public List<Long> getTariffIdByLocationId(Long locationId) {
        return tariffsInfoRepository.findTariffIdByLocationId(locationId)
            .filter(list -> !list.isEmpty())
            .orElseThrow(() -> new NotFoundException(String.format(TARIFF_NOT_FOUND_BY_LOCATION_ID, locationId)));
    }

    @Override
    public List<GetActiveTariffInfoDto> getTariffsInfo() {
        return tariffsInfoRepository.findAllActiveTariffsInfo()
            .stream()
            .map(entity -> {
                GetActiveTariffInfoDto dto = modelMapper.map(entity, GetActiveTariffInfoDto.class);
                Set<TariffLocation> locations = Optional.ofNullable(entity.getTariffLocations()).orElse(Set.of());
                if (locations.size() > 1) {
                    String locationsUk = joinLocationNames(locations, true);
                    String locationsEn = joinLocationNames(locations, false);
                    dto.setDescriptionMessageUk(
                        "До тарифу також включені " + locationsUk
                    );
                    dto.setDescriptionMessageEn(
                        "The tariff also includes " + locationsEn
                    );
                }
                return dto;
            })
            .toList();
    }

    private TariffsInfo findTariffsInfoByCourierAndLocationId(Long courierId, Long locationId) {
        return tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(courierId, locationId)
            .orElseThrow(
                () -> new NotFoundException(
                    String.format(TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST, courierId, locationId)));
    }

    private String joinLocationNames(Set<TariffLocation> locations, boolean isUk) {
        return locations.stream()
            .map(loc -> isUk ? loc.getLocation().getNameUk() : loc.getLocation().getNameEn())
            .collect(Collectors.joining(", "));
    }
}
