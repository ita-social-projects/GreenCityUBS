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
            .nameUk(source.getNameUk())
            .nameEn(source.getNameEn())
            .descriptionUk(source.getDescriptionUk())
            .descriptionEn(source.getDescriptionEn())
            .build();
    }

    private Double convertIntoBills(Long coins) {
        return BigDecimal.valueOf(coins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .doubleValue();
    }
}
