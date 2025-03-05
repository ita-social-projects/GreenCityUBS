package greencity.mapping.service;

import greencity.constant.AppConstant;
import greencity.dto.service.GetTariffServiceDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class GetTariffServiceDtoMapper extends AbstractConverter<Bag, GetTariffServiceDto> {
    @Override
    protected GetTariffServiceDto convert(Bag source) {
        return GetTariffServiceDto.builder()
            .id(source.getId())
            .capacity(source.getCapacity())
            .price(convertIntoBills(source.getPrice()))
            .commission(convertIntoBills(source.getCommission()))
            .fullPrice(convertIntoBills(source.getFullPrice()))
            .name(source.getNameUk())
            .nameEng(source.getNameEn())
            .description(source.getDescriptionUk())
            .descriptionEng(source.getDescriptionEn())
            .limitIncluded(source.getLimitIncluded())
            .build();
    }

    private Double convertIntoBills(Long coins) {
        return BigDecimal.valueOf(coins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .doubleValue();
    }
}
