package greencity.mapping.tariff;

import greencity.ModelUtils;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.courier.CourierDto;
import greencity.entity.order.TariffsInfo;
import greencity.mapping.location.TariffsForLocationDtoMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TariffsForLocationDtoMapperTest {

    @InjectMocks
    TariffsForLocationDtoMapper mapper;

    @Test
    void convert() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        TariffsForLocationDto dto = mapper.convert(tariffsInfo);

        Assertions.assertEquals(tariffsInfo.getId(), dto.getTariffInfoId());
        Assertions.assertEquals(tariffsInfo.getCourierLimit(), dto.getCourierLimit());
        Assertions.assertEquals(CourierDto.builder().courierId(tariffsInfo.getCourier().getId())
            .nameUk(tariffsInfo.getCourier().getNameUk())
            .nameEn(tariffsInfo.getCourier().getNameEn())
            .courierStatus(tariffsInfo.getCourier().getCourierStatus().name())
            .createDate(tariffsInfo.getCourier().getCreateDate())
            .build(), dto.getCourierDto());
        Assertions.assertEquals(tariffsInfo.getMin(), dto.getMin());
        Assertions.assertEquals(tariffsInfo.getMax(), dto.getMax());
    }
}
