package greencity.service.ubs.tariff;

import greencity.dto.TariffInfoByLocationDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.entity.order.Courier;
import greencity.entity.order.TariffsInfo;
import greencity.exceptions.NotFoundException;
import greencity.repository.CourierRepository;
import greencity.repository.TariffsInfoRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import static greencity.constant.ErrorMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TariffServiceImpl implements TariffService {
    private final CourierRepository courierRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final ModelMapper modelMapper;

    @Override
    public TariffInfoByLocationDto getTariffInfo(Long tariffId) {
        TariffsInfo tariffsInfo = findTariffsInfoByTariffId(tariffId);

        return TariffInfoByLocationDto.builder()
            .orderIsPresent(true)
            .tariffsForLocationDto(modelMapper.map(
                tariffsInfo, TariffsForLocationDto.class))
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
    public List<GetActiveTariffInfoDto> getTariffsInfo(Long courierId) {
        Courier courier = courierRepository.findById(courierId).orElseThrow(
            () -> new NotFoundException(COURIER_IS_NOT_FOUND_BY_ID + courierId));
        return tariffsInfoRepository.findAllActiveTariffsInfoWithCourierId(courier)
            .stream()
            .map(entity -> modelMapper.map(entity, GetActiveTariffInfoDto.class))
            .toList();
    }

    private TariffsInfo findTariffsInfoByTariffId(Long tariffId) {
        return tariffsInfoRepository.findById(tariffId)
            .orElseThrow(() -> new NotFoundException(TARIFF_NOT_FOUND + tariffId));
    }
}
