package greencity.mapping;

import greencity.ModelUtils;
import greencity.entity.order.TariffsInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTariffsInfoDtoMapperTest {
    @InjectMocks
    private GetTariffsInfoDtoMapper mapper;

    @Test
    void convert() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        String creator =
            tariffsInfo.getCreator().getRecipientName() + " " + tariffsInfo.getCreator().getRecipientSurname();

        Assertions.assertEquals(tariffsInfo.getId(), mapper.convert(tariffsInfo).getCardId());
        Assertions.assertEquals(
            tariffsInfo.getCourierLocations().get(0).getCourier().getCourierTranslationList().get(0).getName(),
            mapper.convert(tariffsInfo).getCourierTranslationDtos().get(0).getName());
        Assertions.assertEquals(creator, mapper.convert(tariffsInfo).getCreator());
        Assertions.assertEquals(tariffsInfo.getCreatedAt(), mapper.convert(tariffsInfo).getCreatedAt());
        Assertions.assertEquals(tariffsInfo.getCourierLocations().get(0).getLocation()
            .getLocationTranslations().get(0).getLocationName(),
            mapper.convert(tariffsInfo).getLocationInfoDto()
                .getLocationsDto().get(0).getLocationTranslationDtoList().get(0).getLocationName());
    }
}
