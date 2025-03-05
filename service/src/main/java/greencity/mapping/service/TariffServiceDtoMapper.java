package greencity.mapping.service;

import greencity.constant.AppConstant;
import greencity.dto.service.TariffServiceDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class TariffServiceDtoMapper extends AbstractConverter<Bag, TariffServiceDto> {
    @Override
    protected TariffServiceDto convert(Bag source) {
        return TariffServiceDto.builder()
            .capacity(source.getCapacity())
            .price(convertIntoBills(source.getPrice()))
            .commission(convertIntoBills(source.getCommission()))
            .name(source.getNameUk())
            .nameEng(source.getNameEn())
            .description(source.getDescriptionUk())
            .descriptionEng(source.getDescriptionEn())
            .build();
    }

    private Double convertIntoBills(Long coins) {
        return BigDecimal.valueOf(coins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .doubleValue();
    }
}
