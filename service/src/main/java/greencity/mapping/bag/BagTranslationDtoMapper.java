package greencity.mapping.bag;

import greencity.constant.AppConstant;
import greencity.dto.bag.BagTranslationDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class BagTranslationDtoMapper extends AbstractConverter<Bag, BagTranslationDto> {
    @Override
    protected BagTranslationDto convert(Bag source) {
        return BagTranslationDto.builder()
            .id(source.getId())
            .capacity(source.getCapacity())
            .price(BigDecimal.valueOf(source.getFullPrice())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .limitedIncluded(source.getLimitIncluded())
            .build();
    }
}
