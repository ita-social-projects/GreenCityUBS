package greencity.service;

import greencity.ModelUtils;
import greencity.dto.tariff.ChangeTariffLocationStatusDto;
import greencity.entity.enums.LocationStatus;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.exceptions.BadRequestException;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.service.ubs.SuperAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {

    @InjectMocks
    private SuperAdminServiceImpl superAdminService;

    @Mock
    private TariffLocationRepository tariffsLocationRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;

    @Test
    void changeTariffLocationsStatusParamActivate() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        TariffLocation tariffLocation = tariffsInfo.getTariffLocations().stream().findFirst().get();
        Location location = tariffLocation.getLocation();

        List<Long> locationsId = new ArrayList<>();
        locationsId.add(location.getId());
        ChangeTariffLocationStatusDto dto = new ChangeTariffLocationStatusDto().setLocationIds(locationsId);

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        superAdminService.changeTariffLocationsStatus(1L, dto, "activate");
        verify(tariffsLocationRepository).changeStatusAll(1L, locationsId, LocationStatus.ACTIVE.name());
    }

    @Test
    void changeTariffLocationsStatusParamDeactivate() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        TariffLocation tariffLocation = tariffsInfo.getTariffLocations().stream().findFirst().get();
        Location location = tariffLocation.getLocation();

        List<Long> locationsId = new ArrayList<>();
        locationsId.add(location.getId());
        ChangeTariffLocationStatusDto dto = new ChangeTariffLocationStatusDto().setLocationIds(locationsId);

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        superAdminService.changeTariffLocationsStatus(1L, dto, "deactivate");
        verify(tariffsLocationRepository).changeStatusAll(1L, locationsId, LocationStatus.DEACTIVATED.name());
    }

    @Test
    void changeTariffLocationsStatusParamUnresolvable() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        TariffLocation tariffLocation = tariffsInfo.getTariffLocations().stream().findFirst().get();
        Location location = tariffLocation.getLocation();

        List<Long> locationsId = new ArrayList<>();
        locationsId.add(location.getId());
        ChangeTariffLocationStatusDto dto = new ChangeTariffLocationStatusDto().setLocationIds(locationsId);

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        assertThrows(BadRequestException.class, () ->
                superAdminService.changeTariffLocationsStatus(1L, dto, "unresolvable"));
    }
}
