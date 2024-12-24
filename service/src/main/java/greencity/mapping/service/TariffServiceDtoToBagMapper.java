package greencity.mapping.service;

import greencity.constant.AppConstant;
import greencity.dto.service.TariffServiceDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class TariffServiceDtoToBagMapper extends AbstractConverter<TariffServiceDto, Bag> {
    @Override
    protected Bag convert(TariffServiceDto source) {
        return Bag.builder()
            .capacity(source.getCapacity())
            .price(convertIntoCoins(source.getPrice()))
            .commission(convertIntoCoins(source.getCommission()))
            .fullPrice(convertIntoCoins(source.getPrice() + source.getCommission()))
            .limitIncluded(false)
            .name(source.getName())
            .nameEng(source.getNameEng())
            .description(source.getDescription())
            .descriptionEng(source.getDescriptionEng())
            .createdAt(LocalDate.now())
            .build();
    }

    private Long convertIntoCoins(Double bills) {
        return BigDecimal.valueOf(bills)
            .movePointRight(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(AppConstant.NO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
            .longValue();
    }
}
