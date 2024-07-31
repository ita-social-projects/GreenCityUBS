package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.repository.*;
import greencity.service.locations.LocationApiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;



@Slf4j
public final class PaymentUtil {
    public PaymentUtil() {
    }

    public static Long convertBillsIntoCoins(Double bills) {
        return bills == null ? 0
                : BigDecimal.valueOf(bills)
                .movePointRight(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(AppConstant.NO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
                .longValue();
    }

    public static Double convertCoinsIntoBills(Long coins) {
        return coins == null ? 0
                : BigDecimal.valueOf(coins)
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static Double setTotalPrice(CounterOrderDetailsDto dto) {
        if (isContainsExportedBags(dto)) {
            return dto.getSumExported();
        }
        if (isContainsConfirmedBags(dto)) {
            return dto.getSumConfirmed();
        }
        return dto.getSumAmount();
    }
    public static Boolean isContainsConfirmedBags(CounterOrderDetailsDto dto) {
        return dto.getSumConfirmed() != 0;
    }

    public static Boolean isContainsExportedBags(CounterOrderDetailsDto dto) {
        return dto.getSumExported() != 0;
    }

}
