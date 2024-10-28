package greencity.mapping.tariff;

import greencity.dto.TariffInfoDto;
import greencity.dto.courier.CourierDto;
import greencity.entity.order.TariffsInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getTariffsInfo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class TariffInfoDtoMapperTest {
    @InjectMocks
    private TariffInfoDtoMapper mapper;

    @Test
    void convert() {
        TariffsInfo tariffsInfo = getTariffsInfo();
        tariffsInfo.getCourier().setCreatedBy(getEmployee());
        TariffInfoDto result = mapper.convert(tariffsInfo);
        assertEquals(tariffsInfo.getId(), result.getTariffInfoId());
        assertEquals(tariffsInfo.getMin(), result.getMin());
        assertEquals(tariffsInfo.getMax(), result.getMax());
        assertEquals(tariffsInfo.getCourierLimit(), result.getCourierLimit());
        assertEquals(tariffsInfo.getLimitDescription(), result.getLimitDescription());
        CourierDto resultCourier = result.getCourierDto();
        assertEquals(tariffsInfo.getCourier().getId(), resultCourier.getCourierId());
        assertEquals(tariffsInfo.getCourier().getCourierStatus().toString(), resultCourier.getCourierStatus());
        assertEquals(tariffsInfo.getCourier().getNameEn(), resultCourier.getNameEn());
        assertEquals(tariffsInfo.getCourier().getNameUk(), resultCourier.getNameUk());
        assertEquals(tariffsInfo.getCourier().getCreateDate(), resultCourier.getCreateDate());
        assertEquals(tariffsInfo.getCourier().getCreatedBy().getEmail(), resultCourier.getCreatedBy());
    }

    @Test
    void convertCaseCourierNull() {
        TariffsInfo tariffsInfo = getTariffsInfo();
        tariffsInfo.setCourier(null);
        TariffInfoDto result = mapper.convert(tariffsInfo);
        assertEquals(tariffsInfo.getId(), result.getTariffInfoId());
        assertEquals(tariffsInfo.getMin(), result.getMin());
        assertEquals(tariffsInfo.getMax(), result.getMax());
        assertEquals(tariffsInfo.getCourierLimit(), result.getCourierLimit());
        assertEquals(tariffsInfo.getLimitDescription(), result.getLimitDescription());
        assertNull(result.getCourierDto());
    }

    @Test
    void convertCaseNull() {
        TariffsInfo tariffsInfo = null;
        TariffInfoDto result = mapper.convert(tariffsInfo);
        assertNull(result);
    }
}
